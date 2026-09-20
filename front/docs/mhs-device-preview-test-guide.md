# MHS 모니터 단말 화면 승격 — 테스트 가이드

`MhsMonitorDevicePage.jsx`(실제 단말 렌더링 페이지)와 `device/preview.do`(인증 불필요 공개 API)를
로컬에서 확인하는 절차. 관련 작업 로그: `E:\dev\front\did_emart\2026-09-13.md` 참고.

## 사전 준비

1. **백엔드 재시작**
   `CultureDisInfoManageController`(신규 `device/preview.do`)와 `SecurityConfig`(화이트리스트
   등록)를 수정했는데, 이 프로젝트엔 spring-boot-devtools가 없어 핫리로드가 안 된다. 지금 켜져
   있는 백엔드(포트 7004)를 반드시 재시작해야 변경사항이 반영됨.
2. **프론트 실행** (안 켜져 있다면)
   `front` 폴더에서 `npm run dev` → `http://localhost:3003` 접속

## 테스트 절차

1. `http://localhost:3003/login`에서 관리자 계정으로 로그인
2. 좌측 메뉴 **문화센터(MHS) 룸 관리 → 모니터 관리** 탭 진입
3. **실제로 편성(강의 연결)이 돼 있는 모니터**를 하나 고른다 — 비어있는 강의실보다 데이터가
   있는 걸 골라야 "현재 진행중/다음 강의" 카드까지 제대로 보인다. 없으면:
   - "강의 관리" 탭에서 강의 하나 등록
   - "편성표" 탭에서 그 강의를 원하는 모니터에 연결
4. 그 행의 **미리보기** 버튼 클릭 → 모달이 뜨는지, 데이터가 맞게 나오는지 확인
5. 모달 하단 **"단말 화면 원본 열기"** 클릭 → 새 창(1920x1080)에서 실제 단말 화면이 뜨는지 확인
6. **핵심 검증 포인트** — 5번에서 뜬 URL(`.../backoffice/sub/roomManage/mhs/device?mhsMonitorcd=...`)을
   복사해서 **시크릿(비공개) 창**에 붙여넣는다.
   - 로그인 없이도 화면이 그대로 뜨면 "인증 불필요" 설정이 제대로 된 것.
   - 같은(로그인된) 창에서 열어보는 것만으로는 인증 없이 되는 건지 구분이 안 되므로 반드시
     시크릿 창(또는 다른 브라우저)으로 확인할 것.
7. 그 화면에서 개발자도구(F12) → Network 탭을 열고 확인:
   - `device/preview.do` 요청이 **200**으로 오는지
   - `Authorization` 헤더 **없이도** 정상 응답이 오는지
8. 1분 정도 기다리며 확인:
   - 화면 우측 상단 시계가 매초 바뀌는지
   - 60초 뒤 `device/preview.do`가 한 번 더 호출되는지(자동 재조회)

## 문제 발생 시

- 6번에서 로그인 화면으로 튕기거나 401이 뜨는 경우:
  - 백엔드를 아직 재시작하지 않았을 가능성 → 재시작 후 재확인
  - `SecurityConfig.AUTH_GET_WHITELIST`에 등록된 경로
    (`/api/backoffice/sub/roomManage/mhs/device/preview.do`)와 실제 요청 경로가 다른지 확인

## 남은 과제 (코드 밖의 작업)

이 페이지를 만들었다고 실제 물리 단말이 자동으로 바뀌지는 않는다. 실제 문화센터 룸 단말은
여전히 레거시 `did.emart.com/backoffice/sub/roomManage/preView.do`를 보고 있으므로, 실제
전환하려면 각 단말 키오스크 브라우저의 홈 URL 자체를
`(did_emart 프론트 도메인)/backoffice/sub/roomManage/mhs/device?mhsMonitorcd=...`로 바꿔야
한다(원격 기기 관리 도구 또는 직접 방문 필요).
