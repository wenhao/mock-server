package com.github.wenhao.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    private String body;
    private List<Header> headers;

    public static Response empty() {
        return Response.builder()
                .body("")
                .headers(Stream.of(Header.builder()
                        .name("Content-Type")
                        .values(Stream.of("application/json;charset=UTF-8").collect(toList()))
                        .build())
                        .collect(toList()))
                .build();
    }
}