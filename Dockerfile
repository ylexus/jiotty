FROM maven:3.6.2-jdk-8 AS build
COPY * /usr/src/app/
WORKDIR /usr/src/app
RUN mvn -T1C clean package