# Stone Notes API

Stone Notes API is the backend service for the Stone Notes application, providing a robust and secure API for managing notes.

## Technologies Used
- Spring Boot 3.4.3
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL
- Mockito & JUnit 5 for testing
- Maven

## Prerequisites
Before running the application, ensure you have the following:
- Java 21 installed
- Maven installed
- Keycloak server running (or another OAuth2 provider)
- PostgreSQL database
- Create an `application-local.properties` file in the `src/main/resources` folder with the following configuration:
  ```properties
  spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/your-realm
  spring.datasource.url=jdbc:postgresql://localhost:5432/stonenotes
  spring.datasource.username=your-username
  spring.datasource.password=your-password
  ```

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