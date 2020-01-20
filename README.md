## Cool Quarkus demo

- Move within a terminal to the folder where you will play the scenario
```bash
cd /Users/dabou/Temp/quarkus/demo
```
- Steps to execute to create the Quarkus REST project
```bash
mvn io.quarkus:quarkus-maven-plugin:1.1.1.Final:create \
    -DprojectGroupId=dev.snowdrop.quarkus \
    -DprojectArtifactId=quarkus-rest \
    -DclassName="dev.snowdrop.quarkus.HelloApplication" \
    -Dpath="/hello"
```
- Move to the project. Explore the Java class created
- Launch it 
```bash
./mvnw compile quarkus:dev
```
- Our REST endpoint should be exposed at `localhost:8080/hello`. Let's test it with the `curl/httpie` command executed within another terminal
```bash
curl localhost:8080/hello
http -s solarized localhost:8080/hello
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
- Modify the existing `HelloApplication.java` class
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
- After that, we'll modify the `HelloService` class to use our new property
```java
@ConfigProperty(name = "greeting")
private String greeting;
...
public String politeHello(String name){
    return greeting + ", " + name;
}
```
- Fix the issue of the Test class to change the condition to validate
```java
.when().get("/hello/polite/redhat")
.then()
   .statusCode(200)
   .body(is("Good afternoon, redhat"));
```
- We can easily `package` the application by running the following command to generate a uber jar file
```bash
./mvnw package
java -jar ./target/quarkus-rest-1.0-SNAPSHOT-runner.jar
```
- Build it natively (or using docker image) - optional step
```bash
./mvnw package -Dquarkus.native.container-build=true -Pnative
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

## Jump to Spring on Quarkus now

- Add to the pom.xml file, the `Spring Web` dependency, quarkus extension to use Spring CDI and Web starter
```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-spring-web</artifactId>
</dependency>
```
- We will now revisit the code to use Spring `Annotations`
- Comment within the class `HelloApplication`, all the Java REST annotations
```java
//@Path("/hello")
public class HelloApplication {
...    
    /*
    @Inject
    HelloService helloService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/polite/{name}")
    public String greeting(@PathParam("name") String name) {
        return helloService.politeHello(name);
    }
    */
```

- Add the `@RestController`, `@RequestMapping` annotations as class memebers
```java
@RestController
@RequestMapping("/hello")
public class HelloApplication {
``` 
- Change the method to use the Spring annotations : `@GetMapping`, `@PathVariable`
```java
@GetMapping("/{name}")
public String greeting(@PathVariable("name") String name) {
    return "Hello, " + name;
}
```
- Test it
```bash
curl localhost:8080/hello/charles
http -s solarized localhost:8080/hello/charles
```
- Revisit the method `greeting` to map the path of the name id `/{name}` and get the `@PathVariable`
```java
@GetMapping("/{name}")
public HelloService.Greeting greeting(@PathVariable("name") String idName) {
    return helloService.politeHello(idName);
```

- Comment the annotation `@ApplicationScoped` within the class `HelloService` and declare the class
  as `@Component`
```java
//@ApplicationScoped
@Component
public class HelloService {
```
- Create an internal Greeting class
```java
    public static class Greeting {
        private final String message;

        public Greeting(String message) {
            this.message = message;
        }

        public String getMessage(){
            return message;
        }
    }
```
- Inject the greeting property from the `application.properties` file
```java
    @Value("${greeting}")
    private String greeting;
```
- Wire the `HelloService` bean
```java
@Autowired
HelloService helloService;
```
- and consume the `Greeting setter field` from the Greeting class
```java
 public HelloService.Greeting politeHello(String name){
     return new HelloService.Greeting(greeting + ", " + name);
 }
```
- compile the project and test it 
```bash
./mvnw clean package quarkus:dev
```