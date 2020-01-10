#FROM openjdk:13
FROM gradle:jdk13 as builder
COPY --chown=gradle:gradle . /home/gradle/src
#WORKDIR /home/gradle/src
#RUN gradle build --stacktrace

CMD ["gradle","build","--stacktrace"]
CMD ["echo hello there"]
#WORKDIR /build/libs
CMD ["java","-jar","build/libs/ground-server-0.0.1-SNAPSHOT.jar"]

#FROM openjdk:13
#CMD ["./run"]
