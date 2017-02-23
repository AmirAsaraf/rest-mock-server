# REST-mock-server
A generic rest mock to simulate rest server using a simple configuration

### Features
*	JSON Configuration
*	Supports injection of values from the request to responses.
*	Supports wildcards in responses.
*	Supports method, body, headers, response codes.
*	Supports multiple requests forwarding
*   Supports in memory persistence
*   Supports intercepting response using customized code

## Getting Started
1. Set **config.properties** server port and service configuration location
2. Start from examples/**simple.json** to test the configured services.
3. From any REST client (example : https://www.getpostman.com) try to GET http://localhost:6060/simple1 you should get the following response:
```
{ "message" : "response1 OK!" }
```

    

## Project's CLI Reference

### Service

````bash
$ java -jar ./target/mock-rest-server-1.0.jar
````

### Maven Tasks
Build
````bash
$ mvn clean install
````

Create Executable JAR container
````bash
$ mvn package
````