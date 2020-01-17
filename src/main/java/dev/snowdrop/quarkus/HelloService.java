package dev.snowdrop.quarkus;

//import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//import javax.enterprise.context.ApplicationScoped;

//@ApplicationScoped
public class HelloService {

    //@ConfigProperty(name = "greeting")
    //private String greeting;

    @GetMapping("/{name}")
    public Greeting politeHello(String name){
        //return greeting + " " + name;
        return new Greeting("hello" + name);
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

   /*
   public String politeHello(String name){
        return "Hello Mr/Mrs " + name;
    }
    */
}