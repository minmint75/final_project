
# ====== Build stage ======
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Xmx1024m"
ENV MAVEN_ARGS="--no-transfer-progress"
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# ====== Run stage ======
FROM eclipse-temurin:17-jre
WORKDIR /app
# Đặt timezone nếu cần
ENV TZ=Asia/Ho_Chi_Minh
# Copy jar
COPY --from=build /app/target/*.jar app.jar
# Railway sẽ cung cấp PORT; Spring đọc từ env
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
