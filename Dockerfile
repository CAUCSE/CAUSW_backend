FROM openjdk:11-jre-slim

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]

EXPOSE 8080