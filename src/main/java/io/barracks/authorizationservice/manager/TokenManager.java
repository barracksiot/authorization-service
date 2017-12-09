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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class TokenManager {

    private final TokenHandler tokenHandler;
    private final TokenRepository tokenRepository;

    @Autowired
    public TokenManager(TokenHandler tokenHandler, TokenRepository tokenRepository) {
        this.tokenHandler = tokenHandler;
        this.tokenRepository = tokenRepository;
    }

    public Token createToken(User user, String label) {
        final String generatedToken = tokenHandler.createTokenForEmail(user.getEmail());
        final Token toSave = Token.builder()
                .userId(user.getId())
                .label(label)
                .value(generatedToken)
                .build();
        return tokenRepository.insert(toSave);
    }

    public Page<Token> getTokensForUser(User user, Pageable pageable) {
        return tokenRepository.getTokensByUserId(user.getId(), pageable);
    }

    public Token revokeToken(String tokenValue) {
        final Token token = getTokenByValue(tokenValue);
        return tokenRepository.revokeToken(token.getId());
    }

    public Token getTokenByValue(String tokenValue) {
        final Optional<Token> token = tokenRepository.findByValue(tokenValue);
        return token.orElseThrow(() -> new UnknownTokenException(tokenValue));
    }

    public String getEmailFromToken(String token) {
        return tokenHandler.parseEmailFromToken(token);
    }

    public String createTokenForUser(UserDetails user, Date expiration) {
        return tokenHandler.createTokenForEmail(user.getUsername(), expiration);
    }

    public Date getTokenExpirationDate(String token) {
        return tokenHandler.getTokenExpirationDate(token);
    }

}

