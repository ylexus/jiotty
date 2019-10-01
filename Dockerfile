FROM maven:3.6.2-jdk-8 AS build
COPY * /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml clean package