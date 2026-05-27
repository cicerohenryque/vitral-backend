package com.vitral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VitralBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VitralBackendApplication.class, args);
    }

}
