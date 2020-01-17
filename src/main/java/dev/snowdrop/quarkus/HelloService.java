package dev.snowdrop.quarkus;

//import org.eclipse.microprofile.config.inject.ConfigProperty;
//import javax.enterprise.context.ApplicationScoped;

import org.springframework.stereotype.Component;

//@ApplicationScoped
@Component
public class HelloService {
    /*
    @ConfigProperty(name = "greeting")
    private String greeting;

    public String politeHello(String name){
        return "Hello Mr/Mrs " + name;
    }
    */

    public HelloService.Greeting politeHello(String name){
        //return greeting + " " + name;
        return new HelloService.Greeting("hello " + name);
    }

    public static class Greeting {
        private final String message;

        public Greeting(String message) {
            this.message = message;
        }

        public String getMessage(){
            return message;
        }
    }
}