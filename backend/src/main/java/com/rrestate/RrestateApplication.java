package com.rrestate;

import com.rrestate.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class RrestateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RrestateApplication.class, args);
    }
}
