FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
WORKDIR /app

COPY *.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
