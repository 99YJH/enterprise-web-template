package com.enterprise.webtemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EnterpriseWebTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseWebTemplateApplication.class, args);
    }
}