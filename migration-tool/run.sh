#!/bin/sh
# ==================================================================
#  Oracle(EMARTCMS) -> PostgreSQL 이관 도구 실행 (Linux 서버용)
#  - Java 17 이상 필요
#  - 실행 후 브라우저 : http://<서버IP>:8099  (서버에서만 쓰면 http://localhost:8099)
#  - 접속 정보는 jar 안의 application.yml 에 들어 있음 (아래 export 로 바꿀 수 있음)
# ==================================================================
cd "$(dirname "$0")" || exit 1

# jar 위치 : 스크립트와 같은 폴더(dist) 또는 build/libs (프로젝트 폴더에서 실행한 경우)
JAR="$(pwd)/migration-tool-1.0.0.jar"
[ -f "$JAR" ] || JAR="$(pwd)/build/libs/migration-tool-1.0.0.jar"
if [ ! -f "$JAR" ]; then
  echo "[오류] migration-tool-1.0.0.jar 를 찾지 못했습니다. 이 스크립트와 같은 폴더에 jar 를 두세요."
  exit 1
fi
echo "실행 jar : $JAR"

# 접속 정보를 바꿔야 하면 주석을 풀고 수정
# export MIGRATION_ORACLE_URL='jdbc:oracle:thin:@10.253.32.23:1521:EMTINDB'
# export ORACLE_PASSWORD='...'
# export MIGRATION_POSTGRES_URL='jdbc:postgresql://did-stg-app-rds.ckealtzyno16.ap-northeast-2.rds.amazonaws.com:5432/db_didinfo'
# export PG_PASSWORD='...'

# -Djava.security.egd : Linux 에서 Oracle JDBC 접속 시 /dev/random 대기로 "Connection reset" 이 나는 문제 방지
java -Djava.security.egd=file:/dev/./urandom -jar "$JAR"
