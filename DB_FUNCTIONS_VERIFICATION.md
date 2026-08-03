# DID_EMART DB 함수/프로시저 생성 확인 및 코드 반영 현황

작성일: 2026-07-27

## 1. 배경

마이그레이션 작업(9~17절, `db-encoding-fix/LOGIN_MANAGER_FEATURE.md` 참고) 중, 레거시
Oracle 코드가 호출하던 다수의 DB 함수/프로시저(`FN_BASICCODE`, `FN_CENTERID`,
`FN_DETAILCODENM` 등)가 PostgreSQL(`EMTINDB`)에 존재하지 않는 것을 여러 차례 확인하고,
그때마다 애플리케이션 레벨(Java, MAX+1 또는 서브쿼리)로 대체 구현했었다.

**2026-07-27 사용자가 해당 함수/프로시저 20개+3개를 DB에 직접 생성**했고, 이 문서는
(1) 생성 전 "없음" 확인 기록, (2) 생성 후 실제 확인 결과, (3) 이에 따라 되돌린 코드 변경
내역을 정리한다.

## 2. 생성 전 상태 (참고용 — 세션 초반 검증 결과)

과거 검증 스크립트(`scratchpad/check_*.js`)로 아래 함수들이 없음을 확인했었음:

```
FN_DETAILCODENM        => NOT FOUND
FN_DETAILCODEDOC       => NOT FOUND
FN_PAGEINTERVALDETAIL  => NOT FOUND
FN_SEQMAX              => NOT FOUND
FN_DIDMESSAGEINFO      => NOT FOUND
EMARTCMS.FN_GROUPID    => NOT FOUND
```

(그 외에도 `FN_BASICCODE`, `FN_CENTERID`, `FN_CENTERANNICODE`, `FN_CENTERBRODINFO`,
`FN_MHSMONITERID`, `mhsconn_seq` 등 다수를 도메인별 작업 중 개별 확인했음 — 각 절 참고)

## 3. 생성 후 확인 결과 (2026-07-27, 라이브 DB 재조회)

`pg_proc`/`pg_get_functiondef()` 직접 조회로 아래 20개 함수 + 3개 프로시저가 모두 실제
생성되어 있음을 확인함(사용자 스크린샷과 일치):

| 이름 | 인자 | 반환 | 실제 동작 요약 |
|---|---|---|---|
| `fn_basiccode()` | - | char | 당일(`frst_regist_pnttm`) 등록건수 COUNT+1 기반 `BC+yyMMdd+2자리` |
| `fn_centerid()` | - | char | 당일(`center_regdate`) 등록건수 COUNT+1 기반 `C+yyMMdd+2~3자리` |
| `fn_centerannicode(centerid, startday)` | bpchar, bpchar | char | `centerid+startday+건수+1` (단, WHERE절이 `'C16102601'`로 하드코딩되어 있어 실제로는 인자를 안 씀 — 버그로 추정, 4-4절 참고) |
| `fn_centerbrodinfo(centerid, date)` | bpchar, bpchar | varchar | 매장 기념일/정규 편성 시간대+방송코드 조회 |
| `fn_groupcode()` | - | char | `lettnauthorgroupinfo` 기준 MAX+1, `EMART_`+13자리 |
| `fn_detailcodenm(code)` | varchar | varchar | `LETTCCMMNDETAILCODE.CODE_NM` 조회 |
| `fn_detailcodedoc(code)` | varchar | varchar | `LETTCCMMNDETAILCODE.CODE_DC` 조회 |
| `fn_didid(centerid)` | varchar | varchar | 매장별 DID_ID MAX+1 |
| `fn_didmessageinfo(didid)` | varchar | char | `didid+현재시각(yyMMddHHmmss)` (SEND_DIDID용) |
| `fn_mhsmoniterid(centerid)` | varchar | varchar | 매장별 MHS 모니터코드 MAX+1 |
| `fn_mhscenterid()` | - | char | 당일 등록건수 COUNT+1 기반 `M+yyMMdd+2~3자리` |
| `fn_pageinterval(con_seq)` | varchar | varchar | `TB_CONDETAIL.TIME_INTERVAL`을 콤마로 이어붙임(STRING_AGG) |
| `fn_pageintervaldetail(con_seq)` | varchar | varchar | 첫 페이지의 `TIME_INTERVAL` 1건만 반환 |
| `fn_schcode()` | - | varchar | 당일 `S+yyMMdd`+MAX+1(3자리) |
| `fn_seqmax(table, column)` | varchar, varchar | varchar | **동적 SQL**(`EXECUTE 'SELECT MAX(...) FROM ...'`) — 단일 테이블 MAX만 반환(MAX+1 아님) |
| `fn_uniresult(resultColumn, table, column, value)` | varchar×4 | varchar | **동적 SQL**(`EXECUTE`) — value만 `quote_literal` 처리, table/column명은 문자열 결합 |
| `fn_contentnm(conseq)` | varchar | varchar | `TB_CONTENT`/`TB_CONTENTMUTIL` UNION으로 콘텐츠명 조회 |
| `fn_dayconvert(date)` | varchar | varchar | `YYYYMMDD` → `YYYY-MM-DD` 변환 |
| `fn_brodcodesp()` | - | char | `BROD_`+**9000000000 이상** 코드만 대상 MAX+1(일반 채번과 다른 별도 규칙, 복사/임시용 추정) |
| `fu_rolecode(authorcode)` | varchar | varchar | `authorcode+건수+1000` |
| `sp_basicupdate()` | - | - | 기초방송 만료 파일 정리 + 스케줄 재생성 배치 |
| `sp_didsttus()` | - | - | DID 15분 무응답시 OFF 처리 배치 |
| `sp_tableinsert(value)` | varchar | - | CSV 파싱 후 `tb_split` upsert (범용/테스트용으로 추정) |

### ⚠️ 주의가 필요한 부분

1. **`fn_uniresult`/`fn_seqmax`는 동적 SQL(문자열 결합) 구조** — table/column 인자를
   사용자 입력에서 직접 받지 않고 **자바 코드에 하드코딩된 리터럴로만** 넘기면 안전함.
2. **`fn_centerannicode`가 버그로 보임** — `WHERE center_id = 'C16102601'`이 하드코딩되어
   있어 실제 인자(`in_centerid`)를 쓰지 않음. 그대로 쓰면 항상 특정 매장 기준으로만 카운트됨.
   **사용자 확인/DB측 수정 필요** — 이번 코드 반영에서는 일단 그대로 호출하되 이 사실을 알고
   있어야 함.
3. **`fn_groupcode()`는 `lettnauthorgroupinfo`(부서 조직도) 기준** — MHS "그룹"(뷰그룹) 채번에
   써도 되는지는 대상 테이블이 다를 수 있어 별도 확인 필요(4-5절 참고).
4. **`fn_brodcodesp()`는 일반 BROD_CODE 채번과 다른 별도 규칙**(9000000000 이상 범위) —
   기존에 만든 `generateBrodCode()`(MAX+1, 전체 범위)와 용도가 다를 수 있어 이번엔 그대로
   두고 반영 대상에서 제외함.

## 4. 코드 반영 내역 (2026-07-27, 사용자 확인 후 일괄 진행)

| 도메인 | 변경 파일 | 변경 내용 |
|---|---|---|
| sts/brd (기초방송) | `BasicBrodInfoManageService.java`, `BasicBrodManagerMapper.xml`, `BasicBrodManageController.java` | `generateBasicCode()`(MAX+1) 제거 → 기존에 있던 `selectBasicCode()`(`SELECT FN_BASICCODE()`) 재사용 |
| sts/brd (방송콘텐츠) | `BrodContentInfoManagerMapper.xml` | `SEC_GUBUN` 서브쿼리 → `FN_DETAILCODENM(A.SEC_GUBUN)` |
| sts/cnt (콘텐츠) | `ContentInfoManagerMapper.xml` | `CON_PLAYTYPE` 서브쿼리 → `FN_DETAILCODENM(...)` |
| sts/cnt (멀티페이지 콘텐츠) | `ContentMutiInfoManagerMapper.xml` | `CON_SCREEN`/`CON_TYPE`/`CON_PLAYTYPE`/`CON_URLTYPE` 서브쿼리 → `FN_DETAILCODENM(...)`, `CON_SCREEN` 설명 → `FN_DETAILCODEDOC(...)`. `selectMaxSeqInfo`(다음 CON_SEQ 미리보기)는 `FN_SEQMAX`가 단일테이블 MAX만 지원(MAX+1 아님, TB_CONTENT/TB_CONTENTMUTIL 두 테이블 겸용 불가)이라 기존 GREATEST 로직 유지 |
| sts/cnt (DID 메시지 발송) | `ContentMessageInfoManagerMapper.java/.xml`, `ContentMessageInfoManageService.java` | `generateSendDidId()`가 자바에서 직접 계산하던 것 → `selectSendDidId()`(`SELECT FN_DIDMESSAGEINFO(#{didId})`) 신규 추가해서 호출. `SEND_MSGID`는 대응 함수가 없어 기존 MAX+1(`generateSendMsgId`) 유지 |

## 5. 반영 결과 (2026-07-27 완료)

- ✅ `bas/cnt`: `generateCenterId()` → `selectCenterId()`(`FN_CENTERID()`)
- ✅ `bas/cnt`: `selectCenterTimeInfo()` → `GET /{centerId}/timeInfo.do`로 신규 노출
  (서비스/매퍼는 이미 `FN_CENTERBRODINFO` 연결되어 있었음)
- ⏸️ `bas/cnt`: `CenterAnniManagerService` 채번은 **전환하지 않음** — `fn_centerannicode()`
  정의를 열어보니 `WHERE center_id = 'C16102601'`처럼 매개변수 대신 특정 매장코드가
  하드코딩되어 있어(버그로 추정) 그대로 쓰면 다른 매장 기념일 코드가 잘못 계산됨.
  DB측 수정 필요(사용자 전달 필요), 그 전까지 기존 애플리케이션 레벨 채번 유지
- ✅ `sts/mhs`: `MhsMonitorInfoManageService` 채번 → `selectMhsMonitorId()`(`FN_MHSMONITERID()`)
- ✅ `use`: `GroupManagerService` 채번 → `selectGroupCode()`(`FN_GROUPCODE()`) — 대상 테이블
  (`LETTNAUTHORGROUPINFO`) 일치 확인 완료
- ✅ `sts/brd`/`sts/cnt`: `FN_DETAILCODENM`/`FN_DETAILCODEDOC` 서브쿼리 전부 실제 함수 호출로 전환
- ✅ `sts/cnt`: SEND_DIDID 채번 → `FN_DIDMESSAGEINFO()`
- ⏸️ `sts/cnt`: `ContentMutiInfoManagerMapper.selectMaxSeqInfo`는 **전환하지 않음** —
  `fn_seqmax(table, column)`이 단일 테이블 MAX만 지원(MAX+1 아님)해서 TB_CONTENT/
  TB_CONTENTMUTIL 두 테이블을 함께 봐야 하는 이 쿼리엔 부적합
- ⏸️ **`sts/brd`: 17절에서 제외했던 "편성표 생성/조회/엑셀" 클러스터는 복원하지 못함** —
  해당 컨트롤러를 전면 재작성(덮어쓰기)할 때 원본 ~410줄이 이미 사라졌고, 이 프로젝트가
  git 저장소가 아니라 복구할 히스토리도 없음. 사용자 확인 후 "레거시 소스/화면을 나중에
  제공하면 그거 보고 포팅"하기로 결정 — 자료 받는 대로 재개
- ⏸️ `fn_brodcodesp()`: 일반 BROD_CODE 채번과 다른 별도 범위 규칙(9000000000 이상)으로
  보여 정확한 용도가 불명확 — 이번엔 반영하지 않음
- ⏸️ `fn_mhscenterid`/`fn_schcode`/`fn_contentnm`/`fn_dayconvert`/`fu_rolecode`: 각각
  `sym/sch`/`sym/did`/`bas/role` 컨트롤러가 사용하는데, 이 컨트롤러들은 이번 마이그레이션
  범위 밖(아직 리팩토링 전)이라 손대지 않음
- ⏸️ `sp_basicupdate`/`sp_didsttus`/`sp_tableinsert` 프로시저는 스케줄러 연동 요청을 받지
  않아 손대지 않음

### 검증
- ✅ `./gradlew compileJava` — 오늘 수정한 파일 전부 에러 없음
- ✅ 프론트 `npm run build` 성공
- 상세 변경 파일 목록은 `db-encoding-fix/LOGIN_MANAGER_FEATURE.md` 18절 참고
