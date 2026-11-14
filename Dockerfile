FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# New Relic agent
RUN mkdir -p /app/newrelic
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-agent.jar /app/newrelic/newrelic.jar
COPY newrelic.yml /app/newrelic/newrelic.yml

COPY --from=build /app/target/os-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-javaagent:/app/newrelic/newrelic.jar", \
  "-jar", "app.jar"]
