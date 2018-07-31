package com.github.wenhao.failover.okhttp.interceptor;

import com.github.wenhao.common.domain.Header;
import com.github.wenhao.common.domain.Request;
import com.github.wenhao.failover.okhttp.health.OkHttpClientHealthCheck;
import com.github.wenhao.failover.properties.MushroomsFailoverConfigurationProperties;
import com.github.wenhao.failover.repository.FailoverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static okhttp3.Protocol.HTTP_1_1;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
public class CachingOkHttpClientInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json;charset=UTF-8");
    private final FailoverRepository repository;
    private final MushroomsFailoverConfigurationProperties properties;
    private final List<OkHttpClientHealthCheck> healthChecks;

    @Override
    public Response intercept(final Chain chain) throws IOException {
        okhttp3.Request request = chain.request();

        final Request cacheRequest = Request.builder()
                .uri(request.url().uri())
                .headers(getHeaders(request))
                .method(request.method())
                .body(Optional.ofNullable(request.body()).map(this::getRequestBody).orElse(""))
                .build();

        final Response response = getRemoteResponse(chain, request);
        boolean isHealth = healthChecks.stream().allMatch(okHttpClientInterceptor -> okHttpClientInterceptor.health(response));
        if (isHealth) {
            log.debug("[MUSHROOMS]Refresh cached data for request\n{}.", cacheRequest.toString());
            String responseBody = getResponseBody(response.body());
            repository.save(cacheRequest, responseBody);
            return getResponse(request, response, responseBody);
        }
        return Optional.ofNullable(repository.get(cacheRequest))
                .map(cache -> {
                    log.debug("[MUSHROOMS]Respond with cached data for request\n{}.", cacheRequest.toString());
                    return cache;
                })
                .map(body -> getResponse(request, response, body))
                .orElse(getResponse(request, response, ""));
    }

    private Response getResponse(final okhttp3.Request request, final Response response, final String body) {
        return Mono.just(new Response.Builder())
                .map(resp -> resp.code(OK.value()))
                .map(resp -> resp.request(request))
                .map(resp -> resp.message(response.message()))
                .map(resp -> resp.protocol(response.protocol()))
                .map(resp -> Optional.ofNullable(response.body()).map(respBody -> resp.body(ResponseBody.create(respBody.contentType(), body)))
                        .orElse(resp.body(ResponseBody.create(APPLICATION_JSON_UTF8, body))))
                .map(resp -> Optional.ofNullable(response.headers()).map(resp::headers).orElse(resp))
                .map(resp -> Optional.ofNullable(response.cacheResponse()).map(resp::cacheResponse).orElse(resp))
                .map(resp -> Optional.ofNullable(response.handshake()).map(resp::handshake).orElse(resp))
                .map(resp -> Optional.ofNullable(response.networkResponse()).map(resp::networkResponse).orElse(resp))
                .map(resp -> Optional.ofNullable(response.priorResponse()).map(resp::priorResponse).orElse(resp))
                .map(resp -> resp.receivedResponseAtMillis(response.receivedResponseAtMillis()))
                .block()
                .build();
    }

    private Response getRemoteResponse(final Chain chain, final okhttp3.Request request) {
        try {
            return chain.proceed(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new Response.Builder()
                    .code(INTERNAL_SERVER_ERROR.value())
                    .request(request)
                    .message(e.getMessage())
                    .protocol(HTTP_1_1)
                    .body(ResponseBody.create(APPLICATION_JSON_UTF8, ""))
                    .headers(Headers.of("Content-Type", APPLICATION_JSON_UTF8.toString()))
                    .build();
        }
    }

    private List<Header> getHeaders(final okhttp3.Request request) {
        final Map<String, List<String>> headerMap = request.headers().toMultimap();
        final List<Header> headers = headerMap.keySet().stream()
                .map(name -> Header.builder().name(name).values(headerMap.get(name)).build())
                .collect(toList());
        if (!isEmpty(properties.getHeaders())) {
            return headers.stream()
                    .filter(header -> properties.getHeaders().contains(header.getName()))
                    .collect(toList());
        }
        return headers;
    }

    private String getRequestBody(final RequestBody requestBody) {
        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            return buffer.readString(charset);
        } catch (Exception e) {
            return "";
        }
    }

    private String getResponseBody(final ResponseBody responseBody) {
        try {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            return buffer.clone().readString(charset);
        } catch (Exception e) {
            return "";
        }
    }
}
