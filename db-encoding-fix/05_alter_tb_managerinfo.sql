-- ============================================================
-- TB_MANAGERINFO(이미 생성됨, 0건) 컬럼 추가 + PK/FK 추가 + lettngnrlmber 데이터 이관
-- (원본 lettngnrlmber는 삭제/rename 하지 않고 그대로 유지, 신규 테이블에 복사만 함)
--
-- ⚠️ 선행 필수:
--   1) 02_recover_update.sql 실행(lettngnrlmber 56행/56셀 한글 복구)
--   2) 04_create_tb_partinfo.sql 실행(PART_ID FK 대상 테이블 먼저 존재해야 함)
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1) 컬럼 추가: 매장/센터 스코프
--    lettngnrlmber.center_id 실데이터로 검증 완료 — tb_centerinfo.center_id 참조.
--    NULL이면 "전체 매장 접근"(ROLE_ADMIN류)을 의미.
-- ------------------------------------------------------------
ALTER TABLE tb_managerinfo ADD COLUMN IF NOT EXISTS center_id VARCHAR(9);
-- 참고: lettngnrlmber.center_id 중 4건이 tb_centerinfo와 불일치(공백/오타 추정)했으므로
-- 강한 FK 대신 컬럼만 추가함. 정합성 정리 후 필요하면 FK 추가:
-- ALTER TABLE tb_managerinfo ADD CONSTRAINT FK_TB_MANAGERINFO_CENTERID FOREIGN KEY (center_id) REFERENCES tb_centerinfo(center_id);

-- ------------------------------------------------------------
-- 2) PK 추가 (현재 0건이라 바로 추가 가능)
-- ------------------------------------------------------------
ALTER TABLE tb_managerinfo ADD CONSTRAINT PK_TB_MANAGERINFO PRIMARY KEY (manager_id);

-- ------------------------------------------------------------
-- 3) PART_ID FK 추가 (TB_PARTINFO가 먼저 만들어져 있어야 함)
-- ------------------------------------------------------------
ALTER TABLE tb_managerinfo ADD CONSTRAINT FK_TB_MANAGERINFO_PARTID FOREIGN KEY (part_id) REFERENCES tb_partinfo(part_id);

-- ------------------------------------------------------------
-- 4) lettngnrlmber → tb_managerinfo 데이터 이관
--    ⚠️ manager_password는 lettngnrlmber.password(평문)를 그대로 복사함.
--       신규 시스템(Spring Security)에 붙이기 전 반드시 별도 배치로 재해시(bcrypt 등) 처리할 것.
--       role_code는 실사용 0건 확인되어 이관하지 않음(tb_authorrolerelate 포함 사용 안 함).
-- ------------------------------------------------------------
INSERT INTO tb_managerinfo (
    manager_id, manager_password, manager_name, use_yn, part_id,
    manager_email, manager_tel, manager_status, role_id, center_id,
    password_hint, password_cnsr, frst_regist_pnttm, lock_yn, dltn_yn,
    frst_register_id, last_updusr_id, last_updt_pnttm
)
SELECT
    trim(mber_id),
    password,
    mber_nm,
    'Y',
    NULLIF(trim(group_id), ''),
    mber_email_adres,
    mbtlnum,
    mber_sttus,
    author_code,
    NULLIF(trim(center_id), ''),
    password_hint,
    password_cnsr,
    sbscrb_de,
    'N',
    'N',
    'SYSTEM',
    'SYSTEM',
    NOW()
FROM lettngnrlmber;

-- 검증:
-- SELECT (SELECT count(*) FROM lettngnrlmber) AS src, (SELECT count(*) FROM tb_managerinfo) AS dst;
-- SELECT manager_id, part_id FROM tb_managerinfo WHERE part_id IS NOT NULL
--   AND part_id NOT IN (SELECT part_id FROM tb_partinfo); -- FK 위반이면 여기서 이미 걸러짐(트랜잭션 롤백)

COMMIT;
