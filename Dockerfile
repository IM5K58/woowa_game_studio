FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN sed -i 's/\r$//' gradlew
RUN chmod +x ./gradlew


RUN ./gradlew bootJar -x test


FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]