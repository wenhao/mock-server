package com.github.wenhao;

import com.github.wenhao.interceptor.RestTemplateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class Application {

    @Autowired
    private RestTemplateInterceptor restTemplateInterceptor;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Stream.of(restTemplateInterceptor).collect(toList()));
        return restTemplate;
    }
}
