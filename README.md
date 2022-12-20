# Full Stack Review System

## Introduction
Developed a fully tested book review application where users can add new books and write or see reviews posted by different users. The main
objective of the project is to learn best practices for setting up the necessary architecture to perform unit, integration and end-to-end tests.


## Course Application Architecture

To mirror a typical microservice architecture, the demo application uses the following tech stack:

- Keycloak (open source identity and access management solution) to secure parts of the frontend and backend
- Amazon SQS (Simple Queuing Service) for asynchronous message processing
- PostgreSQL (RDBMS) to store data in a relational database
- Single Page Application Frontend with React and TypeScript
- Spring Boot backend with Java
- Dependency on a remote REST API

<p align="center">
  <a href="https://rieckpil.de/testing-spring-boot-applications-masterclass/">
    <img src="https://rieckpil.de/wp-content/uploads/2021/11/book-reviewr-application-architecture-750x666-1.png" alt="Testing Spring Boot Applications Technical Architecture">
  </a>
</p>

# Local Project Setup

## Requirements

Mandatory requirements:

* Java 17 (JDK flavour (OpenJDK/Azul/Oracle) does not matter).

```
$ java -version
openjdk version "17" 2021-09-14 LTS
OpenJDK Runtime Environment Zulu17.28+13-CA (build 17+35-LTS)
OpenJDK 64-Bit Server VM Zulu17.28+13-CA (build 17+35-LTS, mixed mode, sharing)
```

* Docker Engine (Community Edition is enough) and Docker Compose:

```
$ docker version
Client: Docker Engine - Community
 Version:           20.10.6
 API version:       1.41
 Go version:        go1.13.15
 Git commit:        370c289
 Built:             Fri Apr  9 22:47:17 2021
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          20.10.6
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.13.15
  Git commit:       8728dd2
  Built:            Fri Apr  9 22:45:28 2021
  OS/Arch:          linux/amd64
  Experimental:     false

$ docker-compose version
docker-compose version 1.26.2, build eefe0d31
docker-py version: 4.2.2
CPython version: 3.7.7
OpenSSL version: OpenSSL 1.1.1g  21 Apr 2020
```

Optional requirements:

* Maven >= 3.6 (the project also includes the Maven Wrapper).

When using Maven from the command line, make sure `./mvnw -version` reports the correct Java version:

```
$ ./mvnw -version

Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Maven home: /home/rieckpil/.m2/wrapper/dists/apache-maven-3.8.4-bin/52ccbt68d252mdldqsfsn03jlf/apache-maven-3.8.4
Java version: 17.0.1, vendor: Eclipse Adoptium, runtime: /usr/lib/jvm/jdk-17.0.1+12
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.4.0-92-generic", arch: "amd64", family: "unix"
```

* IntelliJ IDEA or any IDE/Code Editor (Eclipse, NetBeans, Code, Atom, etc.)

## Running the Project Locally

Assuming your local setups meets all requirements as stated above, you can now start the application:

1. Make sure your Docker Engine is up- and running
2. Start the required infrastructure components with `docker-compose up`
3. Run the application with `./mvnw spring-boot:run` or inside your IDE
4. Access http://localhost:8080 for the application frontend
5. (Optional) Access http://localhost:8888 for the Keycloak Admin interface

Valid application users:

* duke (password `dukeduke`)
* mike (password `mikemike`)

## Running the Tests

_Replace `./mvnw` with `mvnw.cmd` if you're running on Windows._

Run all **unit** tests (Maven Surefire Plugin): `./mvnw test`

Run all **integration & web** tests (Maven Failsafe plugin):

1. Make sure no conflicting Docker containers are currently running: `docker ps`
2. Make sure the test classes have been compiled and the frontend has been build and is part of the `target/classes/public` folder: `./mvnw package -DskipTest`
3. Execute `./mvnw failsafe:integration-test failsafe:verify`

Run **all tests** together:

1. Make sure no conflicting Docker container is currently running: `docker ps`
2. Execute `./mvnw verify`

Skip all tests (don't do this at home):

1. Execute `./mvnw -DskipTests=true verify`