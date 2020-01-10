FROM gradle:jdk13
WORKDIR /home/gs-backend
CMD ["sh", "-c", "gradle build --stacktrace ; java -jar build/libs/ground-server-0.0.1-SNAPSHOT.jar"]
