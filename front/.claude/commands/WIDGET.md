# 위젯 대시보드 (react-grid-layout)

사용자가 자신의 메인 화면에 위젯을 자유롭게 추가·배치(드래그/리사이즈)·삭제할 수 있는 기능과,
관리자가 그 위젯들의 "정의"(어떤 API에서 데이터를 가져와 어떤 형태로 보여줄지)를 등록/수정/삭제하는
화면 전체에 대한 문서. 다른 프로젝트에서도 그대로 재사용 가능하도록 `SKILL.md`와 별도로 관리한다.

기간별(일/주/월) 무거운 집계가 필요한 CHART 위젯은 이 문서만으로는 부족하다 — `MONGODB.md`(배치 집계 →
MongoDB 적재 패턴)를 함께 참고할 것.

---

## 1. 한눈에 보는 구조

```
[관리자] 위젯 정의 등록/수정/삭제  (WidgetMstInfo.jsx, /sub/bas/widgetMst)
    │
    │  TB_WIDGET_MST (마스터)
    │  ├─ TB_WIDGET_LIST_INFO  + TB_WIDGET_LIST_COL    (WIDGET_TYPE = LIST 일 때)
    │  ├─ TB_WIDGET_CHART_INFO + TB_WIDGET_CHART_SERIES (WIDGET_TYPE = CHART 일 때)
    │  └─ TB_WIDGET_COUNT_INFO                          (WIDGET_TYPE = COUNT 일 때)
    ▼
[사용자] 메인 화면(MainWidgetDashboard.jsx, /sub/dashboard/mainWidget)
    │  "편집" → "+ 위젯 추가" → TB_USER_WIDGET 에 배치(x,y,w,h,순서) 저장
    ▼
GET myWidgetListAjax.do
    │  TB_USER_WIDGET ⋈ TB_WIDGET_MST ⋈ (타입별 단일 설정) + 컬럼/시리즈 배열 병합
    ▼
react-grid-layout 렌더링 (react-grid-layout/legacy, v1 호환 API)
    │  각 위젯 카드(WidgetContainer)가 자기 API_URL로 개별 데이터 fetch
    │  → WIDGET_TYPE에 따라 ListWidget / ChartWidget(recharts) / CountWidget 분기 렌더
    ▼
편집모드에서 드래그/리사이즈 → "저장" 클릭 시 updateUserWidgetLayout.do 로 일괄 저장
```

핵심 설계 포인트:
- **레이아웃 조회와 위젯 데이터 조회를 분리.** `myWidgetListAjax.do`는 배치+설정만 한 번에 받고, 각 위젯의 실제 표시 데이터는 위젯 카드가 자기 `apiUrl`로 각자 비동기 로딩한다. 특정 위젯의 API가 느려도 전체 화면이 막히지 않는다.
- **위젯 타입이 바뀔 수 있다는 전제.** 관리자가 위젯 저장 시마다 LIST/CHART/COUNT 설정을 전부 지우고 현재 타입 것만 다시 넣는다. 이전 타입의 잔여 설정이 남지 않는다.
- **드래그/리사이즈는 편집모드에서만 상태 반영.** 실수로 배치가 바뀌는 것을 막고, "저장" 버튼을 눌러야 서버에 반영된다.
- **`apiUrl` 하나 = 파라미터 없는 단발 GET.** `WidgetContainer`는 `widget.apiUrl`을 마운트 시 딱 한 번 GET으로 호출한다. 기간(일/주/월)처럼 뷰가 바뀌는 데이터가 필요하면 `MONGODB.md`의 배치 집계 패턴을 검토할 것 — 위젯을 여러 개 등록하거나 API를 기간별로 쪼개는 것보다, 무거운 집계는 배치로 미리 계산해두고 읽기 전용 API로 노출하는 편이 낫다.

---

## 2. DB 스키마 (7개 테이블)

```sql
TB_WIDGET_MST
  WIDGET_ID              VARCHAR(50)   PK   -- 위젯 ID (예: WGT_NOTICE)
  MENU_NO                INT                -- 연결된 메뉴 번호 (선택)
  WIDGET_NM              VARCHAR(100)       -- 위젯 노출명
  WIDGET_ICON            VARCHAR(50)        -- 아이콘 (문자열/이모지)
  WIDGET_TYPE            VARCHAR(30)        -- LIST | CHART | COUNT
  API_URL                VARCHAR(255)       -- 데이터 조회용 GET API
  WIDGET_DEFAULT_WIDTH   INT   DEFAULT 1    -- 기본 그리드 너비
  WIDGET_DEFAULT_HEIGHT  INT   DEFAULT 1    -- 기본 그리드 높이
  WIDGET_USEYN           CHAR(1) DEFAULT 'Y'
  FRST_REGISTER_ID / FRST_REGIST_PNTTM / LAST_UPDUSR_ID / LAST_UPDT_PNTTM

TB_WIDGET_LIST_INFO      (WIDGET_ID PK, FK → MST, CASCADE)
  PAGE_USE_YN, PAGE_ROW_CNT, MORE_BTN_URL

TB_WIDGET_LIST_COL       (WIDGET_ID + COL_KEY 복합 PK, FK → MST, CASCADE)
  COL_NM, COL_WIDTH, COL_ALIGN, SORT_ORDR

TB_WIDGET_CHART_INFO     (WIDGET_ID PK, FK → MST, CASCADE)
  CHART_KND(BAR/LINE/PIE/DOUGHNUT), X_AXIS_KEY, X_AXIS_NM, LEGEND_USE_YN

TB_WIDGET_CHART_SERIES   (WIDGET_ID + SERIES_KEY 복합 PK, FK → MST, CASCADE)
  SERIES_NM, SERIES_COLOR, SORT_ORDR

TB_WIDGET_COUNT_INFO     (WIDGET_ID PK, FK → MST, CASCADE)
  DATA_KEY, UNIT_NM, FORMAT_STR, TREND_USE_YN, TREND_LBL

TB_USER_WIDGET           (USER_ID + WIDGET_ID 복합 PK, FK → MST, CASCADE)
  SORT_ORDER, POS_X, POS_Y, WIDTH, HEIGHT, FRST_REGIST_PNTTM, LAST_UPDT_PNTTM
```

> ⚠️ **`application.yml`의 `ddl-auto: validate`** 설정 때문에, 위 7개 테이블이 실제 DB에 미리 존재해야 Spring Boot가 기동된다. 새 프로젝트에 이식할 때 DDL을 먼저 실행할 것.
> 모든 하위 테이블이 `ON DELETE CASCADE`로 `TB_WIDGET_MST`를 참조하므로, 위젯 정의 하나를 지우면 딸린 설정과 사용자 배치까지 자동으로 함께 삭제된다.

---

## 3. 백엔드 파일 구조

```
com/common/backoffice/bas/wedget/
├── models/
│   ├── WedgetInfo.java              (TB_WIDGET_MST)
│   ├── WedgetListInfo.java          (TB_WIDGET_LIST_INFO)
│   ├── WedgetListCol.java + WedgetListColId.java       (TB_WIDGET_LIST_COL, 복합키)
│   ├── WedgetChartInfo.java         (TB_WIDGET_CHART_INFO)
│   ├── WedgetChartSeries.java + WedgetChartSeriesId.java (TB_WIDGET_CHART_SERIES, 복합키)
│   ├── WedgetCountInfo.java         (TB_WIDGET_COUNT_INFO)
│   ├── UserWedgetInfo.java + UserWedgetInfoId.java     (TB_USER_WIDGET, 복합키)
│   └── dto/
│       ├── WedgetInfoReqDto.java     (위젯 정의 등록/수정 — LIST/CHART/COUNT 필드 전부 포함, 타입에 맞는 것만 채워 보냄)
│       └── UserWedgetInfoReqDto.java (사용자 배치 저장용 — userId, widgetId, sortOrder, posX, posY, width, height)
├── mapper/
│   └── WedgetInfoManageMapper.java   (인터페이스, MyBatis @Mapper)
├── service/
│   └── WedgetInfoManageService.java
├── batch/
│   └── ReservationStatCollectJob.java  (MONGODB.md 참고 — CHART 배치 집계 파일럿)
└── web/
    └── WedgetInfoManageController.java

mapper/mysql/bas/wedget/WedgetInfoManageMapper.xml   (실제 SQL)
mapper/config/mysql-config.xml                        (WedgetInfoReqDto/UserWedgetInfoReqDto 타입 별칭 등록)
```

> **⚠️ MyBatis 방식으로 구현** — `@Entity` 클래스는 있지만 실제 CRUD는 전부 XML Mapper SQL로 처리한다 (JPA Repository 미사용). `BookMarksInfo`/`VenderContractInfo`와 동일한 컨벤션.

### 3-1. `WedgetInfoReqDto` 전체 필드

모든 숫자형 필드는 프론트에서 문자열로 보내도 안전하도록 **String 타입**으로 선언했다 (이 코드베이스 전반의 컨벤션 — JSON 문자열 → Integer 강제 변환 이슈 회피).

```java
mode, widgetId, menuNo, widgetNm, widgetIcon, widgetType, apiUrl,
widgetDefaultWidth, widgetDefaultHeight, widgetUseyn, userId,
// LIST 전용
pageUseYn, pageRowCnt, moreBtnUrl, listCols (List<Map<String,Object>>: colKey/colNm/colWidth/colAlign/sortOrdr),
// CHART 전용
chartKnd, xAxisKey, xAxisNm, legendUseYn, chartSeries (List<Map<String,Object>>: seriesKey/seriesNm/seriesColor/sortOrdr),
// COUNT 전용
dataKey, unitNm, formatStr, trendUseYn, trendLbl
```

---

## 4. API 레퍼런스

### 4-1. 사용자 대시보드용

#### `POST /api/backoffice/bas/wedget/myWidgetListAjax.do`
내 위젯 목록 (배치 + 정의 + 타입설정 + 컬럼/시리즈 배열까지 병합해서 반환)

```json
// 응답 result.result 예시
[
  {
    "widgetId": "WGT_NOTICE", "widgetNm": "최근 공지사항", "widgetType": "LIST",
    "apiUrl": "/api/board/notice/recent", "widgetDefaultWidth": 2, "widgetDefaultHeight": 2,
    "sortOrder": 1, "posX": 0, "posY": 0, "width": 2, "height": 2,
    "pageUseYn": "N", "pageRowCnt": 5, "moreBtnUrl": "/board/notice",
    "columns": [
      { "widgetId": "WGT_NOTICE", "colKey": "title", "colNm": "제목", "colWidth": "70%", "colAlign": "left", "sortOrdr": 1 },
      { "widgetId": "WGT_NOTICE", "colKey": "regDt", "colNm": "등록일", "colWidth": "30%", "colAlign": "center", "sortOrdr": 2 }
    ]
  }
]
```

#### `POST /availableWidgetListAjax.do`
아직 내 화면에 추가하지 않은(`WIDGET_USEYN='Y'`) 위젯 목록.

#### `POST /insertUserWidget.do`
Body: `UserWedgetInfoReqDto[]` — `[{ widgetId, posX, posY, width, height, sortOrder }]` (userId는 서버가 로그인 세션에서 채움)

#### `POST /updateUserWidgetLayout.do`
Body: `UserWedgetInfoReqDto[]` (배치 전체) — `WIDGET_ID`별 `CASE WHEN` 방식으로 한 번의 UPDATE 문에 일괄 반영.

#### `POST /deleteUserWidget.do`
Body: `[{ widgetId }]`

### 4-2. 관리자(위젯 정의) CRUD용

| Method | URL |
|---|---|
| POST | `/wedgetMstListAjax.do` (페이징, `searchKeyword`로 위젯명 검색) |
| GET | `/wedgetMst/{widgetId}.do` |
| GET | `/wedgetMst/idCheck/{widgetId}.do` |
| POST | `/updateWedgetMst.do` (Body: `WedgetInfoReqDto`, mode=Ins/Edt) |
| DELETE | `/wedgetMst/{widgetId}.do` |

---

## 5. 프론트 파일 구조

```
front_common/src/
├── packages/widget-dashboard/          ← 재사용 가능한 독립 패키지 (README.md 별도 포함)
│   ├── useWidgetDashboard.js            (훅 — 조회/배치상태/저장/추가/삭제)
│   ├── WidgetDashboard.jsx              (바로 쓰는 완성형 컴포넌트, react-grid-layout 포함)
│   ├── WidgetDashboard.css
│   ├── WidgetContainer.jsx              (위젯 카드 공통 프레임 — 자체 데이터 로딩)
│   ├── WidgetRenderer.jsx               (widgetType별 렌더러 분기)
│   ├── WidgetAddModal.jsx
│   ├── renderers/
│   │   ├── ListWidget.jsx
│   │   ├── ChartWidget.jsx              (recharts BAR/LINE/PIE/DOUGHNUT)
│   │   ├── CountWidget.jsx
│   │   └── ProfileWidget.jsx            (apiUrl 미사용 — 쿠키의 로그인 사용자 정보만 표시)
│   └── index.js                          (barrel export)
├── pages/backoffice/Dashboard/MainWidgetDashboard.jsx   (사용자 대시보드 페이지, /sub/dashboard/mainWidget)
└── pages/backoffice/Basic/
    ├── WidgetMstInfo.jsx                  (관리자 CRUD 목록 페이지, /sub/bas/widgetMst)
    └── components/WidgetMstFormModal.jsx  (등록/수정 모달 — 타입별 동적 폼 + 컬럼/시리즈 행 추가/삭제)
```

패키지 재사용법·확장 방법은 `src/packages/widget-dashboard/README.md`에 더 자세히 정리되어 있다.

---

## 6. 실사용 시나리오

### A. 관리자가 새 위젯을 등록할 때
1. `기초 관리 > 위젯 관리` (`/sub/bas/widgetMst`)
2. `위젯 등록` → 위젯 ID 입력 후 **중복체크** (필수)
3. 위젯명 / 아이콘 / 타입(LIST·CHART·COUNT) / **데이터 API 주소** 입력
   - API 주소는 실제로 존재하는, 인증 쿠키로 호출 가능한 GET 엔드포인트여야 함
4. 타입별 하단 설정 입력
   - LIST: 표시 컬럼의 `colKey`는 API 응답 JSON의 실제 키와 반드시 일치해야 화면에 값이 찍힘
   - CHART: X축 키 + 시리즈(Y축) 목록, `seriesKey`도 마찬가지로 API 응답 키와 일치해야 함
   - COUNT: 표시할 `dataKey` + 단위
5. 저장 → 사용유무 Y면 즉시 모든 사용자의 "위젯 추가" 목록에 노출

### B. 사용자가 메인 화면을 꾸밀 때
1. `/sub/dashboard/mainWidget` 접속
2. `편집` → `+ 위젯 추가` → 목록에서 선택 (맨 아래에 기본 크기로 추가됨)
3. 드래그/리사이즈로 배치 조정
4. `저장` (누르기 전엔 서버 반영 안 됨) / 카드의 `✕`로 편집 중 삭제

### C. 새로운 위젯 "타입"을 추가하고 싶을 때 (예: PROGRESS 진행률 위젯)
1. DB에 `TB_WIDGET_PROGRESS_INFO` 같은 설정 테이블 추가 (DDL은 직접 실행 필요, CLAUDE.md 9-1 확인 절차 준수)
2. `WedgetInfoManageMapper`/`.xml`에 해당 타입 조회·삭제·삽입 메서드 추가
3. `WedgetInfoManageService.saveWedgetMst`의 타입 분기(`if "LIST"/"CHART"/"COUNT"`)에 `PROGRESS` 케이스 추가
4. 프론트 `renderers/ProgressWidget.jsx` 작성 → `WidgetRenderer.jsx` switch문에 케이스 추가 → `index.js` export 추가
5. `WidgetMstFormModal.jsx`에 `widgetType === 'PROGRESS'`일 때 보여줄 입력 필드 섹션 추가

### D. CHART 위젯에 기간별(일/주/월) 무거운 집계가 필요할 때
실시간으로 `apiUrl`을 호출하는 대신, `MONGODB.md`의 배치 집계 패턴을 적용한다 — 매일 밤 배치가 MySQL을
미리 집계해 MongoDB에 적재하고, 위젯은 그 결과만 읽어온다. (`ReservationStatCollectJob.java`가 첫 구현 예시)

---

## 7. 알려진 제약사항 / 확인 필요 사항

- **DDL 미실행 가능성**: `ddl-auto: validate`라서 DB에 테이블이 없으면 앱이 아예 기동되지 않는다. 새 프로젝트에 이식 시 DDL 실행 여부부터 확인.
- **반응형**: 모바일(≤768px)에서는 react-grid-layout을 CSS로 끄고 1열 세로 나열로 폴백한다. breakpoint별 레이아웃은 저장하지 않는다(데스크톱 배치만 저장).
- **react-grid-layout 버전**: npm 최신설치 시 v2(API 완전히 다름)가 잡혀서, `react-grid-layout/legacy` 서브패스로 v1 호환 API를 쓰도록 고정했다. 향후 라이브러리 업데이트 시 이 부분 주의.
- **위젯 데이터 API 응답 형식**: 프론트가 강제하지 않고, `{ result: { resultList: [...] } }` 또는 `{ result: { result: ... } }` 형태를 자동으로 벗겨서 사용한다. 다른 형태의 응답이면 위젯이 빈 상태로 보일 수 있음.
- **`apiUrl`은 파라미터 없는 단발 GET**: 기간별 뷰 전환이 필요하면 4번 항목의 D를 참고할 것.
