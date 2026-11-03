// 문화센터(MHS) 계정 공통 상수 — 로그인 후 이동 경로와 룸 관리 화면의 지점 고정 판단에 사용
export const MHS_ROLES = ['ROLE_MHS_ADMIN', 'ROLE_MHS_USER'];

export const MHS_LANDING_PATH = '/backoffice/sub/roomManage/mhs?menuNo=600001';

export const isMhsRole = (roleId) => MHS_ROLES.includes(roleId);
