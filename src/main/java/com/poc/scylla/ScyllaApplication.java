package com.poc.scylla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ScyllaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScyllaApplication.class, args);
    }

}
