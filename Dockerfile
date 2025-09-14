FROM maven:3.9.11-amazoncorretto-21@sha256:e2d145396855bc17b8d2778b052c03de10032d6ab384116ec10648d69bbd9240 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.8-alpine@sha256:fda60fd7965970ce7ed7ce789b18418647b56ac6112fc17df006337bdc6355c4

WORKDIR /usr/locale/stream-backend

COPY --from=build /build/target/Stream.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
