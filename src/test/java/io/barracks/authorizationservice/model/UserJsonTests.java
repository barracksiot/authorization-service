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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class UserJsonTests {

    @Autowired
    private JacksonTester<User> json;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:io/barracks/authorizationservice/model/user.json")
    private Resource userResource;

    @Test
    public void deserializeJson_shouldIgnoreEncryptedPasswordAndDisabled() throws Exception {
        // Given
        final JsonNode jsonNode = objectMapper.readTree(userResource.getInputStream());

        final User expected = User.builder()
                .id(jsonNode.get("id").textValue())
                .firstName(jsonNode.get("firstName").textValue())
                .lastName(jsonNode.get("lastName").textValue())
                .email(jsonNode.get("email").textValue())
                .status(UserStatus.fromName(jsonNode.get("status").textValue().toLowerCase()))
                .apiKey(jsonNode.get("apiKey").textValue())
                .company(jsonNode.get("company").textValue())
                .phone(jsonNode.get("phone").textValue())
                .build();

        // When
        final User result = json.parseObject(jsonNode.toString());

        // Then
        assertThat(expected).hasNoNullFieldsOrPropertiesExcept("disabled", "encryptedPassword");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void serializeJson_shouldSerializeAllFieldsExceptEncryptedPasswordAndDisabled() throws IOException {
        // Given
        final User user = UserUtils.getUser();

        // When
        JsonContent<User> result = json.write(user);

        // Then
        assertThat(user).hasNoNullFieldsOrProperties();
        assertThat(result).doesNotHaveJsonPathValue("@.encryptedPassword");
        assertThat(result).doesNotHaveJsonPathValue("@.disabled");
        assertThat(result).extractingJsonPathStringValue("@.id").isEqualTo(user.getId());
        assertThat(result).extractingJsonPathStringValue("@.firstName").isEqualTo(user.getFirstName());
        assertThat(result).extractingJsonPathStringValue("@.lastName").isEqualTo(user.getLastName());
        assertThat(result).extractingJsonPathStringValue("@.email").isEqualTo(user.getEmail());
        assertThat(result).extractingJsonPathStringValue("@.status").isEqualTo(user.getStatus().getName());
        assertThat(result).extractingJsonPathStringValue("@.apiKey").isEqualTo(user.getApiKey());
        assertThat(result).extractingJsonPathStringValue("@.company").isEqualTo(user.getCompany());
        assertThat(result).extractingJsonPathStringValue("@.phone").isEqualTo(user.getPhone());

    }

}

