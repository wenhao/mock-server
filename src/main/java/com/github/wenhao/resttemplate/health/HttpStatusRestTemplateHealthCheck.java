package com.github.wenhao.resttemplate.health;

import com.github.wenhao.resttemplate.interceptor.ClientHttpResponseWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpStatus.OK;

@Component
public class HttpStatusRestTemplateHealthCheck implements RestTemplateHealthCheck {

    @Override
    public boolean health(final ClientHttpResponseWrapper response) {
        try {
            return OK.equals(response.getStatusCode());
        } catch (IOException e) {
            return false;
        }
    }
}
