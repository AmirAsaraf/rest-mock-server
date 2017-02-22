# rest-mock-server
A generic rest mock to simulate rest server using a simple configuration

### Features
*	Configuration JSON
*	Supports injection of values from the request to responses.
*	Supports wildcards in responses.
*	Supports method, body, headers, response codes.
*	Supports multiple requests forwarding
*   Supports in memory persistence
*   Supports intercepting response using customized code

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