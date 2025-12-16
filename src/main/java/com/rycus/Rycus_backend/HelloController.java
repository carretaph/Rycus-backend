package com.rycus.Rycus_backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Rycus backend is running ✅";
    }

    @GetMapping("/health")
    public String health() {
        return "Hola Rycus, backend funcionando 🚀";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Rycus 👋";
    }
}
