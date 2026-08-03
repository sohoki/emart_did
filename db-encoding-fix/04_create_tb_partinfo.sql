-- ============================================================
-- TB_PARTINFO 신규 생성 + lettnauthorgroupinfo 데이터 이관
-- (원본 lettnauthorgroupinfo는 삭제/rename 하지 않고 그대로 유지, 신규 테이블에 복사만 함)
--
-- ⚠️ 선행 필수: 02_recover_update.sql을 먼저 실행해서 lettnauthorgroupinfo의
--    한글 깨짐을 복구한 뒤 이 스크립트를 실행할 것. (lettnauthorgroupinfo는
--    31행/56셀이 깨짐 대상에 포함되어 있었음 — 먼저 복구 안 하면 깨진 채로 복사됨)
-- ============================================================

BEGIN;

CREATE TABLE tb_partinfo (
    part_id           VARCHAR(20)  PRIMARY KEY,
    part_nm           VARCHAR(60),
    parent_part_id    VARCHAR(20),          -- 최상위는 '0' (basic/backend 컨벤션과 동일, FK 미설정)
    part_order        VARCHAR(10),
    part_useyn        CHAR(1)      DEFAULT 'Y',
    part_dc           VARCHAR(200),
    part_endyn        CHAR(1)      DEFAULT 'N',
    part_end_de       VARCHAR(20),
    part_head_user_id VARCHAR(30),
    part_create_de    VARCHAR(20),          -- YYYYMMDDHHmmss 원본 문자열 그대로 보존
    instt_code        VARCHAR(20),
    part_etc1         VARCHAR(100),         -- lettnauthorgroupinfo.mhsyn(문화센터 여부) 보관용으로 사용
    part_etc2         VARCHAR(100)
);

INSERT INTO tb_partinfo (
    part_id, part_nm, parent_part_id, part_dc, part_create_de, part_useyn, part_etc1, part_endyn
)
SELECT
    trim(group_id),
    group_nm,
    trim(parent_group_id),   -- 최상위 그룹은 이미 '0'으로 저장돼 있어 그대로 이관됨
    group_dc,
    group_creat_de,
    useyn,
    mhsyn,
    'N'
FROM lettnauthorgroupinfo;

-- 검증: lettnauthorgroupinfo 건수와 이관된 건수가 같은지 확인
-- SELECT (SELECT count(*) FROM lettnauthorgroupinfo) AS src, (SELECT count(*) FROM tb_partinfo) AS dst;

COMMIT;
