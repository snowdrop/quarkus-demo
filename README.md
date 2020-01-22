# Cool Quarkus demo

The purpose of this demo is to showcase how you can :

- Create a new Quarkus project using a maven archetype
- Build/Run the project locally and call the endpoint
- Play with hot reload feature
- Use Spring technology instead of Java Microprofile
- Deploy and run your project on a kubernetes cluster using Halkyon operator and hal client

## Table of Contents

  * [Create a Quarkus REST project](#create-a-quarkus-rest-project)
  * [Jump to Spring on Quarkus now](#jump-to-spring-on-quarkus-now)
  * [Deploy on K8s/OpenShift](#deploy-on-k8sopenshift)
  * [And what about Spring JPA](#and-what-about-spring-jpa)
  * [TODO](#todo)

## Create a Quarkus REST project

- Move within a terminal to the folder where you will play the scenario
```bash
cd /Users/dabou/Temp/quarkus/demo
```
- Steps to execute to create the Quarkus REST project
```bash
mvn io.quarkus:quarkus-maven-plugin:1.1.1.Final:create \
    -DprojectGroupId=dev.snowdrop \
    -DprojectArtifactId=quarkus-rest \
    -DclassName="dev.snowdrop.HelloApplication" \
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
  **NOTE** : You can also add the extension ot the pom using the quarkus maven goal `mvn quarkus:add-extension -Dextensions="spring-web`
  
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

## Deploy on K8s/OpenShift

- To deploy our application on K8s, we will use the `halkyon` project with its `hal` tool
- First, create a component to deploy the project on the cluster
```bash
hal component create quarkus-rest-1 
? Runtime quarkus
? Version 1.1.1.Final
? Expose microservice Yes
? Port 8080
? Use code generator (Y/n) n
```
- If the component is created correctly, you will see this message
```bash
❯ Selected Name: quarkus-rest-1
 ✓  Successfully created 'quarkus-rest-1' component
```
- Check if the component exists 
```bash
oc get component -n quarkus-demo
NAME             RUNTIME   VERSION       AGE   MODE   STATUS   MESSAGE                                                        REVISION
quarkus-rest-1   quarkus   1.1.1.Final   50m   dev    Ready    Ready: 'PodName' changed to 'quarkus-rest-1-7856dbf6d-lmrpg'   d9ac140d2dbcd6d60167ff8f14e6994a03bf2b7b  
```

- Next push the code of the project and wait a few moment till it will compiled
```bash
hal component push -c quarkus-rest-1
Local changes detected for 'quarkus-rest-1' component: about to push source code to remote cluster
 ✓  Uploading /Users/dabou/Temp/quarkus/demo/quarkus-rest-1.tar
 ✓  Cleaning up component
 ✓  Extracting source on the remote cluster
 ✓  Performing build
 ✓  Restarting app
 ✓  Successfully pushed 'quarkus-rest-1' component to remote cluster
```
- Find the `Route` to call the endpoint 
```bash
export host=$( kc get route/quarkus-rest-1 -o jsonpath='{.spec.host}')
http -s solarized http://$host/hello/'Red Hat'

HTTP/1.1 200 OK
Cache-control: private
Content-Length: 41
Content-Type: application/json
Set-Cookie: aaebe0aa5dce9bfc0f50f192e0fe0810=0504a63f890d9931c1aeeb5e22b80db4; path=/; HttpOnly

{
    "message": "Good afternoon 2, Red%20Hat"
}
```
- To test changes, execute the following command after every file's code changed
```bash
hal component push -c quarkus-rest-1
```
- To clean the project, simply delete the component
```bash
hal component delete quarkus-rest-1
? Really delete 'quarkus-rest-1' component Yes
 ✓  Successfully deleted 'quarkus-rest-1' component
```

## TODO

- Test the demo using the quarkus maven archetype for `spring-web`
  ```bash
  mvn io.quarkus:quarkus-maven-plugin:1.1.1.Final:create \
      -DprojectGroupId=org.acme \
      -DprojectArtifactId=spring-web-quickstart \
      -DclassName="org.acme.spring.web.GreetingController" \
      -Dpath="/greeting" \
      -Dextensions="spring-web"
  ```

## And what about Spring JPA

- Steps to execute to create the Quarkus REST project
```bash
mvn io.quarkus:quarkus-maven-plugin:1.1.1.Final:create \
    -DprojectGroupId=dev.snowdrop \
    -DprojectArtifactId=quarkus-spring-jpa \
    -DclassName="com.example.FruitController" \
    -Dpath="/api/fruits" \
    -Dextensions="spring-web,resteasy-jsonb,spring-data-jpa"
```
- Add postgres dependency
   ```xml
   <dependency>
         <groupId>io.quarkus</groupId>
         <artifactId>quarkus-jdbc-h2</artifactId>
   </dependency>
   ```
- Add datasource config
  ```
  quarkus.datasource.url=jdbc:h2:tcp://localhost/mem:quarkus_test
  quarkus.datasource.driver=org.h2.Driver
  quarkus.datasource.username=quarkus_test
  quarkus.datasource.password=
  
  quarkus.hibernate-orm.dialect=org.hibernate.dialect.H2Dialect
  quarkus.hibernate-orm.database.generation=drop-and-create
  quarkus.hibernate-orm.sql-load-script=import.sql
  ```
- Add import.sql with following content
  ```sql
  insert into fruit (name) values ('Cherry');
  insert into fruit (name) values ('Apple');
  insert into fruit (name) values ('Banana'); 
  ```
  
- Start locally the h2 database
  ```bash
  cd /path/to/h2/installed
  java -cp bin/h2-1.4.199.jar org.h2.tools.Server -ifNotExists
  ```  
  
- Launch the app in dev mode
  ```bash
  mvn compile quarkus:dev  
  ```
