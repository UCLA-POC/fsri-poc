FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/file-to-soap-1.0-SNAPSHOT.jar app.jar
EXPOSE 80
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]