/*
 * Copyright © 2019, Wen Hao <wenhao@126.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.wenhao.mushrooms.stub.matcher;

import com.github.wenhao.mushrooms.stub.domain.Request;
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
                .body("{\"name\":\"Juergen\",\"id\":1}")
                .contentType("application/json")
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
                .body("<bookstore> \n" +
                        "   <book nationality=\"ITALIAN\" category=\"COOKING\">\n" +
                        "       <title lang=\"en\">Everyday Italian</title>\n" +
                        "       <author>Giada De Laurentiis</author>\n" +
                        "       <year>2005</year>\n" +
                        "       <price>30.00</price>\n" +
                        "   </book>\n" +
                        "</bookstore>")
                .contentType("text/xml")
                .build();

        // when
        final boolean isMatch = bodyMatcher.match(stub, real);

        // then
        assertThat(isMatch).isTrue();
    }

    @Test
    void should_match_if_body_is_empty() {
        // given
        final Request stub = Request.builder()
                .body("")
                .build();
        final Request real = Request.builder()
                .body("{\"name\":\"Juergen\",\"id\":1}")
                .contentType("application/json")
                .build();

        // when
        final boolean isMatch = bodyMatcher.match(stub, real);

        // then
        assertThat(isMatch).isTrue();

    }
}