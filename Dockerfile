FROM maven:3.8-jdk-11 AS build
COPY . /
WORKDIR /
RUN mvn -T1C clean verify