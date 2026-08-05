
# Claude 작업 규칙
- Prefre simple, readable code over clever code
- Always respect the project's runtime version and environment settings

## 응답 언어
- 항상 **한국어**로 응답한다.

---

## 1. 작업 시작 전 체크리스트

작업을 시작하기 전에 아래 항목을 순서대로 확인한다.

1. **브랜치 확인** — 현재 브랜치가 `main` 또는 `dev`이면 작업 전에 경고하고 확인을 받는다.
2. **인코딩 확인** — 대상 파일이 UTF-8인지 확인한다. UTF-8이 아니면 변환 후 작업한다.
3. **작업 범위 파악** — 수정할 파일과 영향 범위를 먼저 요약해서 보고한다.
4. **수정 금지 파일 확인** — 아래 "절대 수정 금지 파일" 목록과 대조한다.

---

## 2. 인코딩 규칙

- 작업 전 반드시 파일 인코딩을 확인하고, UTF-8이 아니면 UTF-8로 변환 후 진행한다.
- 작업 중 한글이 깨지면 **즉시 작업을 중단**하고 아래 경고 메시지를 출력한다.

```
⚠️ 경고: 한글 깨짐 감지
파일: [파일 경로]
작업을 중단합니다. 인코딩을 확인해 주세요.
```

---

## 3. 코드 스타일 규칙

- **프레임워크:** 전자정부 프레임워크 (eGovFrame) 기반
- **들여쓰기:** 스페이스 2칸 (HTML/CSS/JS) 또는 4칸 (Java)
- **따옴표:** JavaScript는 작은따옴표(`'`) 사용
- **세미콜론:** JavaScript는 세미콜론 필수
- **주석:** 한국어 우선, 영어 혼용 허용
- **변수/함수명:** camelCase, 클래스명은 PascalCase

---

## 4. 절대 수정 금지 파일

아래 파일은 명시적 허락 없이 절대 수정하지 않는다.

```
globals.css
공통 설정 파일 (config.js, application.properties 등)
공통 레이아웃 파일 (layout.html, header.html, footer.html 등)
```

> 수정이 필요하다고 판단되면 수정하지 않고 먼저 사용자에게 보고한다.

---

## 5. 에러 처리 규칙

- **빌드 에러** 발생 시 즉시 작업을 중단하고 에러 내용을 보고한다.
- **테스트 실패** 시 커밋하지 않는다.
- 에러 해결이 불가능하면 작업 전 상태로 되돌리고 사용자에게 상황을 보고한다.

---

## 6. 커밋 메시지 형식

```
feat: 새로운 기능 추가
fix: 버그 수정
refactor: 리팩토링 (기능 변경 없음)
style: 코드 포맷, 스타일 변경 (기능 변경 없음)
docs: 문서 수정
chore: 빌드, 설정 파일 수정
```

**예시:**
```
feat: 민원 목록 페이지 검색 필터 추가
fix: 로그인 세션 만료 시 리다이렉트 오류 수정
```

---

## 7. Git 작업 순서

작업 완료 후 아래 순서를 반드시 지킨다.

```
1. git fetch
2. git pull
   - 타 사용자의 변경사항이 있으면 내용을 보여주고 사용자 확인 후 진행
   - 충돌(conflict) 발생 시 즉시 중단하고 사용자에게 보고
3. git add [변경 파일]
4. git commit -m "커밋 메시지"
5. git push
```

> `git pull` 중 다른 사용자의 변경이 감지되면:
> - 변경된 파일 목록과 내용을 요약해서 보여준다.
> - 사용자가 확인 및 승인한 후에만 다음 단계로 진행한다.

---

## 8. 작업 로그 저장 규칙

작업이 완료될 때마다 프로젝트 루트(`E:\dev\front\did_emart`)에
`YYYY-MM-DD.md` 파일에 항목을 추가한다. (2026-07-27부터 적용, 이전에는
`E:\dev\front\common\front_common`에 기록했음 — 사용자 지시로 위치 변경)

파일이 없으면 `# YYYY-MM-DD 작업 로그` 헤더로 새로 생성한다.

### 로그 형식

```markdown
## HH:MM — 파일명 또는 작업 제목
**파일:** `경로/파일명`

### 변경 내용
- ...

### 리뷰 체크리스트 결과
- [x] Code quality & readability
- [x] Naming conventions & style
- [x] Logic errors & edge cases
- [x] Security vulnerabilities
- [x] Performance concerns
- [ ] Test coverage

### 이슈 / 개선사항
- ...
```

---

## 9. DB 작업 규칙

### 9-1. 테이블/객체 생성 전 필수 확인

DB에 새로운 객체(테이블, 함수, 프로시저, 트리거 등)를 생성하기 전에  
**반드시 사용자에게 아래 내용을 확인받은 후 진행한다.**

```
🛢️ DB 생성 확인 요청

생성 유형: [테이블 / 함수 / 프로시저 / 트리거]
이름: [생성할 객체명]
목적: [용도 한 줄 설명]
영향 범위: [연관 테이블 또는 기능]

진행할까요? (확인 후 작업합니다)
```

> ⚠️ 사용자 승인 없이 DB 객체를 생성, 수정, 삭제하지 않는다.

---

### 9-2. DB 네이밍 규칙

모든 DB 객체는 아래 접두사 규칙을 반드시 따른다.

| 객체 유형 | 접두사 | 예시 |
|-----------|--------|------|
| 테이블 (Table) | `TB_` | `TB_USER_INFO` |
| 함수 (Function) | `FN_` | `FN_GET_USER_NM` |
| 프로시저 (Procedure) | `SP_` | `SP_INSERT_USER` |
| 트리거 (Trigger) | `TR_` | `TR_USER_UPDATE` |
| 인덱스 (Index) | `IDX_` | `IDX_TB_USER_INFO_01` |
| 뷰 (View) | `VW_` | `VW_USER_DEPT_INFO` |

### 추가 네이밍 세부 규칙

- 모든 이름은 **대문자 + 언더스코어(`_`)** 조합으로 작성한다.
- 이름은 **명확하고 의미 있게** 작성한다. (약어 남용 금지)
- 단어 구분은 언더스코어(`_`)로 한다.
- 길이는 최대 **30자** 이내를 권장한다.
- **새 테이블/컬럼명을 제안하기 전에 단어 뜻과 스펠링을 먼저 확인**하고, 확인한 내용(뜻/철자)을
  사용자에게 함께 알려준다. (예: "Vender"는 "Vendor"의 오타 — 실제로 이 프로젝트에서 발생해
  6개 테이블명이 잘못 명명된 적이 있음)

**올바른 예시:**
```sql
TB_CIVIL_APPLICATION      -- 민원 신청 테이블
FN_GET_DEPT_NM            -- 부서명 조회 함수
SP_INSERT_CIVIL_APP       -- 민원 신청 등록 프로시저
TR_CIVIL_APP_UPDATE       -- 민원 신청 수정 트리거
IDX_TB_CIVIL_APP_01       -- 민원 신청 테이블 인덱스 (테이블명 + 순번)
VW_CIVIL_APP_USER         -- 민원 신청 + 사용자 조인 뷰
```

**잘못된 예시:**
```sql
tbl_user               -- 소문자 사용 금지
T_U                    -- 의미 없는 약어 금지
TABLECIVILAPPLICATION  -- 언더스코어 없이 붙여쓰기 금지
USER                   -- 접두사 없음 금지
index_user             -- 소문자 및 잘못된 접두사
view_dept              -- 소문자 및 잘못된 접두사
```

> 💡 **인덱스 네이밍 팁:** `IDX_[테이블명]_[순번(01~)]` 형식을 권장한다.  
> 예: `TB_USER_INFO` 테이블의 첫 번째 인덱스 → `IDX_TB_USER_INFO_01`

---
## JAVA 파일 작업시 고려 사항
java 파일에서는 줄간격을 space 가 아니라 tab 형태로 해서 최대한 간격 맞춰줘

- **백엔드(basic/backend)는 Java 17 기준이다.** Spring Boot 3.x + Jakarta EE 9+ 이므로
  `javax.validation.*`, `javax.servlet.*` 등 Jakarta EE 계열 패키지는 반드시 `jakarta.*`로 써야 한다.
  단, `javax.crypto.*`, `javax.sql.*`, `javax.xml.*`, `javax.imageio.*` 등 JDK 자체 패키지는
  Jakarta EE 이관 대상이 아니므로 `javax.*` 그대로 유지한다(둘을 혼동해서 바꾸지 않도록 주의).

## 10. 작업 완료 보고 형식

작업이 끝나면 아래 형식으로 요약 보고한다.

```
✅ 작업 완료 보고

작업 내용: [한 줄 요약]

변경 파일:
- 경로/파일명1
- 경로/파일명2

영향 범위:
- [영향받는 기능이나 페이지]

특이사항:
- [없으면 "없음"]
```

---

## 11. 프론트엔드 리스트(관리) 화면 기본 골격

`AppLayout`(사이드바/헤더) 하위에서 렌더링되는 관리자 리스트류 화면은 아래 골격을 기본으로
따른다. (2026-08-04부터 적용, `ManagerListPage.jsx`부터 시작해 순차 적용 중)

```jsx
<div className="row g-0 main-contents">
    <div className="col-12 content-header">
        <div className="content-header__title">관리자 관리</div>
        <div className="content-header__breadcrumb">
            <ol className="breadcrumb">
                <li className="breadcrumb-item">인사 관리</li>
                <li className="breadcrumb-item">관리자 관리</li>
            </ol>
        </div>
    </div>

    <div className="col-12 content-search">
        <div className="row g-0 w-100 justify-content-between">
            <div className="col-auto content-search__option">
                <select id="searchCondition" name="searchCondition" value={tempParams.searchCondition}
                    onChange={handleInputChange}>
                    <option value="">선택</option>
                    <option value="adminNm">이름</option>
                    <option value="adminId">아이디</option>
                    <option value="adminEmail">이메일</option>
                </select>
                <input type="text" id="searchKeyword" name="searchKeyword" placeholder="검색어를 입력하세요"
                    value={tempParams.searchKeyword}
                    onChange={handleInputChange}
                    onKeyDown={onSearchKeyDown}
                />
            </div>
            <div className="col-auto content-search__action">
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={() => onSearch(1)}>검색</button>
                <button type="button" className="btn btn-outline-dark btn-outline__gray"
                    onClick={handleReset}>검색 초기화</button>
                <button type="button" className="btn btn-primary btn-default__blue"
                    onClick={() => handleOpenManagerModal()}>관리자 등록</button>
            </div>
        </div>
    </div>

    <div className="col-12 content-table content-table__main">
        <div className="ag-theme-material" style={{ height: 760, width: '100%' }}>
            <AppAgGrid
                columnDefs={columnDefs}
                theme={gridTheme}
                defaultColDef={defaultColDef}
                rowModelType="infinite"
                pagination={true}
                paginationPageSize={pageUnit}
                cacheBlockSize={pageUnit}
                maxBlocksInCache={2}
                rowSelection={{ mode: 'singleSelect' }}
                onGridReady={onGridReady}
                overlayNoRowsTemplate="<span class='ag-overlay-loading-center'>데이터가 없습니다.</span>"
                overlayLoadingTemplate="<span class='ag-overlay-loading-center'>조회 중...</span>"
            />
        </div>
    </div>
</div>
```

### 규칙
- 최상위는 `<div className="row g-0 main-contents">` 하나로 감싼다. `content-header`(제목 +
  breadcrumb) / `content-search`(검색조건 select+input, 검색/초기화/등록 버튼) /
  `content-table content-table__main`(그리드) 3단 구성을 그대로 따른다.
- 클래스명(`content-header__title`, `content-search__option`, `btn-outline__gray`,
  `btn-default__blue` 등)은 전부 `public/resource/css/common.css`(index.html에서 전역 로드)에
  이미 정의돼 있으므로 별도 CSS 파일을 새로 만들지 않는다.
- 검색조건 `select`/`input`은 **className 없이 순수 태그**로 둔다 — `common.css`의
  `.content-search__option select/input` 규칙이 부모 클래스 기준으로 스타일을 입힌다.
- 그리드는 클라이언트 사이드 배열이 아니라 `hooks/grid/use-grid-infinite.js`의
  `useGridInfinite({ fetchApi, pageUnit, initialFilters })`로 무한스크롤(rowModelType="infinite")
  방식을 쓴다. `fetchApi(query)`는 `{ rows, total }`을 반환해야 한다.
- breadcrumb 상위 항목(`인사 관리` 등)은 해당 화면이 속한 그룹명으로 채운다(사이드바 메뉴
  그룹과 맞출 것).
- 이 골격을 기준으로 새 리스트 화면을 만들 때는 `pages/backoffice/Basic/RoleInfo.jsx` 또는
  `ProgrameInfo.jsx`를 실제 동작 예시로 참고한다(동일 골격 + `useGridInfinite` + 등록/수정
  모달 패턴이 이미 구현돼 있음).

---