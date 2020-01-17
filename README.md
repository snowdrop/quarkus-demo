## Cool Quarkus demo

- Steps to execute to create the project
```bash
mkdir demo && cd demo
mvn io.quarkus:quarkus-maven-plugin:1.1.1.Final:create \
    -DprojectGroupId=dev.snowdrop.quarkus \
    -DprojectArtifactId=quarkus-project \
    -DclassName="dev.snowdrop.quarkus.HelloResource" \
    -Dpath="/hello"
```
- Move to the project. Explore the Java class created
- Rename `HelloResource` to `HelloApplication`
- Launch it 
```bash
./mvnw compile quarkus:dev:
```
- Our REST endpoint should be exposed at `localhost:8080/hello`. Let's test it with the curl command
```bash
http localhost:8080/hello
```
- Demo about Hot reload ;-)
- Create a new Java Class `HelloService`
```java
@ApplicationScoped
public class HelloService {
    public String politeHello(String name){
        return "Hello Mr/Mrs " + name;
    }
}
```
- Modify the existing `HelloResource.java` class
```java
@Inject
HelloService helloService;
 
@GET
@Produces(MediaType.APPLICATION_JSON)
@Path("/polite/{name}")
public String greeting(@PathParam("name") String name) {
    return helloService.politeHello(name);
}
```
- Next, let's test our new endpoint:
```
$  http localhost:8080/hello/polite/Charles -s solarized
```
- Edit the `application.properties` file
```
greeting=Good morning
```
- After that, we'll modify the HelloService to use our new property
```java

```
- We can easily package the application by running:
```bash
./mvnw package -DskipTests=true
```
- Build it natively (or using docker image)
```bash
./mvnw package -DskipTests=true -Dquarkus.native.container-build=true -Pnative
```
- We can run `./mvnw verify -DskipTests=true -Pnative` to verify that our native artifact was properly constructed
- First, we'll create a docker image:
```bash
docker build -f src/main/docker/Dockerfile.native -t quarkus/quarkus-demo .
```
- Now, we can run the container using:
```bash
docker run -i --rm -p 8080:8080 quarkus/quarkus-demo
```

## Use Spring

- Add dependency of `quarkus-spring-web` to the pom
```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-spring-web</artifactId>
    </dependency>
```

- Add a `@Restcontroller` to the class `HelloApplication`, @RequestMapping("/hello")
- Build and curl
```bash
./mvnw clean compile quarkus:dev     
http -s solarized :8080/hello/Charles                           
HTTP/1.1 404 Not Found
Content-Length: 0
Content-Type: application/json
```