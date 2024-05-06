FROM eclipse-temurin:17.0.11_9-jre

COPY demo.jar /

ENTRYPOINT java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /demo.jar

USER 1001