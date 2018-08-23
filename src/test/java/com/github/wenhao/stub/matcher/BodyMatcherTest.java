package com.github.wenhao.stub.matcher;

import com.github.wenhao.common.domain.Header;
import com.github.wenhao.common.domain.Request;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BodyMatcherTest {

    private BodyMatcher bodyMatcher;

    @BeforeEach
    void setUp() {
        bodyMatcher = new BodyMatcher(ImmutableList.of(new JsonBodyMatcher(), new XMLBodyMatcher()));
    }

    @Test
    void should_match_json() {
        // given
        final Request stub = Request.builder()
                .body("{\"id\":1,\"name\":\"Juergen\"}")
                .build();
        final Request real = Request.builder()
                .headers(ImmutableList.of(Header.builder().name("Content-Type").value("application/json").build()))
                .body("{\"name\":\"Juergen\",\"id\":1}")
                .build();

        // when
        final boolean isMatch = bodyMatcher.match(stub, real);

        // then
        assertThat(isMatch).isTrue();
    }

    @Test
    void should_match_xml() {
        // given
        final Request stub = Request.builder()
                .body("<bookstore> \n" +
                        "   <book nationality=\"ITALIAN\" category=\"COOKING\">\n" +
                        "       <title lang=\"en\">Everyday Italian</title>\n" +
                        "       <author>Giada De Laurentiis</author>\n" +
                        "       <year>2005</year>\n" +
                        "       <price>30.00</price>\n" +
                        "   </book>\n" +
                        "</bookstore>")
                .build();
        final Request real = Request.builder()
                .headers(ImmutableList.of(Header.builder().name("Content-Type").value("text/xml").build()))
                .body("<bookstore> \n" +
                        "   <book nationality=\"ITALIAN\" category=\"COOKING\">\n" +
                        "       <title lang=\"en\">Everyday Italian</title>\n" +
                        "       <author>Giada De Laurentiis</author>\n" +
                        "       <year>2005</year>\n" +
                        "       <price>30.00</price>\n" +
                        "   </book>\n" +
                        "</bookstore>")
                .build();

        // when
        final boolean isMatch = bodyMatcher.match(stub, real);

        // then
        assertThat(isMatch).isTrue();
    }
}