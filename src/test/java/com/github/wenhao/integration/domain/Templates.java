package com.github.wenhao.integration.domain;

import javafx.util.Pair;

public class Templates {
    public static final Pair<String, String> GET_BOOK_PRICE = new Pair<>("get_book.ftl", "$.soap:Envelope.soap:Body.m:GetBookResponse");
    public static final Pair<String, String> FAULT = new Pair<>("", "$.S:Envelope.S:Body.S:Fault");
}
