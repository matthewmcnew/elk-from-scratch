package com.mattmcnew.elkfromscratch.elkfromscratch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggingController {

    @GetMapping("/")
    public String root() {
        System.out.println("Log to STDOUT");
        return "Hello";
    }

    @GetMapping("/exception/{name}")
    public String throwException(@PathVariable String name) {
        throw new RuntimeException(name);
    }

}
