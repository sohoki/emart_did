# React 컴포넌트 작성 공통 가이드

---

## 1. 삭제 처리 — `useCommonDelete`

**파일:** `front_common/src/hooks/use-common-delete.js`

모든 삭제 처리는 인라인 fetch 대신 이 훅을 사용한다.

### 파라미터

```js
const { handleDelete } = useCommonDelete({
    URL,             // 기본 URL. 실제 요청: `${URL}/${encodeURIComponent(code)}.do` (DELETE)
    MESSAGE,         // 삭제 확인 다이얼로그에 표시할 대상명 (예: '사용자', '부서')
    reloadFunction,  // 성공 후 목록 재조회 함수 (함수 | 'grid')
    gridApiRef,      // (선택) AG Grid API ref — reloadFunction이 'grid'일 때 필요
    callback,        // (선택) 성공 후 추가 콜백 (params, extractedData) => void
    returnParams,    // (선택) 응답 json.result에서 추출할 키 배열
    extraParams,     // (선택) 추가 쿼리 파라미터 [{ key, value }] 또는 (params) => [{ key, value }]
});
```

### `handleDelete` 호출

```js
handleDelete(
    { code, name },        // code: 삭제할 ID, name: 확인창에 표시할 이름
    dynamicExtraParams,    // (선택) [{ key, value }] — 호출 시점에 결정되는 추가 파라미터
    refreshFn,             // (선택) 서브그리드 등 부분 갱신 함수 () => void
)
```

### 성공 조건

```
json?.resultCodeInfo === 'SUCCESS'
```

### 기본 사용 예시 (목록 재조회)

```jsx
const { handleDelete } = useCommonDelete({
    URL: URL.USER_INFO,
    MESSAGE: '사용자',
    reloadFunction: onSearch,
});

// 그리드 삭제 버튼
<button onClick={() => handleDelete({ code: p.data.userId, name: p.data.userNm })}>
    삭제
</button>
```

### 서브그리드 부분 갱신이 필요한 경우 (refreshFn 사용)

```jsx
// reloadFunction 생략, 삭제 후 refreshFn으로 특정 서브그리드만 갱신
const { handleDelete: handleDeletePart } = useCommonDelete({
    URL: URL.DEPT_PART,
    MESSAGE: '부서',
});

// gridContext에서 호출 시 alertSeq를 클로저로 캡처
onDeletePart: (partSeq, parentSeq) => {
    handleDeletePart(
        { code: partSeq, name: partSeq },
        null,
        () => refreshSubGrid(parentSeq),
    );
},
```

### 추가 쿼리 파라미터가 필요한 경우

```jsx
// 정적 파라미터 (훅 선언 시)
const { handleDelete } = useCommonDelete({
    URL: URL.LOG_INFO,
    MESSAGE: '로그',
    reloadFunction: onSearch,
    extraParams: [{ key: 'logType', value: 'ERROR' }],
});

// 동적 파라미터 (handleDelete 호출 시)
handleDelete(
    { code: row.logId, name: row.logId },
    [{ key: 'insttCode', value: selectedInsttCode }],
);
```

---

## 2. 입력/저장 처리 — `useCommonSubmit`

**파일:** `front_common/src/hooks/use-common-submit.js`

Modal 내 등록·수정 저장은 인라인 fetch 대신 이 훅을 사용한다.

### 파라미터

```js
const { handleSubmit } = useCommonSubmit({
    form,              // 제출할 form 상태 객체 (반드시 mode: 'Ins' | 'Edt' 포함)
    URL,               // POST 요청 URL
    confirmMessage,    // 저장 확인 다이얼로그에 표시할 대상명 (예: '사용자 정보')
    checkField,        // 필수 입력 검증 배열 — validateEmptyByType 사용
    reloadFunction,    // 저장 성공 후 목록 재조회 함수 (함수 | 'grid')
    setModalOpen,      // (선택) 성공 시 모달 닫기 setState (false 전달)
    gridApiRef,        // (선택) AG Grid API ref
    type,              // (선택) 'json'(기본) | 'multipart'
    uploadField,       // (선택) FormData 파일 필드명 배열 (type='multipart' 시)
    idFieldMessage,    // (선택) ID 중복 체크 미완료 시 표시 메시지 (기본: '아이디')
    regexFieldCheck,   // (선택) 정규식 검증 배열
    compareField,      // (선택) 두 필드 값 비교 검증 배열
    callback,          // (선택) 저장 성공 후 추가 콜백 () => void
});
```

### form 객체 필수 구조

```js
const [form, setForm] = useState({
    mode: 'Ins',   // 'Ins' = 등록, 'Edt' = 수정
    // ... 나머지 필드
});
```

### 성공 조건

```
json?.resultCodeInfo === 'SUCCESS'
```

### `checkField` 구조 (`validateEmptyByType` 형식)

```js
checkField={[
    { id: 'userId',   type: 'input',  label: '사용자 ID' },
    { id: 'userNm',   type: 'input',  label: '사용자명' },
    { id: 'deptCode', type: 'select', label: '부서' },
]}
```

### `regexFieldCheck` 구조

```js
regexFieldCheck={[
    {
        inputId: 'email',
        regex: /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/,
        message: '올바른 이메일 형식을 입력해 주세요.',
    },
    {
        inputId: 'phone',
        regex: /^(\d{2,3}-\d{3,4}-\d{4})$/,
        message: '올바른 전화번호 형식을 입력해 주세요.',
    },
]}
```

### `compareField` 구조

```js
compareField={[
    {
        primaryId:   'workStime',
        secondaryId: 'workEtime',
        operator:    '<',          // '==' | '!=' | '>' | '<'
        message:     '시작시간은 종료시간보다 이전이어야 합니다.',
    },
]}
```

### 기본 사용 예시 (Modal 등록/수정)

```jsx
const [form, setForm] = useState({ mode: 'Ins', userNm: '', deptCode: '' });

const { handleSubmit } = useCommonSubmit({
    form,
    URL: URL.USER_UPDATE,
    confirmMessage: '사용자 정보',
    checkField: [
        { id: 'userNm',   type: 'input',  label: '사용자명' },
        { id: 'deptCode', type: 'select', label: '부서' },
    ],
    reloadFunction: onSuccess,   // 부모에서 내려온 재조회 콜백
    setModalOpen,
});

<button onClick={handleSubmit}>저장</button>
```

### 파일 업로드가 포함된 경우 (type: 'multipart')

```jsx
const [form, setForm] = useState({ mode: 'Ins', title: '', attachFile: null });

const { handleSubmit } = useCommonSubmit({
    form,
    type: 'multipart',
    uploadField: ['attachFile'],   // FormData에 File 객체로 추가할 필드명
    URL: URL.NOTICE_UPDATE,
    confirmMessage: '공지사항',
    checkField: [{ id: 'title', type: 'input', label: '제목' }],
    reloadFunction: onSearch,
    setModalOpen,
});
```

### sanitize 자동 처리

`useCommonSubmit` 내부에서 자동으로 처리되므로 별도 전처리 불필요:

| 조건 | 처리 |
|------|------|
| 필드명에 `day` 또는 `date` 포함 | 하이픈(`-`) 제거 → `20260618` |
| 필드명에 `price` 또는 `cnt` 포함 | 쉼표(`,`) 제거 |
| null/undefined 값 | 빈 문자열(`''`)로 변환 |

---

## 3. 두 훅 함께 쓰는 패턴 (목록 + 모달)

```jsx
const gridApiRef = useRef(null);
const [modalOpen, setModalOpen] = useState(false);
const [form, setForm] = useState({ mode: 'Ins', /* ... */ });

// 삭제
const { handleDelete } = useCommonDelete({
    URL: URL.ITEM_INFO,
    MESSAGE: '항목',
    reloadFunction: onSearch,
});

// 저장
const { handleSubmit } = useCommonSubmit({
    form,
    URL: URL.ITEM_UPDATE,
    confirmMessage: '항목',
    checkField: [{ id: 'itemNm', type: 'input', label: '항목명' }],
    setModalOpen,
    reloadFunction: onSearch,
});

// 그리드 컬럼
const columnDefs = useMemo(() => [
    { headerName: '항목명', field: 'itemNm', flex: 1 },
    {
        headerName: '수정', width: 70,
        cellRenderer: p => (
            <button onClick={() => {
                setForm({ mode: 'Edt', ...p.data });
                setModalOpen(true);
            }}>수정</button>
        ),
    },
    {
        headerName: '삭제', width: 70,
        cellRenderer: p => (
            <button onClick={() => handleDelete({ code: p.data.itemId, name: p.data.itemNm })}>
                삭제
            </button>
        ),
    },
], [handleDelete]);
```

---

## 4. 주의사항

- **삭제 URL 형식**: `${URL}/${encodeURIComponent(code)}.do` — DELETE method 자동 적용
- **저장 URL 형식**: URL 그대로 POST 요청, form 전체가 body로 전송됨
- **성공 체크**: 두 훅 모두 `json?.resultCodeInfo === 'SUCCESS'` 기준 (기존 `STATUS` 패턴 혼용 금지)
- **서브그리드 갱신**: `reloadFunction` 대신 `refreshFn`(3번째 인자)으로 동적 콜백 전달
- **모달 닫기**: `setModalOpen`을 훅에 전달하면 성공 시 자동으로 `false` 호출됨

---

## 5. Modal Input Validation — 정규식 유효성 검사

### ⚠️ 정규식 작성 전 반드시 확인

**새 정규식을 인라인으로 만들기 전에 `src/lib/validators.js`를 먼저 확인한다.**
이미 등록된 것을 다시 선언하면 불일치가 생기고 유지보수가 어렵다.

```js
// src/lib/validators.js — 현재 등록된 공용 정규식
import { passwordRegex, emailRegex, telRegex, urlRegex } from '@/lib/validators.js';
```

| export 이름 | 용도 | 패턴 요약 |
|---|---|---|
| `passwordRegex` | 비밀번호 | 영문+숫자+특수문자, 8~16자 |
| `emailRegex` | 이메일 | 표준 이메일 형식 |
| `telRegex` | 전화번호(표준) | 지역번호·휴대폰 형식 |
| `urlRegex` | URL | http(s)://~ |

없는 패턴이 필요하면 **`validators.js`에 추가 후 import** — 컴포넌트 안에 인라인으로 선언하지 않는다.

---

### 입력 포맷 헬퍼

```js
// src/lib/formatters.js — 입력 중 자동 하이픈
import { formatTel } from '@/lib/formatters.js';

// onChange 예시
onChange={(e) => updateForm({ tel: formatTel(e.target.value) })}
```

---

Modal 내 특수 필드(이메일·전화번호·비밀번호·URL)는 `regexFieldCheck`에 아래 정규식을 적용한다.

| 필드 | import | 패턴 |
|------|--------|------|
| 이메일 | `emailRegex` | `/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/` |
| 전화번호 | `telRegex` | `/^(02\|0[3-9]{1}[0-9]{1}\|010)-[0-9]{3,4}-[0-9]{4}$/` |
| 비밀번호 | `passwordRegex` | 영문+숫자+특수문자, 8~16자 |
| 홈페이지 | `urlRegex` | `/^(https?:\/\/)?([\w-]+\.)+[\w]{2,}(\/\S*)?$/i` |

### 전화번호 포맷 헬퍼 (`formatTel`)

```js
const formatTel = (raw = '') => {
    const d = raw.replace(/\D/g, '');
    if (!d.startsWith('0'))    return d.replace(/(\d{4})(\d{4})/, '$1-$2');      // 대표번호
    if (d.startsWith('02'))    return d.replace(/(\d{2})(\d{3,4})(\d{4})/, '$1-$2-$3');
    return d.replace(/(\d{3})(\d{3,4})(\d{4})/, '$1-$2-$3');
};
```

### 사업자 구분별 등록번호

```js
const getNumType = (val, options = []) => {
    const item = options.find(o => (o.codeDetailId ?? o.code) === val);
    const nm = (item?.codeDetailNm ?? item?.codeNm ?? '').trim();
    return (nm && nm.includes('개인') && !nm.includes('사업자')) ? 'RESIDENT' : 'BIZ';
};
// RESIDENT: /^\d{6}-\d{7}$/ (주민등록번호)
// BIZ:      /^\d{3}-\d{2}-\d{5}$/ (사업자등록번호)
```

---

## 6. Modal UX 가이드라인

- 사용/미사용 토글 → `IosSwitch.jsx` 컴포넌트 사용 (`onText`, `offText` 속성)
- 라디오 그룹 → `useRadioGroup` 훅 + `GroupSelect.jsx` 컴포넌트
- 파일 업로드 → `use-file-upload.js` 훅 사용
- 그리드 이미지 셀 → 이미지 있으면 `<img>`, 없으면 no-image 아이콘 표시 (`NO_IMG_SRC` — 섹션 10 참고)

### `useEffect` 내 setState 규칙

```jsx
// ❌ 금지
useEffect(() => {
    if (!open) setState(EMPTY);
}, [open]);

// ✅ 올바른 패턴 (cleanup 함수에서 호출)
useEffect(() => {
    if (!open) return;
    return () => setState(EMPTY);
}, [open]);
```

---

## 7. 다크모드 색상 규칙 — Table / Grid / Modal

### 핵심 원칙

하드코딩된 밝은 색상(`#fff`, `#000`, `#111827` 등)을 Table·Grid·Modal에 직접 지정하면
다크모드 전환 시 흰 배경에 흰 글자(또는 검은 배경에 검은 글자)가 되어 텍스트가 보이지 않는다.
**반드시 CSS 변수 또는 `inherit`를 사용해 테마를 따르게 한다.**

### `<td>` / `<tr>` 텍스트 색상

```jsx
// ✅ 올바른 패턴 — 다크/일반 모드 모두 대응
<td style={{ color: 'inherit' }}>내용</td>

// ❌ 금지 — 다크모드에서 글자가 안 보임
<td style={{ color: '#111827' }}>내용</td>
<td style={{ color: '#000' }}>내용</td>

// ❌ 금지 — 일반모드에서 글자가 안 보임
<td style={{ color: '#fff' }}>내용</td>
```

- `color: 'inherit'` → 브라우저/테마가 결정
  - 일반(라이트) 모드: 진한 회색/검정으로 렌더링
  - 다크 모드: 흰색으로 렌더링

### `<table>` 수준 기본값 설정

테이블 자체에도 `color: 'inherit'` 를 주면 모든 하위 셀에 자동 상속된다.

```jsx
<table style={{ color: 'inherit' }}>
```

### Modal 배경색

```jsx
// ✅ 올바른 패턴 — 다크/일반 모드 모두 대응
<div
    className="modal-dialog"
    style={{ backgroundColor: 'var(--bs-body-bg, #fff)' }}
>

// ❌ 금지 — 다크모드에서 흰 배경이 고정됨
<div className="modal-dialog" style={{ background: '#fff' }}>

// ❌ 금지 — 일반모드에서 투명해져 배경이 비쳐 보임
<div className="modal-dialog" style={{ backgroundColor: 'transparent' }}>
```

### 보조 텍스트 / 설명 문구

```jsx
// ✅ 올바른 패턴
<div style={{ color: 'inherit', opacity: 0.6 }}>설명 문구</div>

// ❌ 금지
<div style={{ color: '#6b7280' }}>설명 문구</div>
```

### 테두리 / 구분선

```jsx
// ✅ 올바른 패턴 — 반투명 회색 (다크/라이트 모두 어울림)
borderBottom: '1px solid rgba(128,128,128,0.2)'

// ❌ 금지 — 다크모드에서 거의 안 보임
borderBottom: '1px solid #f3f4f6'
```

### 섹션 배경

```jsx
// ✅ 올바른 패턴
background: 'rgba(128,128,128,0.05)'

// ❌ 금지
background: '#f9fafb'
```

### 빠른 체크리스트 (코드 작성 후 확인)

- [ ] `<td>` 에 `color: '#000'` 또는 `color: '#111827'` 등 하드코딩 없음
- [ ] `<table>` 또는 `<td>` 에 `color: 'inherit'` 적용
- [ ] Modal `.modal-dialog` 배경이 `var(--bs-body-bg, #fff)` 또는 생략
- [ ] 보조 텍스트가 하드코딩 색상 대신 `color: 'inherit', opacity: 0.6`
- [ ] 테두리가 `rgba(128,128,128,0.2)` 계열

---

---

## 8. Rich Text 에디터 — `LexicalEditor`

**파일:** `src/components/Common/LexicalEditor.jsx`

`<textarea>`로 다중 줄 텍스트를 입력받는 모든 곳은 LexicalEditor로 교체한다.
이미지·테이블·링크·HTML 편집 기능이 기본으로 포함되어 있으므로 별도 구현 불필요.

### Props

```js
<LexicalEditor
    id="fieldId"                    // DOM id (라벨 연결, 필수 체크)
    label="설명"                    // 상단 라벨 텍스트 (선택)
    required                        // 라벨 * 표시
    value={form.fieldName || ''}    // Lexical JSON 직렬화 문자열
    onChange={(val) => updateForm({ fieldName: val })}
    height={200}                    // 에디터 최소 높이 (기본 200)
    placeholder="내용을 입력하세요."
    readOnly={false}                // true면 뷰어 모드 (툴바 숨김)
    imageUploadUrl="/api/.../upload.do"  // 이미지 서버 업로드 URL (없으면 base64)
/>
```

### 툴바 기능 (모두 기본 포함)

| 버튼 | 기능 |
|---|---|
| ↩ ↪ | 실행 취소 / 다시 실행 |
| B I U S̶ | 굵게·기울임·밑줄·취소선 |
| • 목록 / 1. 목록 | 비순서·순서 목록 |
| ⊞ 테이블 | 최대 8×8 그리드 팝업으로 테이블 삽입. 셀 경계를 드래그해 컬럼 너비 조절 가능 |
| 🔗 링크 | URL 입력 + **직접 연결 / 신규 창** 선택. 링크 제거 버튼 포함 |
| 🖼 이미지 | 파일 선택 또는 에디터 영역에 **드래그&드롭**. 삽입 후 **8방향 핸들 드래그**로 리사이징 |
| </> | **HTML 소스 편집 모드** 토글. 소스 직접 수정 후 다시 토글하면 반영 |

### 이미지 업로드 동작

| `imageUploadUrl` | 동작 |
|---|---|
| **미지정** | FileReader → base64 dataURL → 에디터 삽입 (서버 불필요) |
| **지정** | `POST {imageUploadUrl}` FormData → 응답 `result.result` (파일경로) → `VITE_REACT_APP_IMG_URL + 경로` 삽입 |

서버 업로드 응답 형식: `{ resultCodeInfo: 'SUCCESS', result: { result: '/path/img.jpg' } }`

### 관련 파일 (직접 수정 금지)

- `src/components/Common/lexical/ImageNode.jsx` — 이미지 DecoratorNode + 리사이즈 핸들 컴포넌트
- `src/components/Common/lexical/ImagePlugin.jsx` — INSERT_IMAGE_COMMAND 처리
- `src/components/Common/lexical/DragDropPlugin.jsx` — 에디터 영역 이미지 드래그&드롭

### 기본 사용 예시

```jsx
import LexicalEditor from '@/components/Common/LexicalEditor.jsx';

// 폼 내부
<LexicalEditor
    id="optionExplain"
    label="설명"
    value={form.optionExplain || ''}
    onChange={(val) => updateForm({ optionExplain: val })}
    height={160}
/>

// 이미지 서버 업로드가 필요한 경우
<LexicalEditor
    id="productDetail"
    label="상품 상세"
    value={form.productDetail || ''}
    onChange={(val) => updateForm({ productDetail: val })}
    height={300}
    imageUploadUrl="/api/backoffice/infra/upload/image.do"
/>
```

### useCommonSubmit과 함께 사용

```js
// checkField: 빈 값 체크 (DOM id 매핑)
checkField: [
    { inputId: 'optionExplain', type: 'text', message: '설명을 입력해주세요.' },
]
// LexicalEditor의 id prop이 inputId와 일치해야 합니다.
```

---

## 9. Input 요소 — `id`, `name` 필수 지정

`<input>`, `<select>`, `<textarea>` 등 모든 입력 요소는 `id`와 `name` 속성을 **반드시 함께** 지정한다.

### 이유

- `id`: `<label htmlFor>` 연결, `checkField`/`regexFieldCheck`의 `inputId` 매칭에 사용
- `name`: `e.target.name` 기반 공통 change 핸들러, 브라우저 자동완성·접근성에 필요

### 규칙

- `id`와 `name`은 원칙적으로 폼 필드명과 동일하게 맞춘다 (예: `id="userName" name="userName"`)
- 둘 중 하나만 지정하는 것은 금지 — 항상 함께 작성한다

### 예시

```jsx
// ✅ 올바른 패턴
<input
    type="text"
    className="form-control"
    id="userName"
    name="userName"
    value={form.userName}
    onChange={(e) => updateForm({ userName: e.target.value })}
/>

// ❌ 금지 — id 또는 name 누락
<input type="text" className="form-control" value={form.userName} onChange={(e) => updateForm({ userName: e.target.value })} />
<input type="text" className="form-control" id="userName" value={form.userName} onChange={(e) => updateForm({ userName: e.target.value })} />
```

---

## 10. 이미지 표시 — `NO_IMG_SRC` 공통 상수

**파일:** `src/constants/IMAGE.jsx`

이미지가 없거나(`null`/`undefined`/`''`) 로드에 실패할 수 있는 모든 `<img>`는
파일마다 placeholder SVG를 새로 만들지 않고 공통 상수 `NO_IMG_SRC`를 import해서 쓴다.

### 이유

과거 동일한 SVG data URI 코드가 7개 파일에 그대로 복붙되어 있었다 — 하나를 수정해도 나머지가
따로 놀게 되므로, 새 화면을 만들 때도 반드시 공통 상수를 재사용해 이 문제가 재발하지 않게 한다.

### 사용 패턴

```jsx
import { NO_IMG_SRC } from '@/constants/IMAGE.jsx';
import config from '@/config/index.jsx';

<img
    src={row.productPic ? `${config.REACT_APP_IMG_URL}${row.productPic}` : NO_IMG_SRC}
    alt=""
    style={{ width: 56, height: 56, objectFit: 'cover', borderRadius: 8 }}
    onError={(e) => { e.currentTarget.src = NO_IMG_SRC; }}
/>
```

### 규칙

- 이미지 필드가 빈 값일 때뿐 아니라, URL은 있지만 실제 로드가 실패하는 경우까지 대비해
  `onError`에서도 `NO_IMG_SRC`로 폴백한다 (둘 다 필수 — 하나만 처리하면 깨진 이미지 아이콘이 노출될 수 있음)
- 새 placeholder SVG를 인라인으로 다시 선언하지 않는다 — 필요한 크기/스타일은 `<img style={{...}}>`에서 조절

### ❌ 금지

```jsx
// 컴포넌트 파일 안에 placeholder SVG를 새로 선언
const NO_IMG_SRC = `data:image/svg+xml,${encodeURIComponent('<svg>...')}`;
```

---

## 11. 위젯 대시보드 — `WIDGET.md`

**파일:** `.claude/commands/WIDGET.md`

관리자가 등록하고 사용자가 메인 화면에 자유 배치하는 위젯(LIST/CHART/COUNT/PROFILE) 시스템 전체 —
DB 스키마 7개 테이블, 백엔드/프론트 파일 구조, API 레퍼런스, 새 위젯 타입 추가 절차까지 정리되어 있다.
다른 프로젝트에서도 재사용 가능하도록 `SKILL.md`와 별도 문서로 관리한다.

CHART 위젯에 기간별(일/주/월) 무거운 집계가 필요하면 `WIDGET.md`만으로는 부족하다 — 12번(`MONGODB.md`)을
함께 참고한다.

---

## 12. 배치 집계 → MongoDB 패턴 — `MONGODB.md`

**파일:** `.claude/commands/MONGODB.md`

CHART 위젯처럼 기간별 집계가 무거운 데이터는 실시간 API 대신, 배치가 매일 밤 MySQL을 미리 집계해서
MongoDB에 적재해두고 화면은 읽기만 하는 구조를 쓴다. 배치 스케줄, 컬렉션 네이밍/upsert 규칙,
`InterfaceResultInfoManageService`를 통한 실행 결과 로깅, `AesUtil` 자격증명 암호화 규칙까지
`MONGODB.md`에 정리되어 있다 — 새 배치를 추가할 때 반드시 그 패턴을 따른다.

---

## 13. 반복된 버그 패턴 모음 — `PITFALLS.md`

**파일:** `.claude/commands/PITFALLS.md`

2026-06-25~07-18 작업 로그를 정리해서, 같은 원인으로 2회 이상 반복된 버그와 한 번이지만 중요했던 함정을
모아둔 문서. 날짜 input 하이픈 처리, ag-grid 모달 portal, SweetAlert2 z-index, HTML 엔티티 손상,
import 경로 대소문자, CASE-WHEN 일괄 UPDATE, `@Transactional` 매니저 트랩, MyBatis 멀티스테이트먼트
반환값, mapper copy-paste 실수, JPA Y/N 컬럼 정의, `set-state-in-effect`, camelCase 매핑 오타, multipart
필드명 충돌, JWT `LoginVO` 반쪽 복원, LexicalEditor 이미지 전용 콘텐츠 오판, 고객(customer) JWT vs
관리자 JWT 충돌(`fnAjaxFetch`), Clipboard API HTTPS 제약, 인증 JWT 단일화(`HttpSession` 저장 금지) 등
19개 패턴 — 새 코드를 작성하기 전에 관련 항목이 있는지 훑어볼 것.

---

## 14. Multi-tenancy(거래처 데이터 격리) — `MULTI_TENANCY.md`

**파일:** `.claude/commands/MULTI_TENANCY.md`

거래처(판매사/공급사)가 `PartnerLogin`으로 직접 로그인해 관리자 화면과 라우트/컨트롤러를 공유하는
구조라, 새 화면·API가 다른 거래처 데이터를 노출/수정하지 않도록 지키는 규칙 모음. 백엔드
`@TenantScoped` 어노테이션 사용법, 프론트 comCode 잠금 패턴, 신규 기능 체크리스트, 2026-07-17 전수
감사에서 발견된 미해결 항목 목록까지 정리되어 있다. `SKILL.md`와 별도 문서로 관리한다.

---

## 트리거 조건

- 삭제 버튼 구현 → `useCommonDelete` 필수 적용
- Modal 저장 버튼 구현 → `useCommonSubmit` 필수 적용
- Modal 폼에 이메일·전화번호·비밀번호·URL 필드 포함 → `regexFieldCheck` 적용
- **Modal 폼에 `<textarea>` 포함 → `LexicalEditor`로 교체** (이미지 업로드 기본 포함)
- LexicalEditor 필드를 필수 입력으로 `regexFieldCheck`에 등록할 때 → `PITFALLS.md` 16번(텍스트 OR 이미지 노드 허용) 패턴 필수 사용
- 인라인 `fetch`/`fnAjaxFetch`로 삭제·저장 처리하는 코드 발견 시 → 훅으로 리팩토링
- Table/Grid/Modal 구현 시 → 다크모드 색상 규칙(섹션 7) 체크리스트 확인
- `<input>`/`<select>`/`<textarea>` 작성 시 → `id`, `name` 속성 필수 동시 지정(섹션 9) 확인
- 이미지가 없거나 로드 실패할 수 있는 `<img>` 작성 시 → `NO_IMG_SRC`(섹션 10) 필수 사용, 인라인 placeholder SVG 새로 선언 금지
- 위젯 대시보드(LIST/CHART/COUNT/PROFILE) 관련 작업 시 → `WIDGET.md`(섹션 11) 먼저 확인
- CHART 위젯 등 기간별 무거운 집계가 필요할 때 → 실시간 대신 배치+MongoDB 패턴 검토 (`MONGODB.md`, 섹션 12)
- **신규 프로젝트/새 대시보드 착수 시** → 배치 집계가 필요한 지표가 있는지 먼저 확인하고, 있다면 `MONGODB.md` 패턴 적용 여부를 사용자에게 질문
- `<input type="date">` 신규 추가 시 → `PITFALLS.md` 1번(하이픈 처리) 필수 적용
- ag-grid master/detail 안에 모달 신규 작성 시 → `PITFALLS.md` 2번(`createPortal`) 필수 적용
- 기존 MyBatis mapper를 복사해서 새 INSERT/UPDATE 작성 시 → `PITFALLS.md` 9번(테이블/컬럼명 재확인) 필수
- 새 Y/N 플래그 JPA 필드 추가 시 → `PITFALLS.md` 10번(`columnDefinition = "char(1)"`) 필수
- 서비스 메서드가 JPA repository를 호출할 때 → `PITFALLS.md` 7번(`@Transactional` 매니저 명시) 확인
- `LoginVO`에 새 필드 추가(로그인 응답에 정보 추가) 또는 role/거래처 기반 인증·필터링 관련 작업 시 → `PITFALLS.md` 15번(JWT claims / `getLoginVOFromToken` 동시 반영) 필수 확인
- 원인이 바로 안 잡히는 버그(상세조회만 비어보임, 데이터가 엉뚱한 테이블에 저장됨 등) 조사 시 → `PITFALLS.md` 전체 훑어보기
- 거래처(판매사/공급사)가 접근 가능한 새 화면·API 작성 시, 또는 URL 파라미터/쿼리로 comCode를 받는 코드 작성 시 → `MULTI_TENANCY.md`(섹션 14) 체크리스트 필수 확인
- 거래처 소유 테이블(호텔/상품/주문/정산/거래처 사용자 등) 조회·수정·삭제 API 신규 작성 시 → `@TenantScoped` 적용 + 매퍼 XML `COM_CODE` 필터 동반 여부 확인 (`MULTI_TENANCY.md` 2번)
- 거래처 소유 테이블의 단건 삭제/수정 컨트롤러를 새로 작성할 때 → PK만으로 키하지 말고 `AuthHelper.isOwnTenant(ownerComCode)` 소유권 체크를 반드시 넣었는지 커밋 전 재확인 (관리자 전용 화면이라도, 같은 엔드포인트가 나중에 거래처 자기서비스 화면에서도 호출될 수 있음 — `AccountQnaInfoManageController`에서 실제로 이 체크 누락을 뒤늦게 발견해 수정한 사례 있음)
- 판매사몰 고객(customer) 로그인이 필요한 API를 프론트에서 호출할 때 → `PITFALLS.md` 17번(`fnAjaxFetch` 대신 raw axios + `getCustomerAuthHeader()`) 필수 확인
- "URL 복사" 등 클립보드 복사 기능을 새로 만들 때 → `PITFALLS.md` 18번(`navigator.clipboard` 존재 확인 + 폴백) 필수 적용
- 관리자/거래처 인증(로그인·로그아웃·권한 체크·감사 로깅) 관련 백엔드 코드를 새로 작성하거나 수정할 때 → `PITFALLS.md` 19번(`HttpSession`/`EgovUserDetailsHelper` 사용 금지, `AuthHelper`/`CustomerAuthHelper`로만 인증) 필수 확인 — 이 프로젝트는 인증을 JWT 단일 체계로만 쓴다
- 새 `@Scheduled` 배치를 추가할 때 → `ShedLockConfig.java`(2026-07-18 도입, `egovframework.com.config`) 패턴대로 `@SchedulerLock(name=..., lockAtMostFor=..., lockAtLeastFor=...)` + `LockAssert.assertLocked()` 필수 적용(다중 인스턴스에서 중복 실행 방지). `name`은 다른 배치와 겹치지 않는 고유한 값으로 지정할 것
