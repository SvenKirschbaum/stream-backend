FROM maven:3.9.6-amazoncorretto-21@sha256:d9b242046b9e8f8a8d80748905812b934a3635f19469fd8a79b58352d71a4a7c as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.2-alpine@sha256:ecdb53d62a45cb978b849e30ebcc16933ad5c2a7659b94f0c556b93fe575cda9

WORKDIR /usr/locale/stream-backend

COPY --from=build /build/target/Stream.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
