# AG Grid 날짜 컬럼 포맷 가이드

## 배경

이 프로젝트의 백엔드는 날짜를 대부분 `YYYYMMDD`(대시 없음, 예: `20260804`) 문자열로
내려준다. AG Grid 컬럼에 `field`만 지정하면 이 원본 값이 그대로 노출되어
`20260804`처럼 표시된다. 화면에는 `2026-08-04` 형태로 보여줘야 하므로, 컬럼 정의에
`valueFormatter`를 붙여야 한다.

## 공통 함수

`front/src/lib/formatters.js`에 이미 정의돼 있다. 새로 만들 필요 없이 import해서 쓴다.

```js
// front/src/lib/formatters.js
export const fmtDate     = (v) => (!v ? '-' : String(v).replace(/(\d{4})(\d{2})(\d{2})/, '$1-$2-$3'));
export const fmtDateTime = (v) => (!v ? '-' : String(v).replace(/(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1-$2-$3 $4:$5:$6'));

// AG Grid valueFormatter에 바로 꽂는 래퍼
export const gridDateFormatter     = (params) => fmtDate(params?.value);
export const gridDateTimeFormatter = (params) => fmtDateTime(params?.value);
```

- `fmtDate` / `fmtDateTime`: 값 하나를 받아 포맷된 문자열을 반환(그리드 밖에서도 사용 가능,
  예: 상세 화면 텍스트 표시).
- `gridDateFormatter` / `gridDateTimeFormatter`: AG Grid의 `valueFormatter`는
  `(params) => string` 형태의 콜백을 요구하므로, `params.value`를 꺼내 `fmtDate`에 넘기는
  얇은 래퍼. **컬럼 정의에는 이 두 개를 바로 대입하면 된다.**
- 이미 `YYYY-MM-DD`처럼 대시가 포함된 값이 들어와도 정규식이 매칭되지 않아 원본을 그대로
  반환하므로, 백엔드가 어떤 형식으로 내려주는지 확신이 없어도 안전하게 적용할 수 있다.
- 값이 없으면(`null`/`undefined`/빈 문자열) `-`을 반환한다.

## 적용 방법

```jsx
import { gridDateFormatter, gridDateTimeFormatter } from '@/lib/formatters.js';

const columnDefs = useMemo(() => ([
    { field: 'centerAnniStartDay', headerName: '시작일', width: 110, valueFormatter: gridDateFormatter },
    { field: 'centerAnniEndDay',   headerName: '종료일', width: 110, valueFormatter: gridDateFormatter },
    { field: 'lastUpdtPnttm',      headerName: '수정일자', width: 160, valueFormatter: gridDateTimeFormatter },
    // ...
]), []);
```

`field`가 날짜/일시 컬럼이면 위처럼 `valueFormatter`만 한 줄 추가하면 끝난다. 별도의
`cellRenderer`나 `new Date(...).toLocaleDateString()` 같은 즉석 변환 코드를 새로 작성하지
않는다(기존 `CodeInfo.jsx`의 `lastUpdtPnttm` 컬럼처럼 인라인으로 `new Date().split(' ')[0]`을
쓰던 코드는 레거시 — 새로 만드는 컬럼은 이 가이드를 따른다).

## 언제 어떤 걸 쓰나

| 필드 예시 | DB 원본 형식 | 사용할 포맷터 | 결과 |
|---|---|---|---|
| `centerAnniStartDay`, `schStartday` 등 "일자"류 | `YYYYMMDD` | `gridDateFormatter` | `2026-08-04` |
| `lastUpdtPnttm`, `frstRegistPnttm` 등 "일시"류 | `YYYYMMDDHHMMSS` | `gridDateTimeFormatter` | `2026-08-04 13:05:00` |
| `didStartTime`, `centerStartTime` 등 "시간"만 있는 값(`HH:mm`) | 이미 `HH:mm` 그대로 | 포맷터 불필요 | — |

## 적용된 곳 (2026-08-04)

- `pages/backoffice/BasicManage/components/CenterAnniModal.jsx`
- `pages/backoffice/BasicManage/CenterAnniListPage.jsx`

같은 패턴이 필요한 화면(스케줄/기념일/발송이력 등 `YYYYMMDD` 필드를 그리드에 표시하는 곳)은
새로 함수를 만들지 말고 이 두 함수를 그대로 import해서 쓸 것.
