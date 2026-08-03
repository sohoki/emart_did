-- ============================================================
-- TB_ROLEINFO(이미 생성됨, 0건) PK 추가 + lettnauthorinfo 데이터 이관
-- (원본 lettnauthorinfo는 삭제/rename 하지 않고 그대로 유지, 신규 테이블에 복사만 함)
--
-- ⚠️ 선행 필수: 02_recover_update.sql 실행(lettnauthorinfo 9행/18셀 한글 복구:
--    author_nm 9건 + author_dc 9건)을 먼저 실행한 뒤 이 스크립트를 실행할 것.
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1) PK — 이미 role_id에 PK(tb_roleinfo_pkey)가 걸려 있는 것을 확인함(생략)
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- 2) lettnauthorinfo → tb_roleinfo 데이터 이관 (9건)
--    ⚠️ ROLE_USER_GUBUN: lettnauthorinfo에 대응 컬럼이 없어 NULL로 채움.
--       이 값은 원래 공통코드(COMTCCMMNDETAILCODE) 값이어야 하므로,
--       분류 규칙이 정해지면 별도 UPDATE로 채워 넣을 것.
-- ------------------------------------------------------------
INSERT INTO tb_roleinfo (
    role_id, role_name, role_dc, role_useyn, role_user_gubun,
    frst_regist_pnttm, frst_register_id, last_updt_pnttm, last_updusr_id
)
SELECT
    trim(author_code),
    author_nm,
    author_dc,
    'Y',
    NULL,
    NULLIF(trim(author_creat_de), '')::timestamp,
    'SYSTEM',
    NOW(),
    'SYSTEM'
FROM lettnauthorinfo;

-- 검증:
-- SELECT (SELECT count(*) FROM lettnauthorinfo) AS src, (SELECT count(*) FROM tb_roleinfo) AS dst;
-- SELECT * FROM tb_roleinfo ORDER BY role_id;

COMMIT;
