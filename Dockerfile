#FROM postgres:latest

#ENV POSTGRES_USER postgres
#COPY docker/database.sql /docker-entrypoint-initdb.d/

#EXPOSE 5432
#CMD ["postgres"]


FROM openjdk:13
VOLUME /tmp
EXPOSE 9000
COPY /build/libs/ground-server-0.0.1-SNAPSHOT.jar /home/
CMD ["java","-jar","/home/ground-server-0.0.1-SNAPSHOT.jar"]
