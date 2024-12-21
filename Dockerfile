FROM maven:3.9.9-amazoncorretto-21@sha256:17ae7b5533254592b8ab1a159cdb63777a692eab49754b708711854c0a68d6a4 as build

WORKDIR /build

COPY pom.xml .
COPY src src

RUN mvn package

FROM amazoncorretto:21.0.5-alpine@sha256:8b16834e7fabfc62d4c8faa22de5df97f99627f148058d52718054aaa4ea3674

WORKDIR /usr/locale/stream-backend

COPY --from=build /build/target/Stream.jar backend.jar

ENTRYPOINT ["java", "-jar", "backend.jar"]
