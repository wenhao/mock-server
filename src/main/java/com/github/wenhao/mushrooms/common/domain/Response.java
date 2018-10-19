package com.github.wenhao.mushrooms.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response {

    private String body;
    @Builder.Default
    private List<Header> headers = new ArrayList<>();
    private String contentType;

}
