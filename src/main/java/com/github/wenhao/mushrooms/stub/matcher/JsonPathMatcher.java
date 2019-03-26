package com.github.wenhao.mushrooms.stub.matcher;

import com.github.wenhao.mushrooms.stub.domain.Request;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JsonPathMatcher implements RequestBodyMatcher {

    @Override
    public boolean isApplicable(final Request stubRequest, final Request realRequest) {
        return realRequest.getContentType().contains("json") && stubRequest.getBody().startsWith("jsonPath:");
    }

    @Override
    public boolean match(final Request stubRequest, final Request realRequest) {
        final String jsonPath = StringUtils.substringAfter(stubRequest.getBody(), "jsonPath:");
        try {
            final List result = JsonPath.parse(realRequest.getBody()).read(jsonPath, List.class);
            return result.size() != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
