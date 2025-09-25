FROM maven:3.9.11-amazoncorretto-21@sha256:8bb189a6673bb8b674af267ead6037e2d0508c950daac45c3360e57093476c07 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:25.0.0-alpine@sha256:807ea3c4000a052986cd1e7097a883f9cd7a6e527f73841f462e3d04851b8835

WORKDIR /usr/locale/stream-backend

COPY --from=build /build/target/Stream.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
