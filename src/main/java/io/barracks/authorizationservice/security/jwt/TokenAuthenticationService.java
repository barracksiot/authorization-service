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
import io.barracks.authorizationservice.security.UserAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TokenAuthenticationService {

    private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";

    private final TokenManager tokenManager;
    private final UserManager userManager;

    @Autowired
    public TokenAuthenticationService(TokenManager tokenManager, UserManager userManager) {
        this.tokenManager = tokenManager;
        this.userManager = userManager;
    }

    public void addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
        final UserDetails user = authentication.getDetails();
        Date expiration = new Date(new Date().getTime() + TimeUnit.DAYS.toMillis(1L));
        response.addHeader(AUTH_HEADER_NAME, tokenManager.createTokenForUser(user, expiration));
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null) {
            final String username = tokenManager.getEmailFromToken(token);
            final User user = userManager.getUserByEmail(username);
            if (isValidToken(token, user)) {
                return new UserAuthentication(user);
            }
        }
        throw new BarracksAuthenticationException();
    }

    boolean isValidToken(String token, User user) {
        if (user != null) {
            if (tokenManager.getTokenExpirationDate(token) == null) {
                Token barracksToken = tokenManager.getTokenByValue(token);
                if (barracksToken != null && !barracksToken.isRevoked()) {
                    return true;
                }
                throw new BarracksAuthenticationException();
            }
            return true;
        }
        return false;
    }
}
