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

package io.barracks.authorizationservice.model;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import io.barracks.authorizationservice.utils.TokenUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@JsonTest
public class TokenJsonTests {

    @Autowired
    private JacksonTester<Token> json;

    @Test
    public void serializeJson_shouldSerializeAllFieldsExceptIdAndUserId() throws IOException {
        // Given
        final Date date = new Date(1234567890L);

        final Token token = TokenUtils.getToken().toBuilder().startDate(date).build();
        final String stringDate = ISO8601Utils.format(date, true);

        // When
        final JsonContent<Token> result = json.write(token);

        // Then

        assertThat(token).hasNoNullFieldsOrProperties();
        assertThat(result).extractingJsonPathStringValue("@.id").isEqualTo(token.getId());
        assertThat(result).extractingJsonPathStringValue("@.userId").isEqualTo(token.getUserId());
        assertThat(result).extractingJsonPathStringValue("@.label").isEqualTo(token.getLabel());
        assertThat(result).extractingJsonPathStringValue("@.value").isEqualTo(token.getValue());
        assertThat(result).extractingJsonPathStringValue("@.startDate").isEqualTo(stringDate);

    }

}
