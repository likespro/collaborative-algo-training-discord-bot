FROM gradle:jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle build --no-daemon

FROM amazoncorretto:21-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*-fat-*.jar app.jar

CMD ["java", "-jar", "app.jar"]
