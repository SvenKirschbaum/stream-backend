FROM maven:3.9.9-amazoncorretto-21@sha256:662de5fb5a54e33aa9360a50e7a2adf7543add66dd060c234a3cbc929efbadbc as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.6-alpine@sha256:1b53a05c5693b5452a0c41a39b1fa3b8e7d77aa37f325acc378b7928bc1d8253

WORKDIR /usr/locale/stream-backend

COPY --from=build /build/target/Stream.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
