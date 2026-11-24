# 한글 깨짐 복구 런북 (DB 이관 후 재실행용)

- 작성일: 2026-11-17
- 대상: Oracle(레거시 `emart_cms3.2.1`) → PostgreSQL로 이관된 **모든 DB**
  (로컬 `EMTINDB`, 서버 RDS `db_didinfo`, 이후 신규 이관 DB 등)
- 원인 분석 상세: [DB_ENCODING_MIGRATION.md](DB_ENCODING_MIGRATION.md)

> 기존 `02_recover_update.sql`은 2026-07-26 시점 `EMTINDB`의 `ctid`를 하드코딩한 스크립트라
> **다른 DB나 재이관된 DB에는 쓸 수 없다.** 이 런북의 SQL은 실행하는 DB를 직접 스캔해서
> 복구하므로 어느 DB에서든 다시 실행할 수 있다.

---

## 1. 증상

화면에 한글이 자모 조각이나 반각 문자로 깨져서 보인다.

| 화면 | 깨진 예 | 원래 값 |
|---|---|---|
| 지점(매장) 관리 | `￀ￌﾸﾶￆﾮ ﾾ￧￁ￖ￁ﾡ` | 이마트 양주점 |
| 문화센터 조직명 | `ﾹﾮ￈ﾭﾼﾾￅￍ` | 문화센터 |
| 강의 관리 (강의명/강사명/강의실) | `ㅓㄹㄹㄹ]ㅁㄱㅔ...` | 강의명 |
| 화면 관리 (편성명칭/편성형태) | `ㄹㄱㅇㅙ1*1` | 가로1*1 |

영문, 숫자, 공백은 정상이고 한글만 깨진다.

## 2. 원인 (요약)

이관할 때 CP949(EUC-KR) 바이트를 Java `(char) byte`로 캐스팅해서 **부호 확장**이 일어났다.
그 결과 `0x80` 이상 바이트가 각각 `U+FF00 + byte` 문자 하나로 저장됐다.

```
원본 '이' = CP949 0xC0 0xCC  →  DB 저장값 U+FFC0 U+FFCC  ('￀ￌ')
```

1바이트가 1문자로 대응하는 **완전 가역 변환**이다. 각 문자의 하위 1바이트를 모아 CP949(UHC)로
다시 디코딩하면 원본이 그대로 복구된다.

- 탐지 패턴: `~ '[ﾀ-￿]'`
- 복구 식: `convert_from(decode(<각 문자 & 0xFF 의 hex>, 'hex'), 'UHC')`

---

## 3. 실행 절차

> ⚠️ **실행 전 확인**
> - 접속한 DB가 맞는지 확인한다. 서버 프로파일(`server`)은 RDS `db_didinfo`이고,
>   로컬(`local`)은 `27.96.130.69:8088/EMTINDB`이다(`backend/src/main/resources/application.yml`).
>   2026-11-17에 로컬 DB에 실행하고 서버 화면에 반영되지 않은 사례가 있었다.
> - 1~4단계는 **같은 세션(같은 SQL 창)** 에서 순서대로 실행한다. 임시 테이블(`TEMP`)은
>   세션이 끊기면 사라진다.
> - 트래픽이 적은 시간에 실행한다. `ctid` 기반으로 UPDATE하기 때문이다(PK가 없는 테이블이 많음).

### STEP 1. 진단: 깨진 테이블과 컬럼 찾기 (조회만 함)

```sql
CREATE TEMP TABLE IF NOT EXISTS KOR_SCAN (TBL TEXT, COL TEXT, CNT BIGINT);
TRUNCATE KOR_SCAN;

DO $$
DECLARE
    c RECORD;
    n BIGINT;
BEGIN
    FOR c IN
        SELECT col.table_name, col.column_name
        FROM information_schema.columns col
        JOIN information_schema.tables t
          ON t.table_schema = col.table_schema
         AND t.table_name   = col.table_name
         AND t.table_type   = 'BASE TABLE'
        WHERE col.table_schema = current_schema()
          AND col.data_type IN ('character varying', 'text', 'character')
          AND col.table_name !~ '_bak_'          -- 백업 테이블 제외
    LOOP
        EXECUTE format('SELECT count(*) FROM %I WHERE %I ~ ''[ﾀ-￿]''',
                       c.table_name, c.column_name) INTO n;
        IF n > 0 THEN
            INSERT INTO KOR_SCAN VALUES (c.table_name, c.column_name, n);
        END IF;
    END LOOP;
END $$;

SELECT * FROM KOR_SCAN ORDER BY TBL, COL;
```

결과가 0건이면 복구할 데이터가 없는 것이다. 여기서 끝낸다.

### STEP 2. 백업 (DB 객체 생성: 실행 전 승인 필요)

영향받는 테이블을 통째로 복사한다. 아래 SELECT를 실행하면 `CREATE TABLE` 문이 만들어진다.
그 결과를 복사해서 실행한다.

```sql
SELECT DISTINCT format('CREATE TABLE %I AS SELECT * FROM %I;',
                       TBL || '_bak_' || to_char(now(), 'YYYYMMDD'), TBL) AS BACKUP_SQL
FROM KOR_SCAN
ORDER BY 1;
```

> 백업 테이블명 규칙은 기존 산출물(`01_backup_tables.sql`)과 같은 `{테이블}_bak_YYYYMMDD`이다.
> 복구를 확인한 뒤 일정 기간이 지나면 `DROP TABLE`로 정리한다.

### STEP 3. 복구 (미리보기 → 실제 적용)

```sql
BEGIN;

CREATE TEMP TABLE IF NOT EXISTS KOR_FIX_LOG (
    TBL TEXT, ROW_ID TEXT, COL TEXT,
    BEFORE_VAL TEXT, AFTER_VAL TEXT, STATUS TEXT, ERR TEXT
);
TRUNCATE KOR_FIX_LOG;

DO $$
DECLARE
    v_dry       BOOLEAN := true;    -- ★ true: 미리보기(변경 안 함) / false: 실제 UPDATE
    v_trim_tail BOOLEAN := false;   -- ★ true: 끝 바이트가 잘린 값은 마지막 바이트를 버리고 복구
    s        RECORD;
    r        RECORD;
    v_hex    TEXT;
    v_fixed  TEXT;
    v_status TEXT;
BEGIN
    FOR s IN SELECT TBL, COL FROM KOR_SCAN ORDER BY TBL, COL LOOP
        FOR r IN EXECUTE format(
            'SELECT ctid::text AS rid, %I::text AS val FROM %I WHERE %I ~ ''[ﾀ-￿]''',
            s.COL, s.TBL, s.COL)
        LOOP
            BEGIN
                v_status := 'OK';

                -- 안전장치 1: 정상 한글 등 다른 문자가 섞인 값은 자동 복구하지 않음
                IF r.val ~ '[^\x01-\x7Fﾀ-￿]' THEN
                    RAISE EXCEPTION 'MIXED: 정상 문자와 깨진 문자가 섞여 있음';
                END IF;

                -- U+FFxx → 0xxx 바이트(hex 문자열)로 변환
                SELECT string_agg(
                         lpad(to_hex(CASE WHEN ascii(ch) >= 65280 THEN ascii(ch) - 65280
                                          ELSE ascii(ch) END), 2, '0'),
                         '' ORDER BY i)
                  INTO v_hex
                  FROM regexp_split_to_table(r.val, '') WITH ORDINALITY AS t(ch, i);

                -- CP949(UHC)로 디코딩. 끝이 잘린 값은 옵션에 따라 마지막 바이트를 버리고 재시도
                BEGIN
                    v_fixed := convert_from(decode(v_hex, 'hex'), 'UHC');
                EXCEPTION WHEN OTHERS THEN
                    IF NOT v_trim_tail THEN
                        RAISE;
                    END IF;
                    v_fixed  := convert_from(decode(left(v_hex, length(v_hex) - 2), 'hex'), 'UHC');
                    v_status := 'TRIMMED';
                END;

                -- 안전장치 2: 복구 결과에 한글이 하나도 없으면 오탐으로 보고 수정하지 않음
                IF v_fixed !~ '[가-힣]' THEN
                    v_status := 'CHECK';
                END IF;

                IF NOT v_dry AND v_status IN ('OK', 'TRIMMED') THEN
                    -- 안전장치 3: ctid + 원래 값이 둘 다 일치할 때만 UPDATE
                    EXECUTE format('UPDATE %I SET %I = $1 WHERE ctid = $2::tid AND %I::text = $3',
                                   s.TBL, s.COL, s.COL)
                    USING v_fixed, r.rid, r.val;
                END IF;

                INSERT INTO KOR_FIX_LOG VALUES (s.TBL, r.rid, s.COL, r.val, v_fixed, v_status, NULL);
            EXCEPTION WHEN OTHERS THEN
                INSERT INTO KOR_FIX_LOG VALUES (s.TBL, r.rid, s.COL, r.val, NULL, 'FAIL', SQLERRM);
            END;
        END LOOP;
    END LOOP;
END $$;
```

### STEP 4. 결과 확인 → COMMIT / ROLLBACK

```sql
-- 테이블/컬럼별 집계
SELECT TBL, COL,
       count(*)                                  AS 전체,
       count(*) FILTER (WHERE STATUS = 'OK')      AS 성공,
       count(*) FILTER (WHERE STATUS = 'TRIMMED') AS 끝바이트제거,
       count(*) FILTER (WHERE STATUS = 'CHECK')   AS 확인필요,
       count(*) FILTER (WHERE STATUS = 'FAIL')    AS 실패
FROM KOR_FIX_LOG
GROUP BY TBL, COL
ORDER BY TBL, COL;

-- 복구 샘플 (눈으로 확인)
SELECT TBL, COL, BEFORE_VAL, AFTER_VAL FROM KOR_FIX_LOG WHERE STATUS = 'OK' LIMIT 100;

-- 수정되지 않은 행 (STATUS = TRIMMED / CHECK / FAIL)
SELECT * FROM KOR_FIX_LOG WHERE STATUS <> 'OK' ORDER BY STATUS, TBL, COL;
```

| 상황 | 할 일 |
|---|---|
| 미리보기(`v_dry = true`)이고 결과가 정상 | `ROLLBACK;`을 실행한다. `v_dry := false`로 바꿔 STEP 3을 다시 실행한 뒤 STEP 4로 확인하고 `COMMIT;`을 실행한다. |
| 실제 적용(`v_dry = false`)이고 결과가 정상 | `COMMIT;` |
| 결과가 이상함 | `ROLLBACK;` |

### STEP 5. 잔여 확인 (COMMIT 후)

STEP 1을 다시 실행한다. 남은 건이 `KOR_FIX_LOG`의 `TRIMMED`/`CHECK`/`FAIL` 건수와 같으면 정상이다.

---

## 4. STATUS별 처리 가이드

| STATUS | 의미 | 처리 |
|---|---|---|
| `OK` | 정상 복구 | 없음 |
| `FAIL` + `invalid byte sequence` | 원본을 저장할 때 컬럼 길이 때문에 한글 마지막 바이트가 잘림 | `v_trim_tail := true`로 다시 실행한다. 마지막 글자 1자가 빠진 채 복구된다. |
| `FAIL` + `MIXED` | 정상 한글과 깨진 문자가 한 값에 섞여 있음. 이관 뒤 일부만 수정한 경우 등 | 해당 행을 화면에서 직접 수정하거나 개별 UPDATE로 처리한다. |
| `CHECK` | 복구 결과에 한글이 없음. 원래 전각 기호(`￦` 등)였을 가능성이 있음 | `BEFORE_VAL`/`AFTER_VAL`을 보고 판단해서 개별 처리한다. |

---

## 5. 지금까지 확인된 영향 테이블 (참고)

STEP 1이 자동으로 찾으므로 이 목록을 따로 관리할 필요는 없다. 화면별로 어떤 테이블이 관련되는지
파악하는 용도로만 쓴다.

| 화면 | 테이블.컬럼 |
|---|---|
| 지점(매장) 관리 | `tb_centerinfo.center_nm` 외 |
| 조직/권한 그룹 | `lettnauthorgroupinfo.group_nm`, `lettnauthorinfo.*` |
| 문화센터 강의 관리 | `tb_mhsclassinfo.mhs_classnm`, `mhs_teachernm`, `mhs_classintro` |
| 문화센터 모니터(강의실) | `tb_mhsmonitorinfo.mhs_monitornm`, `mhs_remark` |
| 문화센터 화면 관리 | `tb_contentmutil.con_nm` |
| 공통코드(편성형태, 송출정보 등) | `lettccmmndetailcode` / `comtccmmndetailcode` 의 `code_nm`, `code_dc` |
| 기타 (2026-07-26 EMTINDB 스캔 기준) | `lettnfiledetail`, `tb_brodanniversary`, `tb_brodschedule`, `tb_didinfo`, `tb_group`, `tb_schedule`, `tb_sendmessagetypr` 등 24개 테이블 |

## 6. 실행 이력

| 일자 | DB | 실행자 | 결과 |
|---|---|---|---|
| 2026-07-26 | 로컬 `EMTINDB` | - | `02_recover_update.sql`(ctid 고정본)로 복구 |
| 2026-11-17 | 서버 RDS `db_didinfo` | | (이 런북으로 실행 예정) |
| 2026-11-22 | 서버 RDS `db_didinfo` | Claude (사용자 승인) | 이 런북과 같은 복구 로직을 Java(JDBC)로 실행. 32개 컬럼 / **33,978셀 UPDATE 후 COMMIT**, 실패 0. 백업 테이블 없이(사용자 결정) 변경 전/후 값을 `rds_recover_20261122.tsv`에 기록. 이중 깨짐(CP949 → Latin-1 → U+FFxx, 예: `°­º¸°æ` → `강보경`)도 한 번 더 디코딩해 복구. 남은 건 `lettnfiledetail` 5행(U+FF61~FF7F 문자 — 원래 반각 문자일 가능성, 미처리). |

## 7. 재발 방지

이 런북은 이미 깨져서 들어간 데이터를 **사후에 복구**하는 용도다. 다시 이관한다면 이관
스크립트(ETL)에서 바이트를 문자열로 바꿀 때 `new String(bytes, "MS949")`처럼 **문자셋을 명시해서
디코딩**하도록 고쳐야 같은 문제가 반복되지 않는다.
