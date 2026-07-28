package com.learningdemo.oauth2clientcredentials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OAuth2ClientCredentialsJwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2ClientCredentialsJwtApplication.class, args);
    }
}
