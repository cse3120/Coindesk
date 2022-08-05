FROM maven:3.5.4-jdk-8-alpine as maven
COPY ./pom.xml ./pom.xml
COPY ./src ./src
RUN mvn dependency:go-offline -B
RUN mvn package
FROM java:8-jdk-alpine
WORKDIR /
COPY --from=maven target/CoinDesk-1.0-SNAPSHOT.jar ./CoinDesk-1.0-SNAPSHOT.jar
CMD ["java", "-jar", "./CoinDesk-1.0-SNAPSHOT.jar"]