# DID EMART CMS — Oracle → PostgreSQL 이관 한글 인코딩 깨짐 분석

- 작성일: 2026-07-26
- 대상 DB: PostgreSQL `27.96.130.69:8088/EMTINDB` (신규, 이관 완료본)
- 원본 DB: Oracle `10.253.32.23:1521:EMTINDB` (레거시 `emart_cms3.2.1`)

## 1. 증상

이관된 PostgreSQL DB의 한글 데이터가 아래처럼 깨져서 보임.

```
￀ ￀ￚ￁ﾤﾺￎ ￇ￁ﾷﾹ￀ￓ﾿￶ￅﾩ ﾰ￦ﾷﾮ￈ﾯﾰ￦ ￅￛￇￃﾸﾴ
```

영문/숫자/공백은 멀쩡하고 한글만 깨짐 (예: `"DID 형태"` → `"DID ￇ￼ￅￂ"`).

## 2. 원인

**Java의 `(char) byteValue` 부호 확장(sign-extension) 버그**로 확인됨. 이관 스크립트(또는 중간 ETL 단계)에서
Oracle에 CP949(MS949/EUC-KR)로 저장돼 있던 원본 바이트를 `new String(bytes, "MS949")`처럼 캐릭터셋을
지정해 변환하지 않고, 바이트를 하나씩 `char`로 직접 캐스팅한 것으로 보임.

- Java `byte`는 부호 있는 타입(-128~127).
- `0x80` 이상 바이트(음수로 취급)를 `char`로 캐스팅하면 부호 확장이 일어나 `0xFF00 | byte` 값이 됨.
- `0x7F` 이하 ASCII 바이트는 그대로 보존됨 → 그래서 영문/공백은 안 깨지고 한글만 깨져 보임.

이 변환은 **1바이트 ↔ 1코드포인트 완전 가역적**이라 데이터 유실 없이 100% 복구 가능함이 실측으로 확인됨.

### 복구 로직

```
recovered = decode( bytes( map(char => char.codePointAt(0) & 0xFF) ), 'CP949' )
```

즉 깨진 문자열의 각 유니코드 문자에서 하위 1바이트(`& 0xFF`)만 뽑아 바이트 배열을 만들고,
그 바이트 배열을 CP949(MS949)로 디코딩하면 원본 한글이 그대로 나옴.

Node.js 기준 예시(iconv-lite):
```js
const bytes = iconv.encode(corrupted, 'binary'); // codepoint & 0xFF
const recovered = iconv.decode(bytes, 'cp949');
```

## 3. 실측 검증 (PostgreSQL 실 데이터 샘플)

| 테이블.컬럼 | 깨진 값 | 복구된 값 |
|---|---|---|
| lettccmmnclcode.cl_code_nm | `￀ￌﾸﾶￆﾮ` | 이마트 |
| lettccmmnclcode.cl_code_dc | `￀ￌﾸﾶￆﾮ ﾻ￳ﾼﾼ ￄￚﾵ￥` | 이마트 상세 코드 |
| lettccmmnclcode.cl_code_nm | `￀￼￀ￚ￁ﾤﾺￎ ￇ￁ﾷﾹ￀ￓ﾿￶ￅﾩ ﾰ￦ﾷﾮ￈ﾯﾰ￦ ￅￛￇￃﾸﾴ` | 전자정부 프레임워크 경량환경 템플릿 |
| lettccmmncode.code_id_nm | `ￆﾯ￁ﾤﾹ￦ﾼￛ﾿ﾩﾺￎ` | 특정방송여부 |
| lettccmmncode.code_id_dc | `DID ￇ￼ￅￂ` | DID 형태 |
| lettccmmndetailcode.code_nm | `ￅ￘ﾽﾺￆﾮ` | 텍스트 |
| lettccmmndetailcode.code_dc | `ﾴￜﾸﾻﾱ￢￀ￎ￁￵` | 단말기인증 |
| lettnauthorgroupinfo.group_nm | `ﾶ￳￀ￌￇ￁ￄ￁ￅￗ￀ￌﾳￊ` | 라이프컨테이너 |

## 4. 영향 범위 (전체 스캔 결과)

`information_schema`의 `character varying / text / character` 타입 컬럼 **481개**(54개 테이블) 전수 조사.
탐지 정규식: `[｡-￾]` (부호 확장 버그로 생기는 문자 범위).

**24개 테이블 / 23,515행 / 36,332개 셀** 영향 확인.

| 테이블 | 영향 행 | 영향 셀 |
|---|---:|---:|
| lettccmmnclcode | 4 | 8 |
| lettccmmncode | 24 | 34 |
| lettccmmndetailcode | 58 | 73 |
| lettnauthorgroupinfo | 31 | 56 |
| lettnauthorinfo | 9 | 18 |
| lettnfiledetail | 3,942 | 6,895 |
| lettngnrlmber | 56 | 56 |
| tb_authorrolerelate | 7 | 7 |
| tb_basicfilegroup | 1 | 1 |
| tb_brodanniversary | 7,830 | 7,830 |
| tb_brodbasicgroup | 2 | 2 |
| tb_brodschedule | 281 | 281 |
| tb_centerinfo | 453 | 471 |
| tb_contentmutil | 380 | 380 |
| tb_didinfo | 664 | 664 |
| tb_didsendmessage | 1 | 1 |
| tb_group | 369 | 369 |
| tb_groupdid | 1 | 1 |
| tb_menu | 1 | 1 |
| tb_mhscenterinfo | 6 | 6 |
| tb_mhsclassinfo | 8,865 | 18,594 |
| tb_mhsmonitorinfo | 91 | 106 |
| tb_schedule | 396 | 396 |
| tb_sendmessagetypr | 43 | 82 |

나머지 30개 테이블은 영향 없음(해당 텍스트 컬럼에 깨짐 패턴 없음).

## 5. 특이사항 — PK 없음

스캔 중 확인된 사실: 이관된 PostgreSQL DB **54개 테이블 전체에 PRIMARY KEY 제약조건이 없음**
(데이터만 복사되고 제약조건/인덱스는 이관되지 않은 것으로 보임). 이 때문에 복구 UPDATE 스크립트는
PK 대신 PostgreSQL의 물리적 행 식별자 `ctid`를 사용했고, 안전장치로 `WHERE ctid = ... AND 컬럼 = '깨진값'`
형태로 이중 체크함. `ctid`는 해당 행에 대한 동시 UPDATE/VACUUM FULL이 발생하면 바뀔 수 있으므로,
**스크립트는 생성 직후 가급적 빨리, 트래픽이 적은 시간대에 실행**할 것을 권장함.

> ⚠️ 별개 이슈: PK가 전혀 없는 상태는 이관 자체의 완성도 문제이므로, 인코딩 복구와는 별도로
> 원본 Oracle 스키마 기준 PK/인덱스 재설계가 필요함. (이번 작업 범위 밖 — 별도 논의 필요)

## 6. 산출물

이 폴더(`db-encoding-fix/`)에 아래 3개 파일 생성:

1. **`01_backup_tables.sql`** — 영향받는 24개 테이블을 `{table}_bak_20260726` 이름으로 통째로 백업.
2. **`02_recover_update.sql`** — 36,332개 셀에 대한 복구 UPDATE 문. 실행 전 반드시 1번 먼저 실행.
3. **`recover_report.txt`** — 테이블별 영향 행/셀 수 원본 로그.

### 실행 순서 (사용자 직접 실행 권장)

```sql
-- 1) 백업
\i 01_backup_tables.sql

-- 2) 복구 (트랜잭션으로 묶어서 실행 권장)
BEGIN;
\i 02_recover_update.sql
-- 결과 확인 후
COMMIT;   -- 문제 있으면 ROLLBACK;
```

실행 후 각 테이블에서 아래 패턴으로 잔여 깨짐 여부 확인 가능:
```sql
SELECT count(*) FROM tb_centerinfo WHERE some_col ~ '[｡-￾]';
```

## 7. 재발 방지 (마이그레이션 스크립트/파이프라인 쪽)

이번 이관이 일회성이 아니라 재실행될 가능성이 있다면, Oracle → PostgreSQL 이관 코드에서
바이트를 문자열로 변환하는 지점을 찾아 **명시적으로 `MS949`(또는 원본 Oracle의 `NLS_CHARACTERSET`과
동일한 캐릭터셋)를 지정해서 디코딩**하도록 고쳐야 근본적으로 해결됨. 지금 산출된 SQL은 이미 깨져서
들어간 기존 데이터에 대한 **사후 복구**일 뿐, 이관 스크립트 자체를 고치지 않으면 다음 이관 때 동일한
문제가 재발함.

## 8. 미해결 / 후속 확인 필요

- PostgreSQL 접속 계정(`root` / 최초 시도한 `didadmin`) 중 `didadmin` 비밀번호가 yml과 불일치했음 —
  운영 계정 정리 필요 여부 확인 권장.
- 이번 스캔은 `character varying / text / character` 타입 컬럼만 대상으로 함. `json`/`jsonb`/`xml`
  타입 컬럼 내부에 한글이 있다면 별도 확인 필요(이번 스캔 대상 아님).
- PK 부재 이슈(5절)는 인코딩과 별개로 후속 조치 필요.

## 9. ⚠️ 라이브 동시 수정 감지

작업 도중 같은 행을 다른 시점에 두 번 조회했는데 값이 달라져 있는 것을 확인함.

- `lettnauthorinfo.author_nm` (ROLE_ADMIN 행): 최초 스캔 시 깨진 값이었는데, 이후 재조회 시 이미
  `"통합관리자"`로 정상 값이 들어가 있었음.
- `lettccmmnclcode.cl_code_nm`: 최초 스캔 시 깨진 값이었는데, 재조회 시 이미 `"이마트"`로 정상.

즉 이 PostgreSQL DB(`EMTINDB`)는 **정적 스냅샷이 아니라 지금도 다른 프로세스/사용자가 계속 쓰고 있는
라이브 DB**로 보임. `02_recover_update.sql`은 `ctid` 기반이라 시간이 지날수록 매칭이 어긋날 수 있으므로
(안전장치로 `AND 컬럼 = '깨진값'`을 같이 걸어놔서 엉뚱한 행을 덮어쓸 위험은 없음 — 최악의 경우 매칭 안 되고
그냥 넘어감), **실행을 미루지 말고 트래픽이 적은 시간에 가능한 빨리 실행**할 것을 권장함.

## 10. 공통코드 3테이블 rename 진행 (lettccmmn* → COMTCCMMN*)

egov 3.x `lettccmmnclcode` / `lettccmmncode` / `lettccmmndetailcode` → egov 5.x
`COMTCCMMNCLCODE` / `COMTCCMMNCODE` / `COMTCCMMNDETAILCODE`로 이관. did_emart 백엔드에는 이미
이 이름을 기대하는 MyBatis 매퍼(`mapper/postgresql/bas/code/**`)와 JPA 엔티티
(`Comtccmmncode`, `Comtccmmndetailcode`)가 스캐폴딩되어 있었음.

**확정된 순서: 한글 복구(`02_recover_update.sql`) → rename/컬럼추가/PK(`03_rename_and_alter_common_code.sql`) → 매퍼 XML 버그 수정(완료)**

### 컬럼 비교
CLCODE(8개)·CODE(9개)는 이름이 이미 100% 동일. DETAILCODE는 `CODE_ETC1`, `CODE_ETC2` 2개 컬럼이
원본에 없어서 추가 필요(`03_rename_and_alter_common_code.sql`에 포함).

### 발견된 이슈와 처리
- **`cl_code` 완전 중복행**: `EMT`, `LET` 각각 내용이 100% 동일한 행이 2개씩 있어 PK를 바로 못 걺 →
  스크립트에 중복 제거 DELETE 포함.
- **`cl_code` 컬럼 폭 불일치**: JPA 엔티티는 `CHAR(7)`을 기대하는데 실제 컬럼/데이터는 `CHAR(3)` →
  스크립트에서 `CHAR(7)`로 넓힘(값 손실 없음).
- **PK 전무**: `code_id`, `code`는 중복 없어 바로 PK 추가 가능. cl_code는 중복 제거 후 추가.
- **`FN_DETAIL_CODEID(codeId)` DB 함수 없음** — 상세코드 자동채번용 함수를 매퍼가 호출하는데 DB에
  없음. 아직 생성 보류함. 실제 CODE 값 포맷이 일관되지 않아(`STATE_01`/`02`처럼 언더스코어+2자리도
  있고, `DIDTYPE01`/`REGC07`/`USR03`/`DIDRES00`처럼 언더스코어 없이 바로 숫자 붙는 것도 있음)
  채번 규칙을 사용자에게 확인받은 뒤 별도 스크립트로 생성 예정.
- **매퍼 XML 버그 수정 완료**:
  - `mapper/postgresql/bas/code/cca/EgovCmmnCodeManageMapper.xml`: 41~42번째 줄에 닫히지 않은
    `<select id="">` 잔재 태그가 있어 MyBatis 로드 시 파싱 에러가 날 상태였음 → 삭제.
  - `mapper/postgresql/bas/code/ccc/EgovCmmnClCodeManageMapper.xml`: 검색조건이 존재하지 않는
    컬럼 `a.USE_YN`을 참조하고 있었음(실제 컬럼은 `USE_AT`, basic/backend 원본 MySQL 매퍼의
    기존 버그가 그대로 복사됨) → `USE_AT`으로 수정.

### 산출물
- `03_rename_and_alter_common_code.sql` — 중복제거 + 컬럼추가 + rename + PK + 컬럼폭 조정
  (트랜잭션으로 묶여 있음, 사용자 직접 실행)

## 11. `lettnauthorinfo` → `TB_ROLEINFO` 검토 결과: **비추천**

`lettnauthorinfo`(권한군 마스터, 9행: `author_code`/`author_nm`/`author_dc`/`author_creat_de`)를
`TB_ROLEINFO`로 rename하려는 계획을 검토한 결과, **이름만 비슷할 뿐 실제로는 서로 다른 목적의
테이블이라 rename하면 안 됨**을 확인함.

### 근거
1. **`TB_ROLEINFO`는 이미 다른 용도로 스캐폴딩되어 있음.** `bas.role` 모듈
   (`RoleInfoManageMapper.xml`, `RoleInfoManageController`, `RoleInfo.java`)이 이미
   `TB_ROLEINFO`를 대상으로 CRUD를 구현해놓았는데, 컬럼 구성이 전혀 다름:
   `ROLE_ID`, `ROLE_NAME`, `ROLE_DC`, `ROLE_USEYN`, `ROLE_USER_GUBUN`, `SYSTEM_CODE` 등 —
   메뉴별 상세 권한(RBAC)을 관리하는 신규 테이블이며, `FN_ROLE_CNT()`/`fn_DetailCodeNm()` DB 함수와
   `COMTNMENUCREATDTLS`(메뉴-역할 매핑) 테이블까지 연계되어 있음. 이 테이블은 아직 DB에
   생성되지 않은 상태(`lettnauthorinfo`와 무관하게 새로 만들어야 함).
2. **`lettnauthorinfo`는 이미 원래 이름 그대로 다른 모듈에 연결되어 있음.** `sym.rnt` 모듈
   (`AuthorInfoManagerMapper.xml`)이 `SELECT AUTHOR_CODE, AUTHOR_NM FROM LETTNAUTHORINFO`로
   **테이블명을 하드코딩**해서 이미 참조 중. rename하면 이 쿼리가 바로 깨짐.
3. 데이터 성격도 다름: `lettnauthorinfo`는 `ROLE_ADMIN`/`ROLE_ANONYMOUS`/`ROLE_DID_ADMIN` 등
   9개의 큰 권한군 코드일 뿐이고, 실제 메뉴 단위 세부 권한은 `tb_authorrolerelate`
   (`author_code` → `role_code`/`role_nm` 매핑, 예: `ROLE_ADMIN1000`)가 담당하고 있음.
   `TB_ROLEINFO`가 요구하는 `ROLE_USER_GUBUN`(사용자구분) 같은 속성은 `lettnauthorinfo`에
   아예 없어서, rename만으로는 만들어지지 않음.

### 참고: 네이밍 규칙 상 문제
CLAUDE.md DB 네이밍 규칙(단어는 언더스코어로 구분)대로라면 `TB_ROLEINFO`보다
`TB_ROLE_INFO`가 맞는 표기임. 다만 이미 매퍼/엔티티 코드 전체가 `TB_ROLEINFO`(언더스코어 없이)로
하드코딩되어 있어서, 지금 와서 `TB_ROLE_INFO`로 바꾸려면 코드도 같이 고쳐야 함 — 이미 존재하는
컨벤션이니 실익이 크지 않다면 `TB_ROLEINFO` 그대로 두는 것을 권장.

### 제안
- `lettnauthorinfo`는 이름 그대로 두거나, 정 바꾸고 싶다면 `TB_ROLEINFO`와 겹치지 않는 이름
  (예: `TB_AUTHORGROUP` 계열)으로 rename하고 `AuthorInfoManagerMapper.xml`의 하드코딩된
  테이블명도 함께 수정.
- `TB_ROLEINFO`(메뉴별 세부 권한)는 `lettnauthorinfo`와 별개로 **신규 생성**이 필요함
  (별도 작업으로 진행 권장).

## 12. 사용자/권한/메뉴 체계 마이그레이션 — 소스 테이블 재확인

당초 `TB_MANAGERINFO`는 `lettnemplyrinfo`(직원정보)에서 이관 예정이었으나, 실제 확인 결과
`lettnemplyrinfo`는 **0건**이라 이관할 데이터가 없었음. 대신 `lettngnrlmber`("일반회원"이라는
이름이지만 실제로는 DID CMS 통합 로그인 계정 테이블, 58건)가 진짜 소스임을 실데이터로 검증함.

### 검증된 사실
- `lettngnrlmber.author_code` 전량이 `lettnauthorinfo.author_code`(9개 권한군)와 100% 일치
- `lettngnrlmber.role_code`는 전량 NULL — `tb_authorrolerelate`(author_code→매장그룹 role_code)
  방식은 실제 계정에는 전혀 쓰이지 않는 죽은 데이터로 확인됨 (이관 제외)
- `lettngnrlmber.center_id`(36/58건 값 있음, 나머지는 전체관리자로 추정)가 실제 매장 스코프
  컬럼이었음 — `tb_centerinfo.center_id`와 대부분 일치(4건만 불일치, 공백/오타 추정)
- `lettngnrlmber.group_id` 전량이 `lettnauthorgroupinfo.group_id`(35건, 유효한 자기참조
  조직도 트리)와 100% 일치 → `lettnauthorgroupinfo`가 `TB_PARTINFO`(basic/backend 부서 트리)의
  소스임을 확인. 최상위 루트의 `parent_group_id='0'` 컨벤션이 `TB_PARTINFO`의
  `PARENT_PART_ID='0'` 컨벤션과 정확히 일치함(우연 아님)

### 진행 방식
기존 테이블(`lettnauthorgroupinfo`, `lettngnrlmber`)은 rename하지 않고 그대로 유지하고,
신규 테이블(`TB_PARTINFO`, `TB_MANAGERINFO`)을 만들어 데이터를 복사하는 방식으로 진행.
`TB_MANAGERINFO`는 사용자가 이미 테이블을 만들어둔 상태(0건, PK 없음, `CENTER_ID` 컬럼 없음)라
컬럼 추가 위주로 진행.

### 산출물
- `04_create_tb_partinfo.sql` — TB_PARTINFO 신규 생성 + `lettnauthorgroupinfo`(35건) 이관
  (PART_ID=group_id, PARENT_PART_ID=parent_group_id, mhsyn(문화센터여부)은 PART_ETC1에 보관)
- `05_alter_tb_managerinfo.sql` — `CENTER_ID` 컬럼 추가, PK(`MANAGER_ID`) 추가,
  `PART_ID` FK(→TB_PARTINFO) 추가, `lettngnrlmber`(58건) 이관

### 주의사항
- **선행 순서 필수**: `02_recover_update.sql`(한글 복구) → `04_create_tb_partinfo.sql` →
  `05_alter_tb_managerinfo.sql`(TB_PARTINFO가 먼저 있어야 PART_ID FK 생성 가능)
- `lettngnrlmber.password`가 **평문 저장**되어 있어 그대로 복사됨 — 신규 로그인(Spring Security)에
  붙이기 전 반드시 별도 배치로 재해시(bcrypt 등) 필요
- `CENTER_ID`는 4건 불일치 데이터가 있어 강한 FK는 걸지 않음(컬럼만 추가) — 정합성 정리 후 FK 추가 권장
- `TB_ROLEINFO`(`lettnauthorinfo`, 9건), `COMTNPROGRMLIST`/`COMTNMENUINFO`/`COMTNMENUCREATDTLS`(신규
  빈 테이블)는 이번 스크립트 범위 밖 — 별도 진행 예정

### 추가: TB_ROLEINFO 이관 스크립트
`tb_roleinfo`도 사용자가 이미 테이블을 만들어둔 상태(0건, PK 없음, 컬럼 구성은 예상과 100% 일치:
`role_id/role_name/role_dc/role_useyn/role_user_gubun/frst_regist_pnttm/frst_register_id/
last_updt_pnttm/last_updusr_id`)임을 확인함.

- `06_migrate_tb_roleinfo.sql` — PK(`ROLE_ID`) 추가 + `lettnauthorinfo`(9건) 이관
  (원본 보존). `ROLE_USER_GUBUN`은 대응 소스가 없어 NULL로 채움 — 분류 규칙 확정 후 별도 UPDATE 필요
- 실행 순서: `02_recover_update.sql` → `06_migrate_tb_roleinfo.sql` (TB_PARTINFO/TB_MANAGERINFO와는
  독립적이라 순서 무관하게 실행 가능)
