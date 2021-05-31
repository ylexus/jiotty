FROM maven:3.8-jdk-11 AS build
COPY . /
WORKDIR /
RUN mvn --settings settings.xml -T1C clean deploy -P release-snapshot