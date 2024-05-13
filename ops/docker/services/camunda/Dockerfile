FROM eclipse-temurin:17.0.11_9-jre

COPY camunda.jar /

ENTRYPOINT java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /camunda.jar

USER 1001