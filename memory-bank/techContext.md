# 기술 맥락 (Tech Context)

## 스택 요약
| 구분 | 기술 |
|------|------|
| 프론트 | Vue 3, Vue Router 4, Pinia, Vue CLI 5, AG Grid, Axios, Lucide Vue |
| 백엔드 | Java 17, Spring Boot 4.0.1, Spring Security, MyBatis, JWT (jjwt) |
| DB | MariaDB, H2(개발용), HikariCP |
| 빌드 | npm (front-end), Gradle (back-end) |

## 실행 방법
- **백엔드**: `back-end/`에서 `./gradlew bootRun` (또는 IDE에서 ManagesysApplication 실행). 포트 8085.
- **프론트**: `front-end/`에서 `npm install` 후 `npm run serve`.
- **DB**: MariaDB 3307, DB명 `managesys`, 사용자 root/1234 (application.properties 기준).

## 주요 경로
- 백엔드 설정: `back-end/src/main/resources/application.properties`
- 매퍼 XML: `back-end/src/main/resources/mapper/`
- 라우터: `front-end/src/router/index.js`
- 스토어: `front-end/src/store/auth.js`

## MariaDB MCP (Cursor)
- **서버 이름**: `project-0-managesys-mariadb` (프로젝트 전용). 도구: `mariadb_query`, 인자: `{ "sql": "SELECT ..." }`
- **사용**: 로그인/로그아웃 이력은 **TB_USER_LOGIN_HISTORY** 테이블 사용 (EVENT_TYPE, USER_ID, IP_ADDRESS 등). 테이블/데이터 확인 시 위 MCP로 쿼리 실행
