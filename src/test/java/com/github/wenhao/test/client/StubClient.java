package com.github.wenhao.test.client;

import com.github.wenhao.common.domain.Header;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "STUB",
        url = "http://localhost:8080"
)
public interface StubClient {

    @GetMapping("test")
    Header get();

    @GetMapping("stub")
    Header stub();
}
