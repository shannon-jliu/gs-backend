#FROM openjdk:13
FROM gradle:jdk13 as builder
#COPY --chown=gradle:gradle . /home/gradle/src
#WORKDIR /home/gradle/src
WORKDIR /home/gs-backend
#RUN gradle build --stacktrace

#CMD ["gradle","build","--stacktrace",";","java","-jar","build/libs/ground-server-0.0.1-SNAPSHOT.jar"]
CMD ["sh", "-c", "gradle build --stacktrace ; java -jar build/libs/ground-server-0.0.1-SNAPSHOT.jar"]
#CMD ["echo hello there"]
#WORKDIR /build/libs
#CMD ["java","-jar","build/libs/ground-server-0.0.1-SNAPSHOT.jar"]

#FROM openjdk:13
#CMD ["./run"]

#FROM gradle AS build
#RUN gradle build --stacktrace

#FROM openjdk:13
#COPY --from=build groundserver.jar .
#RUN java -jar groundserver.jar

