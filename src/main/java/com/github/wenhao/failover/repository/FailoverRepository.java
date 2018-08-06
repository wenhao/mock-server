package com.github.wenhao.failover.repository;

import com.github.wenhao.common.domain.Request;
import com.github.wenhao.common.domain.Response;
import com.github.wenhao.failover.properties.MushroomsFailoverConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;

@RequiredArgsConstructor
public class FailoverRepository {

    private final HashOperations<String, Request, Response> hashOperations;
    private final MushroomsFailoverConfigurationProperties properties;

    public void save(Request request, Response response) {
        hashOperations.put(properties.getKey(), request, response);
    }

    public Response get(Request request) {
        return hashOperations.get(properties.getKey(), request);
    }

}
