FROM eclipse-temurin:17

WORKDIR /app

COPY target/dai-labo-3-1.0-SNAPSHOT.jar /app/dai-labo-3-1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "dai-labo-3-1.0-SNAPSHOT.jar"]

CMD ["--help"]