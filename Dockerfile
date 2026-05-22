# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Salin script wrapper gradle dan konfigurasi
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Pastikan script gradlew bisa dieksekusi (penting jika di Linux/Alpine)
RUN chmod +x gradlew

# Salin source code
COPY src src

# Jalankan proses build (skip test agar lebih cepat saat deploy)
RUN ./gradlew bootJar -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Salin hasil build dari Stage 1
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]