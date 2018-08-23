package com.github.wenhao.stub.matcher;

import com.github.wenhao.common.domain.Request;
import org.json.JSONException;
import org.json.XML;
import org.skyscreamer.jsonassert.JSONCompare;

import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

public class XMLBodyMatcher implements RequestBodyMatcher {

    @Override
    public boolean isApplicable(final Request request) {
        return request.getContentType().contains("xml");
    }

    @Override
    public boolean match(final Request stubRequest, final Request realRequest) {
        try {
            String stubBodyJson = XML.toJSONObject(stubRequest.getBody()).toString();
            String realBodyJson = XML.toJSONObject(realRequest.getBody()).toString();
            return JSONCompare.compareJSON(stubBodyJson, realBodyJson, LENIENT).passed();
        } catch (JSONException e) {
            return false;
        }
    }
}