package com.savushkin.ignite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IgniteApplication {

    public static void main(String[] args) {
        SpringApplication.run(IgniteApplication.class, args);
    }

}
