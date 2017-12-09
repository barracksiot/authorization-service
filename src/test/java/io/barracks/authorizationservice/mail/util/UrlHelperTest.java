/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
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

package io.barracks.authorizationservice.mail.util;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlHelperTest {
    @Test
    public void compile_withUrlHelper_shouldEncode() throws IOException {
        // Given
        final String email = "test+plus@barracks.io";
        final String expected = URLEncoder.encode(email, "UTF-8");
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper(UrlHelper.NAME, new UrlHelper());
        Template template = handlebars.compileInline("{{url context.userEmail}}");
        MyWrapper wrapper = new MyWrapper(email);

        // When
        String result = template.apply(wrapper);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void compileWithUrlHelper_whenEmpty_shouldReturnEmpty() throws IOException {
        // Given
        final String email = "";
        final String expected = "";
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper(UrlHelper.NAME, new UrlHelper());
        Template template = handlebars.compileInline("{{url context.userEmail}}");
        MyWrapper wrapper = new MyWrapper(email);

        // When
        String result = template.apply(wrapper);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void compileWithUrlHelper_whenNull_shouldReturnEmpty() throws IOException {
        // Given
        final String email = null;
        final String expected = "";
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper(UrlHelper.NAME, new UrlHelper());
        Template template = handlebars.compileInline("{{url context.userEmail}}");
        MyWrapper wrapper = new MyWrapper(email);

        // When
        String result = template.apply(wrapper);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static final class MyWrapper {
        private final MyContext context;

        private MyWrapper(String email) {
            this.context = new MyContext(email);
        }

        public MyContext getContext() {
            return context;
        }
    }

    private static final class MyContext {
        private final String userEmail;

        private MyContext(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getUserEmail() {
            return userEmail;
        }
    }
}
