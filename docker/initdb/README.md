# 초기 DB 적재 (선택)

이 디렉토리(`docker/initdb`)의 `*.sql` / `*.sh` 파일은 **mariadb-data 볼륨이 비어 있는 최초 기동 시 1회만**
알파벳 순으로 자동 실행됩니다. (`/docker-entrypoint-initdb.d` 표준 동작)

## 스키마/데이터 시드 방법

1. 덤프 파일을 이 디렉토리에 복사합니다. 예:
   ```powershell
   Copy-Item ..\..\managesys_dump.sql .\01-schema.sql
   ```
   > 루트의 `managesys_*.sql` 덤프에는 개인정보(PII)가 포함될 수 있으니 취급에 주의하세요.

2. 데이터 볼륨을 초기화한 뒤 다시 기동합니다(기존 데이터가 있으면 init 스크립트는 실행되지 않음):
   ```powershell
   docker compose down -v
   docker compose up -d --build
   ```

## 참고
- 시드를 적재하지 않으면 `managesys` DB는 **빈 스키마**로 생성되어, 앱은 정상 기동되지만
  테이블이 없어 로그인/조회 API는 실패합니다.
- 평문 개인정보를 일괄 암호화하려면 시드 적재 후
  `app.encryption.migrate-on-startup=true`(compose 의 `APP_ENCRYPTION_MIGRATEONSTARTUP`)로 1회 기동하세요.
