@echo off
rem ==================================================================
rem  Oracle(EMARTCMS) -> PostgreSQL 이관 도구 실행 (Windows 용)
rem  - Java 17 이상 필요
rem  - 실행 후 브라우저 : http://localhost:8099
rem  - 접속 정보는 jar 안의 application.yml 에 들어 있음 (아래 set 으로 바꿀 수 있음)
rem ==================================================================
setlocal
chcp 65001 > nul
cd /d "%~dp0"

rem jar 위치 : 스크립트와 같은 폴더(dist) 또는 build\libs (프로젝트 폴더에서 실행한 경우)
set "JAR=%~dp0migration-tool-1.0.0.jar"
if not exist "%JAR%" set "JAR=%~dp0build\libs\migration-tool-1.0.0.jar"
if not exist "%JAR%" (
    echo [오류] migration-tool-1.0.0.jar 를 찾지 못했습니다.
    echo        이 스크립트와 같은 폴더에 jar 를 두거나, 프로젝트 폴더에서 gradlew.bat bootJar 로 빌드하세요.
    pause
    exit /b 1
)
echo 실행 jar : %JAR%

rem 접속 정보를 바꿔야 하면 주석을 풀고 수정
rem set MIGRATION_ORACLE_URL=jdbc:oracle:thin:@10.253.32.23:1521:EMTINDB
rem set ORACLE_PASSWORD=...
rem set MIGRATION_POSTGRES_URL=jdbc:postgresql://did-stg-app-rds.ckealtzyno16.ap-northeast-2.rds.amazonaws.com:5432/db_didinfo
rem set PG_PASSWORD=...

rem Java : 같이 넣은 jre 폴더가 있으면 그것을 사용 (PC 에 Java 설치 불필요), 없으면 PC 의 java
set "JAVA=java"
if exist "%~dp0jre\bin\java.exe" set "JAVA=%~dp0jre\bin\java.exe"
echo 실행 java : %JAVA%
echo 브라우저에서 http://localhost:8099 접속 (이 창을 닫으면 종료)

"%JAVA%" -jar "%JAR%"
pause
