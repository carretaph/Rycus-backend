package com.rycus.Rycus_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.rycus.Rycus_backend")
@EnableJpaRepositories(basePackages = "com.rycus.Rycus_backend.repository")
@EntityScan(basePackages = "com.rycus.Rycus_backend")
@ComponentScan(basePackages = "com.rycus.Rycus_backend")
public class RycusBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RycusBackendApplication.class, args);
    }
}
