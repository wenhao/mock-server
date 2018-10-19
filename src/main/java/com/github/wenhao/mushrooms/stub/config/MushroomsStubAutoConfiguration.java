package com.github.wenhao.mushrooms.stub.config;

import com.github.wenhao.mushrooms.stub.dataloader.ResourceReader;
import com.github.wenhao.mushrooms.stub.matcher.BodyMatcher;
import com.github.wenhao.mushrooms.stub.matcher.HeaderMatcher;
import com.github.wenhao.mushrooms.stub.matcher.JsonBodyMatcher;
import com.github.wenhao.mushrooms.stub.matcher.JsonPathMatcher;
import com.github.wenhao.mushrooms.stub.matcher.MethodMatcher;
import com.github.wenhao.mushrooms.stub.matcher.ParameterMatcher;
import com.github.wenhao.mushrooms.stub.matcher.PathMatcher;
import com.github.wenhao.mushrooms.stub.matcher.RequestBodyMatcher;
import com.github.wenhao.mushrooms.stub.matcher.RequestMatcher;
import com.github.wenhao.mushrooms.stub.matcher.XMLBodyMatcher;
import com.github.wenhao.mushrooms.stub.matcher.XpathBodyMatcher;
import com.github.wenhao.mushrooms.stub.okhttp.health.HttpStatusOkHttpClientHealthCheck;
import com.github.wenhao.mushrooms.stub.okhttp.health.OkHttpClientHealthCheck;
import com.github.wenhao.mushrooms.stub.okhttp.interceptor.StubOkHttpClientInterceptor;
import com.github.wenhao.mushrooms.stub.properties.MushroomsStubConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

@Configuration
@ConditionalOnProperty(prefix = "mushrooms.stub", name = "enabled", havingValue = "true")
public class MushroomsStubAutoConfiguration {

    @Bean
    @Order
    public StubOkHttpClientInterceptor stubOkHttpClientInterceptor(MushroomsStubConfigurationProperties properties,
                                                                   List<RequestMatcher> requestMatchers,
                                                                   List<OkHttpClientHealthCheck> healthChecks) {
        return new StubOkHttpClientInterceptor(properties, requestMatchers, healthChecks);
    }

    @Bean
    @ConditionalOnMissingBean
    public MushroomsStubConfigurationProperties mushroomsStubConfigurationProperties(ResourceReader resourceReader) {
        return new MushroomsStubConfigurationProperties(resourceReader);
    }

    @Bean
    @Order(10)
    public HttpStatusOkHttpClientHealthCheck httpStatusOkHttpClientHealthCheck() {
        return new HttpStatusOkHttpClientHealthCheck();
    }

    @Bean
    public ResourceReader resourceReader(ResourceLoader resourceLoader) {
        return new ResourceReader(resourceLoader);
    }

    @Bean
    @Order(5)
    public RequestMatcher pathMatcher() {
        return new PathMatcher();
    }

    @Bean
    @Order(10)
    public RequestMatcher parameterMatcher() {
        return new ParameterMatcher();
    }

    @Bean
    @Order(15)
    public RequestMatcher methodMatcher() {
        return new MethodMatcher();
    }

    @Bean
    @Order(20)
    public RequestMatcher headerMatcher() {
        return new HeaderMatcher();
    }

    @Bean
    public BodyMatcher bodyMatcher(List<RequestBodyMatcher> requestBodyMatchers) {
        return new BodyMatcher(requestBodyMatchers);
    }

    @Bean
    public RequestBodyMatcher jsonBodyMatcher() {
        return new JsonBodyMatcher();
    }

    @Bean
    public RequestBodyMatcher jsonPathBodyMatcher() {
        return new JsonPathMatcher();
    }

    @Bean
    public RequestBodyMatcher xmlBodyMatcher() {
        return new XMLBodyMatcher();
    }

    @Bean
    public RequestBodyMatcher xpathBodyMatcher() {
        return new XpathBodyMatcher();
    }

}
