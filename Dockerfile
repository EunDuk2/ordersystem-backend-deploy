FROM openjdk:17-jdk-slim as stage1

WORKDIR /app

# 폴더명은 폴더 폴더 로 현재 위치에 복사
# 파일은 .으로 현재 위치에 복사
COPY gradle gradle
COPY src src
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .

RUN chmod +x gradlew
RUN ./gradlew bootJar

# ENTRYPOINT [ "java", "-jar", "/app/build/libs/*.jar" ]


# 두번째 스테이지
# 이미지 경량화를 위해 스테이지 분리
FROM openjdk:17-jdk-slim as stage2
WORKDIR /app
# stage1의 jar파일을 stage2로 copy
COPY --from=stage1 /app/build/libs/*.jar app.jar

# 실행 : CMD 또는 ENTRYPOINT를 통해 컨테이너 실행
ENTRYPOINT [ "java", "-jar", "app.jar" ]

# 도커이미지 빌드
# docker build -t ordersystem:v1.0 .

# 도커컨테이너 실행
# docker 내부에서 localhost를 찾는 설정은 루프백 문제 발생
# docker run --name my-ordersystem -d -p 8080:8080 ordersystem:v1.0

# 도커컨테이너 실행 (컨테이너 내의 로컬호스트말고, 실제 로컬호스트를 거쳐 외부 컨테이너에 접속)
# 도커컨테이너 실행시점에 docker.host.internal을 환경변수로 주입
# docker run --name my-ordersystem -d -p 8080:8080 -e SPRING_REDIS_HOST=host.docker.internal -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem ordersystem:v1.0