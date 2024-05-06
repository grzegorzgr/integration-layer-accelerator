FROM eclipse-temurin:17.0.11_9-jre

COPY error-handling.jar /

ENTRYPOINT java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /error-handling.jar

USER 1001