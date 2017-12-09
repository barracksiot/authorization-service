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
import io.barracks.authorizationservice.security.jwt.TokenHandler;
import io.barracks.commons.logging.ManagerMonitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserResourceTest {

    public static final String USER_LOGIN = "test@barracks.io";

    @Value("${local.server.port}")
    private int port;
    private String serverUrl;
    @Value("${io.barracks.authorizationservice.s3cr3t}")
    private String s3cr3t;

    // Template used for requesting
    @Autowired
    private TestRestTemplate testTemplate;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ManagerMonitor managerMonitor;

    private TokenHandler tokenHandler;

    @Before
    public void setUp() {
        serverUrl = "http://localhost:" + port;
        tokenHandler = new TokenHandler(s3cr3t);
    }

    private void insertUserInDb(String email) {
        userRepository.deleteAll();
        final User user = User.builder()
                .email(email)
                .encryptedPassword(encoder.encode("myCrazyTestPassword"))
                .apiKey(UUID.randomUUID().toString())
                .id(UUID.randomUUID().toString())
                .build();
        userRepository.save(user);
    }


    @Test
    public void whoAmI_shouldReturnMyProfile_whenTokenIsCorrect() {
        // Given
        final String email = "test@barracks.io";
        insertUserInDb(email);
        final Date expiration = new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1L));
        final String token = tokenHandler.createTokenForEmail(new TestUserDetails().getUsername(), expiration);
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("X-Auth-Token", Collections.singletonList(token));

        // When
        final ResponseEntity<User> whoAmI = testTemplate.exchange(
                serverUrl + "/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );

        // Then
        assertThat(whoAmI.getBody().getEmail()).isEqualTo(email);
    }

    @Test
    public void whoAmI_shouldGenerateFailure_whenTokenIsWrong() {
        // Given
        final String token = "a.N0Ns3ns3.t0k#n";
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("X-Auth-Token", Collections.singletonList(token));

        //When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/me",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    public void whoAmI_shouldGenerateFailure_whenTokenIsMalformed() {
        //Given
        final String token = "aN0Ns3ns3t0k#n";
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("X-Auth-Token", Collections.singletonList(token));
        //When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/me",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }


    @Test
    public void whoAmI_shouldGenerateFailure_whenTokenIsEmpty() {
        //Given
        final MultiValueMap<String, String> headers = new HttpHeaders();

        //When
        final ResponseEntity responseEntity = testTemplate.exchange(
                serverUrl + "/me",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Void.class
        );

        //Then
        assertThat(responseEntity).hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    private static class TestUserDetails implements UserDetails {

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return USER_LOGIN;
        }

        @Override
        public boolean isAccountNonExpired() {
            return false;
        }

        @Override
        public boolean isAccountNonLocked() {
            return false;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

}
