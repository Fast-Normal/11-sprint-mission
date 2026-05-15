FROM amazoncorretto:17@sha256:049b43c80eb657fe5820922dbcf48458e85a7c023257f5dc394066645554b54a

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test

EXPOSE 80

ENV PROJECT_NAME=discodeit \
    PROJECT_VERSION=1.2-M8 \
    JVM_OPTS="" \
    SERVER_PORT=80

CMD ["/bin/sh", "-c", "java $JVM_OPTS -jar build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar"]