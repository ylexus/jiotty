FROM maven:3.8-jdk-11 AS build
COPY . /
WORKDIR /
RUN mvn --settings settings.xml clean deploy -P release-snapshot