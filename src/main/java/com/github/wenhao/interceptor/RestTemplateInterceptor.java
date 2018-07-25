package com.github.wenhao.interceptor;

import com.github.wenhao.config.ParrotConfiguration;
import com.github.wenhao.domain.CacheRequest;
import com.github.wenhao.domain.Header;
import com.github.wenhao.health.RestTemplateHealthCheck;
import com.github.wenhao.repository.ParrotCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    private final ParrotCacheRepository parrotCacheRepository;
    private final ParrotConfiguration parrotConfiguration;
    private final List<RestTemplateHealthCheck> restTemplateHealthChecks;

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = getRealResponse(request, body, execution);
        ClientHttpResponseWrapper responseWrapper = new ClientHttpResponseWrapper(response);
        CacheRequest cacheRequest = CacheRequest.builder()
                .uri(request.getURI())
                .method(request.getMethodValue())
                .headers(getHeaders(request))
                .body(new String(body))
                .build();
        boolean isHealth = restTemplateHealthChecks.stream().allMatch(restTemplateHealthCheck -> restTemplateHealthCheck.health(responseWrapper));
        if (isHealth) {
            parrotCacheRepository.save(cacheRequest, responseWrapper.getBodyAsString());
            return responseWrapper;
        }
        return Optional.ofNullable(parrotCacheRepository.get(cacheRequest))
                .map(item -> CachedClientHttpResponse.builder()
                        .httpStatus(OK)
                        .httpHeaders(response.getHeaders())
                        .body(item)
                        .build())
                .orElse(new CachedClientHttpResponse(response));
    }

    private ClientHttpResponse getRealResponse(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        try {
            return execution.execute(request, body);
        } catch (Exception e) {
            return CachedClientHttpResponse.builder()
                    .httpStatus(INTERNAL_SERVER_ERROR)
                    .httpHeaders(request.getHeaders())
                    .body("")
                    .build();
        }
    }

    private List<Header> getHeaders(final HttpRequest request) {
        List<Header> headers = request.getHeaders().keySet().stream()
                .map(key -> Header.builder().name(key).values(request.getHeaders().get(key)).build())
                .collect(toList());
        if (!isEmpty(parrotConfiguration.getHeaders())) {
            return headers.stream()
                    .filter(header -> parrotConfiguration.getHeaders().contains(header.getName()))
                    .collect(toList());
        }
        return headers;
    }
}
