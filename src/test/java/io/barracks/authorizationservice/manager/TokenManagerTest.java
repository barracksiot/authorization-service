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

package io.barracks.authorizationservice.manager;

import io.barracks.authorizationservice.exception.UnknownTokenException;
import io.barracks.authorizationservice.model.Token;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.repository.TokenRepository;
import io.barracks.authorizationservice.security.jwt.TokenHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenManagerTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenHandler tokenHandler;

    private TokenManager manager;

    private User user;

    @Before
    public void setUp() {
        manager = spy(new TokenManager(tokenHandler, tokenRepository));
        reset(manager, tokenRepository);
        user = User.builder().email("test@barracks.io").id(UUID.randomUUID().toString()).build();
    }

    @Test
    public void createToken_whenAllIsFine_shouldInsertToken() {
        // Given
        final User user = User.builder().id(UUID.randomUUID().toString()).email("test@barracks.io").build();
        final String label = "My test token";
        final String tokenValue = UUID.randomUUID().toString();
        final Token toSave = Token.builder()
                .userId(user.getId())
                .label(label)
                .value(tokenValue)
                .build();

        final Token expected = toSave.toBuilder()
                .id(UUID.randomUUID().toString())
                .startDate(new Date())
                .build();

        when(tokenHandler.createTokenForEmail(user.getEmail())).thenReturn(tokenValue);
        when(tokenRepository.insert(toSave)).thenReturn(expected);

        // When
        final Token result = manager.createToken(user, label);

        // Then
        verify(tokenHandler).createTokenForEmail(user.getEmail());
        verify(tokenRepository).insert(toSave);
        assertThat(result).isNotNull().isEqualTo(expected);
    }

    @Test
    public void getTokens_whenSomeTokens_shouldCallRepositoryAndReturnResult() {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final Token token1 = Token.builder().id(UUID.randomUUID().toString()).build();
        final Token token2 = Token.builder().id(UUID.randomUUID().toString()).build();
        final List<Token> response = new ArrayList<Token>() {{
            add(token1);
            add(token2);
        }};
        final Page<Token> expected = new PageImpl<>(response, pageable, 2);

        when(tokenRepository.getTokensByUserId(user.getId(), pageable)).thenReturn(expected);

        // When
        final Page<Token> result = manager.getTokensForUser(user, pageable);

        // Then
        verify(tokenRepository).getTokensByUserId(user.getId(), pageable);
        assertThat(result).isEqualTo(expected);
    }


    @Test
    public void getTokenByValue_whenTokenExists_shouldCallRepositoryAndReturnResult() {
        // Given
        final String value = UUID.randomUUID().toString();
        final Token expected = Token.builder()
                .id(UUID.randomUUID().toString())
                .value(value)
                .label("Bonsoir")
                .build();
        when(tokenRepository.findByValue(value)).thenReturn(Optional.of(expected));

        // When
        final Token result = manager.getTokenByValue(value);

        //Then
        verify(tokenRepository).findByValue(value);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getTokenByValue_whenTokenDoesNotExist_shouldCallRepositoryAndThrowException() {
        // Given
        final String value = UUID.randomUUID().toString();

        when(tokenRepository.findByValue(value)).thenThrow(new UnknownTokenException(value));

        // When / Then
        assertThatExceptionOfType(UnknownTokenException.class)
                .isThrownBy(() -> manager.getTokenByValue(value));
        verify(tokenRepository).findByValue(value);
    }

    @Test
    public void revokeToken_whenAllIsFine_shouldCallRepositoryAndReturnRevokedTokenForCurrentUser() {
        // Given
        final Token token = Token.builder()
                .label("Test label")
                .value(UUID.randomUUID().toString())
                .id(UUID.randomUUID().toString())
                .userId(user.getId())
                .build();

        final Token expected = token.toBuilder().revoked(true).build();

        doReturn(Optional.of(token)).when(tokenRepository).findByValue(token.getValue());
        doReturn(expected).when(tokenRepository).revokeToken(token.getId());

        // When
        Token result = manager.revokeToken(token.getValue());

        // Then
        verify(tokenRepository).revokeToken(token.getId());
        assertThat(result.getId()).isEqualTo(token.getId());
        assertThat(result.getUserId()).isEqualTo(user.getId());
        assertThat(result.isRevoked()).isEqualTo(true);
    }
}
