<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [gs-backend](#gs-backend)
  - [Requirements](#requirements)
  - [Installation](#installation)
    - [Mac OS](#mac-os)
      - [Gradle](#gradle)
      - [Java 11/JDK 11](#java-11jdk-11)
      - [Postgres](#postgres)
  - [Setup](#setup)
  - [DISCLAIMER](#disclaimer)
  - [Docker](#docker)
  - [Setup (Native)](#setup-native)
  - [Development guide](#development-guide)
  - [Running (Native)](#running-native)
  - [Testing](#testing)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# gs-backend
Ground Server for the Platform infrastructure, Spring-ified

## Requirements
- Java 11
- `gradle`
- Python 3 for `pre-commit`
- PostgresSQL
- IntelliJ Ultimate

## Installation
### Mac OS
#### Gradle
  - `brew install gradle`

#### Java 11/JDK 11
This assumes you already have some version of Java installed.
  - Go to [OpenJDK](https://jdk.java.net/archive/) to download JDK 11 (Download `11.0.2 (build 11.0.2+9)`).
  - Navigate to where you downloaded the JDK tar, and then run:
      -  `tar -xf <file>.tar.gz`
  - then move the JDK into your JDK folder:
      -  `sudo mv jdk-<...>.jdk/ /Library/Java/JavaVirtualMachines/`
  - run `java --version` to confirm that it is openJDK 11 running


#### Postgres
  - `brew install postgres`

## Setup
1. To install `pre-commit`, run
  - `pip install pre-commit`
  - `pre-commit install` to run `pre-commit` hooks to ensure your commits are nice
2. Run `./setup` in order to create the dependencies.
3. See [IntelliJ Setup](docs/intellij_setup.md).

## DISCLAIMER
Certain properties are defaulted to allow for native development. If you would like to develop using Docker, read the next section. Details for native setup follow.

## Setup (Docker)
1. Ensure that the following four files have the following four lines changed to below:
  - `application.properties`
      * `spring.datasource.url=jdbc:postgresql://db:5432/groundserver`
  - `ebean.properties`
      * `datasource.db.databaseUrl=jdbc:postgresql://db:5432/groundserver`
  - `test-application.properties`
      * `spring.datasource.url=jdbc:postgresql://db:5432/groundservertest`
  - `test-ebean.properties`
      * `datasource.db.databaseUrl=jdbc:postgresql://db:5432/groundservertest`
2. If you have never built the ground server image before, or if you have changed the Dockerfile, from the root directory of the project run
  - `docker-compose build`
3. To create a Docker container, then from the root directory of the project run
  -  `docker-compose up`

## Setup (Native)
1. Setup the database
  - Begin the postgres server:
      * `postgres -D <path-to-postgres-installation>`
  - Create a user
      * `createuser --createdb --pwprompt --superuser --createrole postgres`
      * Note for Linux users: The password must be set to `admin`
  - Enter the Postgres terminal
      * `psql -U postgres`
  - Create the main database
      * ```
        CREATE DATABASE groundserver
        WITH ENCODING='UTF8'
        OWNER=postgres
        CONNECTION LIMIT=-1;
        ```
  - Create the test database
      * ```
        CREATE DATABASE groundservertest
        WITH ENCODING='UTF8'
        OWNER=postgres
        CONNECTION LIMIT=-1;
        ```

## Development guide

When creating a new branch, please use the format of `<your-username>/<informative-branch-name>`. Do not commit to master (you shouldn't be able to anyway) and only squash PRs (you also shouldn't be able to do anything else either).

## Running (Native)
Ensure that the following four files have the following four lines changed to below:
  - `application.properties`
      * `spring.datasource.url=jdbc:postgresql:groundserver`
  - `ebean.properties`
      * `datasource.db.databaseUrl=jdbc:postgresql:groundserver`
  - `test-application.properties`
      * `spring.datasource.url=jdbc:postgresql:groundservertest`
  - `test-ebean.properties`
      * `datasource.db.databaseUrl=jdbc:postgresql:groundservertest`

Then run `./run` in order to start up the server on port `9000`!

## Testing
To enable logging while testing, run `gradle test` with the `--info` or `-i` flag if you are logging at the `INFO` level, or `--debug` or `d` if you are logging at the `DEBUG` level, etc. `System.out.println` will print out on the `INFO` level.
