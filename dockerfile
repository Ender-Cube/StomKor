FROM eclipse-temurin:17-jdk-alpine
COPY build/libs/StomKor-*.*.*.jar app.jar
EXPOSE 25565
ENTRYPOINT ["java","-jar","/app.jar"]