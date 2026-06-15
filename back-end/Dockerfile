# syntax=docker/dockerfile:1
###############################################################################
# managesys 백엔드 (Spring Boot 4.0.1 / Java 17)
# - OS: Ubuntu(Jammy) 기반 Eclipse Temurin 이미지
# - 멀티스테이지: build(JDK) → runtime(JRE)
###############################################################################

############################
# 1) Build stage  (Ubuntu Jammy + JDK 17)
############################
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Gradle 래퍼 + 빌드 스크립트 먼저 복사 (의존성 레이어 캐시 최적화)
#  ※ gradle.properties 는 의도적으로 복사하지 않음.
#     Windows 전용 트러스트스토어(-Djavax.net.ssl.trustStoreType=Windows-ROOT) 설정이 들어 있어
#     Ubuntu JVM 에서는 TLS 핸드셰이크가 깨져 Maven Central 의존성 해석이 실패한다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Windows 체크아웃 호환: CRLF 정규화 + 실행권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 의존성 선다운로드 (소스 변경과 분리하여 캐시) — 에러는 가리지 않고 로그에 노출
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies 2>&1 || true

# 소스 복사 후 부트 JAR 빌드 (테스트 제외)
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

############################
# 2) Runtime stage  (Ubuntu Jammy + JRE 17)
############################
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 비루트 실행 사용자 (gid 0=root 그룹 → named volume 권한 호환)
RUN useradd -r -u 1001 -g root appuser

# 데이터 디렉토리 = named volume 마운트 지점 (사전 생성 후 소유권 부여)
RUN mkdir -p /app/uploads/customer /app/dumps /app/logs \
    && chown -R appuser:root /app

# 부트 JAR 복사 — 정확한 산출물명으로 고정(plain JAR 오선택 방지)
#  rootProject.name=managesys, version=0.0.1-SNAPSHOT → managesys-0.0.1-SNAPSHOT.jar
COPY --from=build /workspace/build/libs/managesys-0.0.1-SNAPSHOT.jar /app/app.jar
RUN chown appuser:root /app/app.jar

USER appuser
EXPOSE 8085

# 컨테이너 메모리 한도 기반 힙 자동 조정
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Duser.timezone=Asia/Seoul"

# TCP 포트 개방 여부로 헬스 판정 (bash /dev/tcp — 추가 패키지 불필요)
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD bash -c 'cat < /dev/null > /dev/tcp/127.0.0.1/8085' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
