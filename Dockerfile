FROM eclipse-temurin:8-jre-alpine
VOLUME /tmp
COPY *.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
