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

package io.barracks.authorizationservice.integration;

import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserAuthenticationTest {
    @Value("${local.server.port}")
    private int port;
    private String serverUrl;

    // Template used for requesting
    @Autowired
    private TestRestTemplate testTemplate;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        serverUrl = "http://localhost:" + port;
    }

    private void insertUserInDb(String email) {
        userRepository.deleteAll();
        final User user = User.builder()
                .email(email)
                .encryptedPassword(encoder.encode("myCrazyTestPassword"))
                .apiKey(UUID.randomUUID().toString())
                .build();
        userRepository.save(user);
    }

    @Test
    public void login_shouldGenerateHttpOkWithAuthTokenHeaderAndUserInformation_whenCredentialsMatch() {
        // Given
        final String email = "test@barracks.io";
        final String requestContent = "{" +
                "\"username\" : \"" + email + "\"," +
                "\"password\" : \"myCrazyTestPassword\"" +
                "}";
        insertUserInDb(email);

        // When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                User.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.OK);
        assertThat(responseEntity.getHeaders().get("X-Auth-Token").get(0)).isNotEmpty();
        assertThat(responseEntity.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("email", email);
    }

    @Test
    public void login_shouldGenerateUnauthorized_whenCredentialsAreMissing() {
        // Give
        final String requestContent = "{}";
        // When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void login_shouldGenerateUnauthorized_whenUsernameIsWrong() {
        // Give
        final String requestContent = "{" +
                "\"username\" : \"wrong@barracks.io\"," +
                "\"password\" : \"myDamnWrongPassword\"" +
                "}";
        insertUserInDb("test2@barracks.io");
        // When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void login_shouldGenerateUnauthorized_whenPasswordIsWrong() {
        // Give
        final String email = "test3@barracks.io";
        final String requestContent = "{" +
                "\"username\" : \" " + email + "\"," +
                "\"password\" : \"myDamnWrongPassword\"" +
                "}";

        insertUserInDb(email);
        // When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void login_shouldGenerateDifferentTokens_whenCalledTwice() {
        // Give"
        final String email = "test4@barracks.io";
        final String requestContent = "{" +
                "\"username\" : \"" + email + "\"," +
                "\"password\" : \"myCrazyTestPassword\"" +
                "}";
        insertUserInDb(email);
        // When
        ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                Void.class
        );
        String token1 = responseEntity.getHeaders().get("X-Auth-Token").get(0);

        responseEntity = testTemplate.exchange(
                serverUrl + "/login",
                HttpMethod.POST,
                new HttpEntity<>(requestContent),
                Void.class
        );

        //Then
        assertThat(token1).isNotEqualTo(responseEntity.getHeaders().get("X-Auth-Token").get(0));
    }

}
