FROM amazoncorretto:17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew clean build -x test --no-daemon


FROM amazoncorretto:17-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/build/libs/discodeit-*.jar app.jar

ENV JVM_OPTS="" \
    SERVER_PORT=80

EXPOSE 80

CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]