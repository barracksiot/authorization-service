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

package io.barracks.authorizationservice.repository;

import io.barracks.authorizationservice.model.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    public void getTokens_whenNoToken_shouldReturnEmptyList() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final Pageable pageable = new PageRequest(0, 10);

        // When
        final Page<Token> result = tokenRepository.getTokensByUserId(userId, pageable);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTokens_whenTokens_shouldReturnTokensList() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final Pageable pageable = new PageRequest(0, 10);
        final ArrayList<Token> expected = getTokens(userId);

        expected.stream().forEach(token -> tokenRepository.save(token));

        // When
        final Page<Token> result = tokenRepository.getTokensByUserId(userId, pageable);

        // Then
        assertThat(result).containsOnlyElementsOf(expected);
    }

    @Test
    public void getTokens_whenTokensForMoreThanOneUser_shouldReturnCurrentUserTokens() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final Pageable pageable = new PageRequest(0, 10);
        final ArrayList<Token> expected = getTokens(userId);
        final ArrayList<Token> otherTokens = getTokens(UUID.randomUUID().toString());

        expected.stream().forEach(token -> tokenRepository.save(token));
        otherTokens.stream().forEach(token -> tokenRepository.save(token));

        // When
        final Page<Token> result = tokenRepository.getTokensByUserId(userId, pageable);

        // Then
        assertThat(result).containsOnlyElementsOf(expected);
    }

    @Test
    public void getTokens_whenManyTokens_shouldReturnPagesList() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final Pageable pageable = new PageRequest(1, 5);
        final ArrayList<Token> tokens = getTokens(userId);
        final List<Token> expected = tokens.subList(5, 10);

        tokens.stream().forEach(token -> tokenRepository.save(token));

        // When
        final Page<Token> result = tokenRepository.getTokensByUserId(userId, pageable);

        // Then
        assertThat(result).containsOnlyElementsOf(expected);
    }

    private ArrayList<Token> getTokens(String userId) {
        ArrayList<Token> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(Token.builder()
                    .userId(userId)
                    .label(UUID.randomUUID().toString())
                    .value(UUID.randomUUID().toString())
                    .build()
            );
        }
        return list;
    }


    @Test
    public void updateTokenField_whenTokenDoesNotExist_shouldReturnNull() {
        // Given
        final String tokenId = UUID.randomUUID().toString();

        // Then When
        assertThat(tokenRepository.revokeToken(tokenId)).isNull();
    }


    @Test
    public void updateTokenField_whenTokenExists_shouldReturnUpdatedToken() {
        // Given
        final String tokenId = UUID.randomUUID().toString();
        final Token token = Token.builder().id(tokenId).build();
        tokenRepository.save(token);

        // When
        final Token result = tokenRepository.revokeToken(tokenId);

        // Then
        assertThat(result.isRevoked()).isTrue();
    }

}
