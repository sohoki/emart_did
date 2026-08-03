-- ============================================================================
-- 08_create_missing_sequences.sql
--
-- ✅ 2026-07-27 실행 완료 확인: 13개 시퀀스 전부 DB에 존재, START_VALUE가 아래 스크립트와
--    정확히 일치하고 각 테이블 현재 MAX+1과도 어긋남 없음(is_called=false, 미사용 상태).
--
-- 목적: 마이그레이션 과정에서 누락된 것으로 확인된 PostgreSQL 시퀀스 13개를 생성한다.
--       레거시(Oracle) 코드가 NEXTVAL/SEQ.NEXTVAL로 채번하던 컬럼들인데, did_emart DB에는
--       해당 시퀀스가 존재하지 않아 지금까지는 애플리케이션 레벨(MAX+1)로 임시 대체해왔음.
--       이 스크립트 실행 후에는 각 매퍼가 다시 NEXTVAL(시퀀스)로 채번하도록 코드도 함께 되돌림
--       (MyBatis <selectKey>로 INSERT 전에 NEXTVAL 값을 미리 가져와 사용).
--
-- 주의: START WITH 값은 스크립트 작성 시점(2026-07-27) 기준 각 테이블의 MAX값 + 1로 설정함.
--       실행 시점까지 데이터가 더 쌓였다면, 실행 직전에 아래 MAX 조회를 다시 돌려 START WITH 값을
--       갱신할 것을 권장함(재확인 없이 그대로 실행하면 이론상 PK 충돌 가능성이 있음).
--
--   SELECT MAX(mhs_connseq)  FROM tb_mhsviewconninfo;      -- mhsconn_seq
--   SELECT GREATEST(COALESCE((SELECT MAX(con_seq) FROM tb_content),0),
--                    COALESCE((SELECT MAX(con_seq) FROM tb_contentmutil),0)); -- content_seq
--   SELECT MAX(file_seq)     FROM tb_contentfileinfo;      -- contentfile_seq
--   SELECT MAX(detail_seq)   FROM tb_condetail;             -- condetail_seq
--   SELECT MAX(brod_annseq)  FROM tb_brodanniversary;       -- conanniversary_seq
--   SELECT MAX(xml_seq)      FROM tb_sendmessagetypr;       -- xml_seq
--   SELECT MAX(msg_seq)      FROM tb_messagehistory;        -- msg_seq
--   SELECT MAX(brod_seq)     FROM tb_brodcontentdetail;      -- brodcondetail_seq
--   SELECT MAX(imsi_seq)     FROM tb_brodimsi;               -- brodconimsi_seq
--
-- 향후 계획(사용자 안내): 이 시퀀스들은 추후 각 컬럼을 IDENTITY/SERIAL(자동증가)로 전환할
-- 예정이며, 그때 이 시퀀스 기반 NEXTVAL 채번 코드는 다시 제거/단순화될 것임.
-- ============================================================================

-- 1) MHS 편성(모니터-강의 시간표 연결) 일련번호 — TB_MHSVIEWCONNINFO.MHS_CONNSEQ
CREATE SEQUENCE IF NOT EXISTS mhsconn_seq START WITH 62063 INCREMENT BY 1;

-- 2) 콘텐츠 일련번호 — TB_CONTENT.CON_SEQ / TB_CONTENTMUTIL.CON_SEQ (두 테이블이 채번 공간 공유)
CREATE SEQUENCE IF NOT EXISTS content_seq START WITH 4543 INCREMENT BY 1;

-- 3) 콘텐츠 상세페이지-파일 연결 일련번호 — TB_CONTENTFILEINFO.FILE_SEQ
CREATE SEQUENCE IF NOT EXISTS contentfile_seq START WITH 71871 INCREMENT BY 1;

-- 4) 콘텐츠 상세페이지 일련번호 — TB_CONDETAIL.DETAIL_SEQ
CREATE SEQUENCE IF NOT EXISTS condetail_seq START WITH 4764 INCREMENT BY 1;

-- 5) 방송 기념일 일련번호 — TB_BRODANNIVERSARY.BROD_ANNSEQ
CREATE SEQUENCE IF NOT EXISTS conanniversary_seq START WITH 9477052 INCREMENT BY 1;

-- 6) XML(장비 통신 명령) 일련번호 — TB_SENDMESSAGETYPR.XML_SEQ
CREATE SEQUENCE IF NOT EXISTS xml_seq START WITH 182 INCREMENT BY 1;

-- 7) DID 발송 이력 일련번호 — TB_MESSAGEHISTORY.MSG_SEQ (Oracle 원본 시퀀스명: MSG_SEQ)
CREATE SEQUENCE IF NOT EXISTS msg_seq START WITH 2249643 INCREMENT BY 1;

-- 8) 방송 콘텐츠 상세(편성) 일련번호 — TB_BRODCONTENTDETAIL.BROD_SEQ
--    SELECT MAX(brod_seq) FROM tb_brodcontentdetail;
CREATE SEQUENCE IF NOT EXISTS brodcondetail_seq START WITH 892193 INCREMENT BY 1;

-- 9) 방송 콘텐츠 임시 편성 일련번호 — TB_BRODIMSI.IMSI_SEQ (현재 0건이라 1부터 시작)
--    SELECT MAX(imsi_seq) FROM tb_brodimsi;
CREATE SEQUENCE IF NOT EXISTS brodconimsi_seq START WITH 1 INCREMENT BY 1;

-- 10) 기초 방송 파일 일련번호 — TB_BRODBASICFILE.BASIC_SEQ (현재 0건이라 1부터 시작).
--     원본 SQL이 NEXTVAL을 그대로 쓰고 있어(애플리케이션 코드 변경 불필요) 시퀀스만 생성하면 됨
--     SELECT MAX(basic_seq) FROM tb_brodbasicfile;
CREATE SEQUENCE IF NOT EXISTS basicbrodfile_seq START WITH 1 INCREMENT BY 1;

-- 11) 기초 방송 배포 스케줄 일련번호 — TB_BASICSCHEDULE.BASIC_SCHEDULE_SEQ (원본 시퀀스명 오타
--     그대로 유지: basicbrodsechedule_seq). 원본 SQL이 NEXTVAL을 그대로 쓰고 있어 시퀀스만 생성
--     SELECT MAX(basic_schedule_seq) FROM tb_basicschedule;
CREATE SEQUENCE IF NOT EXISTS basicbrodsechedule_seq START WITH 10172 INCREMENT BY 1;

-- 12) 기초 방송 파일그룹 일련번호 — TB_BASICFILEGROUP.GROUP_SEQ. 원본 SQL이 NEXTVAL을 그대로
--     쓰고 있어 시퀀스만 생성하면 됨
--     SELECT MAX(group_seq) FROM tb_basicfilegroup;
CREATE SEQUENCE IF NOT EXISTS basicgroup_seq START WITH 42 INCREMENT BY 1;

-- 13) 기초 방송 파일 시간간격 일련번호 — TB_BRODBASICFILE_IVL.BROD_FILESEQ. 원본 SQL이
--     NEXTVAL을 그대로 쓰고 있어 시퀀스만 생성하면 됨
--     SELECT MAX(brod_fileseq) FROM tb_brodbasicfile_ivl;
CREATE SEQUENCE IF NOT EXISTS basicbrodintervalfile_seq START WITH 759 INCREMENT BY 1;
