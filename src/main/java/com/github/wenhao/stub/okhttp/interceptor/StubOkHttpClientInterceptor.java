package com.github.wenhao.stub.okhttp.interceptor;

import com.github.wenhao.common.domain.Header;
import com.github.wenhao.common.domain.Request;
import com.github.wenhao.stub.domain.Stub;
import com.github.wenhao.stub.matcher.RequestMatcher;
import com.github.wenhao.stub.properties.MushroomsStubConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static okhttp3.Protocol.HTTP_1_1;
import static org.apache.commons.lang.StringUtils.substringBefore;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RequiredArgsConstructor
public class StubOkHttpClientInterceptor implements Interceptor {

    private static final MediaType APPLICATION_JSON_UTF8 = MediaType.parse("application/json;charset=UTF-8");
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final MushroomsStubConfigurationProperties properties;
    private final List<RequestMatcher> requestMatchers;

    @Override
    public okhttp3.Response intercept(final Chain chain) throws IOException {
        okhttp3.Request request = chain.request();

        final Request realRequest = Request.builder()
                .path(substringBefore(request.url().toString(), "?"))
                .method(request.method())
                .headers(request.headers().names().stream()
                        .map(name -> Header.builder().name(name).value(request.headers().get(name)).build())
                        .collect(toList()))
                .body(Optional.ofNullable(request.body()).map(this::getRequestBody).orElse(""))
                .contentType(request.body().contentType().toString())
                .build();

        final Optional<Stub> stubOptional = properties.getStubs().stream()
                .filter(stub -> requestMatchers.stream().allMatch(matcher -> matcher.match(stub.getRequest(), realRequest)))
                .findFirst();

        if (stubOptional.isPresent()) {
            log.debug("[MUSHROOMS]Respond with stub data for request\n{}", realRequest.toString());
            return getResponse(request, stubOptional.get().getResponse());
        }
        return chain.proceed(request);
    }

    private okhttp3.Response getResponse(final okhttp3.Request request, final String body) {
        final String contentType = Optional.ofNullable(request.headers().get("Content-Type")).orElse(APPLICATION_JSON_UTF8.toString());
        return Mono.just(new okhttp3.Response.Builder())
                .map(resp -> resp.code(OK.value()))
                .map(resp -> resp.request(request))
                .map(resp -> resp.message("[MUSHROOMS]Respond with stub data"))
                .map(resp -> resp.protocol(HTTP_1_1))
                .map(resp -> resp.body(ResponseBody.create(MediaType.parse(contentType), body)))
                .map(resp -> resp.headers(Headers.of("Content-Type", contentType)))
                .block()
                .build();
    }

    private String getRequestBody(final RequestBody requestBody) {
        Buffer buffer = new Buffer();
        try {
            requestBody.writeTo(buffer);
            Charset charset = Optional.ofNullable(requestBody.contentType()).map(contentType -> contentType.charset(UTF8)).orElse(UTF8);
            return buffer.readString(charset);
        } catch (Exception e) {
            return "";
        }
    }
}
