<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [gs-backend](#gs-backend)
  - [Requirements](#requirements)
  - [Installation](#installation)
    - [Mac OS](#mac-os)
      - [Gradle](#gradle)
      - [Java 13/JDK 13](#java-13jdk-13)
      - [Postgres](#postgres)
  - [Setup](#setup)
  - [Running](#running)
  - [Development guide](#development-guide)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# gs-backend
Ground Server for the Platform infrastructure, Spring-ified

## Requirements
- Java 13
- `gradle`
- Python 3 for `pre-commit`
- PostgresSQL

## Installation
### Mac OS
#### Gradle
  - `brew install gradle`

#### Java 13/JDK 13
This assumes you already have some version of Java installed.
  - Go to [OpenJDK](https://jdk.java.net/13/) to download JDK 13.
  - Navigate to where you downloaded the JDK tar, and then run:
      -  `tar -xf <file>.tar.gz`
  - then move the JDK into your JDK folder:
      -  `sudo mv jdk-<...>.jdk/ /Library/Java/JavaVirtualMachines/`
  - run `java --version` to confirm that it is openJDK 13 running


#### Postgres
  - `brew install postgres`

## Setup
1. To install `pre-commit`, run
  - `pip install pre-commit`
  - `pre-commit install` to run `pre-commit` hooks to ensure your commits are nice
2. Setup the database
  - Begin the postgres server:
      * `postgres -D <path-to-postgres-installation>`
  - Create a user
      * `createuser --createdb --pwprompt --superuser --createrole postgres`
  - Enter the Postgres terminal
      * `psql -U postgres`
  - Create the database
      * ```
        CREATE DATABASE groundserver
        WITH ENCODING='UTF8'
        OWNER=postgres
        CONNECTION LIMIT=-1;
        ```
3. Run `./setup` in order to create the dependencies.

## Running
Then run `./run` in order to start up the server on port `9000`!

## Docker
1. If you have never built the ground server image before, or if you have changed the Dockerfile, from the root directory of the project run
  - `docker-compose build`
2. To create a Docker container, then from the root directory of the project run
  -  `docker-compose up`

## Development guide

When creating a new branch, please use the format of `<your-username>/<informative-branch-name>`. Do not commit to master (you shouldn't be able to anyway) and only squash PRs (you also shouldn't be able to do anything else either).
