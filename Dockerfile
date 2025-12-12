FROM maven:3.9.11-amazoncorretto-25 AS build
COPY . /
WORKDIR /
RUN mvn -T1C clean verify