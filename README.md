# Stone Notes API

Stone Notes API is the backend service for the Stone Notes application, providing a robust and secure API for managing notes.

## Technologies Used
- Spring Boot
- Mockito
- JUnit
- Spring Security (for authentication and authorization)
- Maven

## Prerequisites
Before running the application, ensure you have the following:
- Java 21 installed.
- Maven installed.
- Create an `application-local.properties` file in the `src/main/resources` folder and define an environment variable named `jwt.secret`. It should have a value of at least 32 characters.
  Before running the application, ensure you have the following:

## Setting Up the Project
Build the project:
   ```sh
   mvn clean install
   ```

## Running the Application
To start the application, use:
```sh
mvn spring-boot:run
```

## Running Tests
To execute tests, run:
```sh
mvn test
```