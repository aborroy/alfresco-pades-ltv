package org.alfresco.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class App {

    public static void main(final String[] args) {
        SpringApplication.run(App.class, args);
    }

}