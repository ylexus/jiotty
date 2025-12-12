FROM maven:3.9.11-amazoncorretto-25 AS build
COPY . /
WORKDIR /
RUN mvn --settings settings.xml clean deploy -P release-snapshot