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

package io.barracks.authorizationservice.security.jwt;

import io.barracks.authorizationservice.exception.BarracksAuthenticationException;
import io.barracks.authorizationservice.manager.TokenManager;
import io.barracks.authorizationservice.manager.UserManager;
import io.barracks.authorizationservice.model.Token;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenAuthenticationServiceTest {

    public static final String USER_LOGIN = "test@barracks.io";

    @Mock
    private TokenManager tokenManager;

    @Mock
    private UserManager userManager;

    private TokenHandler tokenHandler;
    private TokenAuthenticationService tokenAuthenticationService;

    @Before
    public void setUp() {
        tokenAuthenticationService = new TokenAuthenticationService(tokenManager, userManager);
        tokenHandler = new TokenHandler(UUID.randomUUID().toString());
    }

    @Test
    public void verifyToken_whenTokenIsValidAndPermanent_shouldReturnTrue() {
        // Given
        final User user = UserUtils.getUser();
        final String token = tokenHandler.createTokenForEmail(user.getEmail());
        final Token barracksToken = Token.builder()
                .id(UUID.randomUUID().toString())
                .label("Test Token Label")
                .userId(user.getEmail())
                .value(token).build();

        when(tokenManager.getTokenExpirationDate(token)).thenReturn(null);
        when(tokenManager.getTokenByValue(token)).thenReturn(barracksToken);

        // When
        Boolean validity = tokenAuthenticationService.isValidToken(token, user);

        //Then
        verify(tokenManager).getTokenExpirationDate(token);
        verify(tokenManager).getTokenByValue(token);
        assertThat(validity).isTrue();
    }

    @Test
    public void verifyToken_whenTokenIsValidAndNotPermanent_shouldReturnTrue() {
        // Given
        final User user = UserUtils.getUser();
        final Date expiration = new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1L));
        final String token = tokenHandler.createTokenForEmail(user.getEmail(), expiration);

        when(tokenManager.getTokenExpirationDate(token)).thenReturn(expiration);

        // When
        Boolean validity = tokenAuthenticationService.isValidToken(token, user);

        //Then
        verify(tokenManager).getTokenExpirationDate(token);
        assertThat(validity).isTrue();
    }

    @Test
    public void verifyToken_whenTokenIsNotInDataBase_shouldThrowException() {
        // Given
        final User user = UserUtils.getUser();
        final String token = tokenHandler.createTokenForEmail(user.getEmail());

        when(tokenManager.getTokenExpirationDate(token)).thenReturn(null);
        when(tokenManager.getTokenByValue(token)).thenReturn(null);

        //Then When
        assertThatExceptionOfType(BarracksAuthenticationException.class)
                .isThrownBy(() ->
                        tokenAuthenticationService.isValidToken(token, user)
                );
        verify(tokenManager).getTokenExpirationDate(token);
        verify(tokenManager).getTokenByValue(token);
    }

    @Test
    public void verifyToken_whenTokenIsRevoked_shouldThrowException() {
        // Given
        final User user = UserUtils.getUser();
        final String value = tokenHandler.createTokenForEmail(user.getEmail());
        final Token revokedToken = Token.builder()
                .id(UUID.randomUUID().toString())
                .value(value)
                .revoked(true)
                .build();

        when(tokenManager.getTokenExpirationDate(value)).thenReturn(null);
        when(tokenManager.getTokenByValue(value)).thenReturn(revokedToken);

        //Then When
        assertThatExceptionOfType(BarracksAuthenticationException.class)
                .isThrownBy(() ->
                        tokenAuthenticationService.isValidToken(value, user)
                );
        verify(tokenManager).getTokenExpirationDate(value);
        verify(tokenManager).getTokenByValue(value);
    }

    @Test
    public void verifyToken_whenTokenHasNoUserDetail_shouldReturnFalse() {
        // Given
        final String token = UUID.randomUUID().toString();

        // When
        Boolean validity = tokenAuthenticationService.isValidToken(token, null);

        //Then
        assertThat(validity).isFalse();
    }
}
