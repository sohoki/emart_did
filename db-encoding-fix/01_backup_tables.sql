-- 02_recover_update.sql 실행 전, 영향받는 24개 테이블을 통째로 백업합니다.
-- (PK가 없어 ctid 기반 UPDATE를 쓰므로, 문제 발생 시 이 백업 테이블로 복원하세요)
CREATE TABLE "lettccmmnclcode_bak_20260726" AS SELECT * FROM "lettccmmnclcode";
CREATE TABLE "lettccmmncode_bak_20260726" AS SELECT * FROM "lettccmmncode";
CREATE TABLE "lettccmmndetailcode_bak_20260726" AS SELECT * FROM "lettccmmndetailcode";
CREATE TABLE "lettnauthorgroupinfo_bak_20260726" AS SELECT * FROM "lettnauthorgroupinfo";
CREATE TABLE "lettnauthorinfo_bak_20260726" AS SELECT * FROM "lettnauthorinfo";
CREATE TABLE "lettnfiledetail_bak_20260726" AS SELECT * FROM "lettnfiledetail";
CREATE TABLE "lettngnrlmber_bak_20260726" AS SELECT * FROM "lettngnrlmber";
CREATE TABLE "tb_authorrolerelate_bak_20260726" AS SELECT * FROM "tb_authorrolerelate";
CREATE TABLE "tb_basicfilegroup_bak_20260726" AS SELECT * FROM "tb_basicfilegroup";
CREATE TABLE "tb_brodanniversary_bak_20260726" AS SELECT * FROM "tb_brodanniversary";
CREATE TABLE "tb_brodbasicgroup_bak_20260726" AS SELECT * FROM "tb_brodbasicgroup";
CREATE TABLE "tb_brodschedule_bak_20260726" AS SELECT * FROM "tb_brodschedule";
CREATE TABLE "tb_centerinfo_bak_20260726" AS SELECT * FROM "tb_centerinfo";
CREATE TABLE "tb_contentmutil_bak_20260726" AS SELECT * FROM "tb_contentmutil";
CREATE TABLE "tb_didinfo_bak_20260726" AS SELECT * FROM "tb_didinfo";
CREATE TABLE "tb_didsendmessage_bak_20260726" AS SELECT * FROM "tb_didsendmessage";
CREATE TABLE "tb_group_bak_20260726" AS SELECT * FROM "tb_group";
CREATE TABLE "tb_groupdid_bak_20260726" AS SELECT * FROM "tb_groupdid";
CREATE TABLE "tb_menu_bak_20260726" AS SELECT * FROM "tb_menu";
CREATE TABLE "tb_mhscenterinfo_bak_20260726" AS SELECT * FROM "tb_mhscenterinfo";
CREATE TABLE "tb_mhsclassinfo_bak_20260726" AS SELECT * FROM "tb_mhsclassinfo";
CREATE TABLE "tb_mhsmonitorinfo_bak_20260726" AS SELECT * FROM "tb_mhsmonitorinfo";
CREATE TABLE "tb_schedule_bak_20260726" AS SELECT * FROM "tb_schedule";
CREATE TABLE "tb_sendmessagetypr_bak_20260726" AS SELECT * FROM "tb_sendmessagetypr";
