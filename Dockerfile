FROM openjdk:13
COPY /build/libs/ground-server-0.0.1-SNAPSHOT.jar /home/
CMD ["java","-jar","/home/ground-server-0.0.1-SNAPSHOT.jar"]
