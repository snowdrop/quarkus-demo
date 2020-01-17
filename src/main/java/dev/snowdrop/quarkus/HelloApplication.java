package dev.snowdrop.quarkus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//@Path("/hello")
@RestController
@RequestMapping("/hello")
public class HelloApplication {

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

    @Autowired
    HelloService helloService;

    @GetMapping("/{name}")
    public HelloService.Greeting greeting(@PathVariable("name") String idName) {
        return helloService.politeHello(idName);
    }
}