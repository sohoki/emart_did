-- ============================================================
-- 2단계: 공통코드 3테이블 rename + 컬럼 추가 + PK 추가
-- (1단계 02_recover_update.sql로 한글 복구를 먼저 실행한 뒤 이 스크립트를 실행할 것)
-- 실행 전 반드시 01_backup_tables.sql로 백업된 상태인지 확인
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 0) 중복행 제거 (cl_code PK 추가를 막는 완전 중복 row 정리)
--    EMT, LET 각각 내용이 100% 동일한 행이 2개씩 존재함(마이그레이션 중복 추정).
--    ctid 기준으로 각 cl_code 그룹의 첫 번째만 남기고 나머지 삭제.
-- ------------------------------------------------------------
DELETE FROM lettccmmnclcode a
WHERE a.ctid NOT IN (
    SELECT min(b.ctid) FROM lettccmmnclcode b WHERE b.cl_code = a.cl_code GROUP BY b.cl_code
)
AND a.cl_code IN (
    SELECT cl_code FROM lettccmmnclcode GROUP BY cl_code HAVING count(*) > 1
);
-- 실행 후 남은 행 수가 2건(EMT 1, LET 1)인지 확인:
-- SELECT cl_code, count(*) FROM lettccmmnclcode GROUP BY cl_code;

-- ------------------------------------------------------------
-- 1) COMTCCMMNDETAILCODE에 누락된 컬럼 추가 (JPA 엔티티/매퍼가 요구)
-- ------------------------------------------------------------
ALTER TABLE lettccmmndetailcode ADD COLUMN IF NOT EXISTS code_etc1 VARCHAR(100);
ALTER TABLE lettccmmndetailcode ADD COLUMN IF NOT EXISTS code_etc2 VARCHAR(100);

-- ------------------------------------------------------------
-- 2) 테이블명 변경 (unquoted 사용 → Postgres가 자동으로 소문자로 접어서 저장.
--    매퍼/JPA도 unquoted 참조라 동일하게 소문자로 접혀 매칭되므로 문제 없음)
-- ------------------------------------------------------------
ALTER TABLE lettccmmnclcode     RENAME TO comtccmmnclcode;
ALTER TABLE lettccmmncode       RENAME TO comtccmmncode;
ALTER TABLE lettccmmndetailcode RENAME TO comtccmmndetailcode;

-- ------------------------------------------------------------
-- 3) PK 추가
-- ------------------------------------------------------------
ALTER TABLE comtccmmnclcode     ADD CONSTRAINT PK_COMTCCMMNCLCODE     PRIMARY KEY (cl_code);
ALTER TABLE comtccmmncode       ADD CONSTRAINT PK_COMTCCMMNCODE       PRIMARY KEY (code_id);
ALTER TABLE comtccmmndetailcode ADD CONSTRAINT PK_COMTCCMMNDETAILCODE PRIMARY KEY (code);

-- ------------------------------------------------------------
-- 4) (참고) FK — 원한다면 추가. 매퍼가 삭제 시 수동으로 자식 행을 먼저 지우므로
--    FK 없이도 애플리케이션은 동작하지만, 데이터 무결성을 위해 권장.
--    필요 시에만 주석 해제해서 사용.
-- ------------------------------------------------------------
-- ALTER TABLE comtccmmncode       ADD CONSTRAINT FK_COMTCCMMNCODE_CLCODE       FOREIGN KEY (cl_code)  REFERENCES comtccmmnclcode(cl_code);
-- ALTER TABLE comtccmmndetailcode ADD CONSTRAINT FK_COMTCCMMNDETAILCODE_CODEID FOREIGN KEY (code_id)  REFERENCES comtccmmncode(code_id);

-- ------------------------------------------------------------
-- 5) cl_code 컬럼 폭 — JPA 엔티티(Comtccmmncode.clCode)는 CHAR(7)을 기대하는데
--    실제 데이터/컬럼은 CHAR(3)뿐임. 값 손실 없이 넓히는 것은 안전하므로 반영.
--    (반대로 엔티티 쪽을 CHAR(3)으로 좁히고 싶다면 이 구문은 실행하지 말 것)
-- ------------------------------------------------------------
ALTER TABLE comtccmmncode ALTER COLUMN cl_code TYPE CHAR(7);

COMMIT;

-- ============================================================
-- 보류: FN_DETAIL_CODEID(codeId) 함수는 아직 생성하지 않았음.
-- 이유: 실제 CODE 값 포맷이 일관되지 않음 (예: STATE_01/02/03 은 언더스코어+2자리,
--       반면 DIDTYPE01/REGC07/USR03/DIDRES00 은 언더스코어 없이 바로 숫자).
-- 어떤 채번 규칙을 쓸지 확정되면 별도 스크립트(04_fn_detail_codeid.sql)로 생성 예정.
-- ============================================================
