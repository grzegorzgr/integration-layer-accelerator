FROM eclipse-temurin:17.0.11_9-jre
#FROM kainoscom/jdk-17-apm:latest

COPY api-gateway.jar /

ENTRYPOINT java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar /api-gateway.jar

USER 1001