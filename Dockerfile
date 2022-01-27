FROM openjdk:8-jdk-alpine

COPY target/scylla-1.0.jar scylla.jar
ENTRYPOINT ["java","-jar","/scylla.jar"]