package com.appdev.set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories("com.appdev.set.repository") // Ensure JPA Repositories are scanned
@EntityScan("com.appdev.set.model") // Ensure JPA Entities are scanned
public class SetApplication {
    public static void main(String[] args) {
        SpringApplication.run(SetApplication.class, args);
    }
}
