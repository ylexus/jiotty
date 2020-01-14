FROM maven:3.6.2-jdk-8 AS build
COPY . /
WORKDIR /
RUN mvn --settings settings.xml clean deploy -P release-snapshot