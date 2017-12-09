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

import io.barracks.authorizationservice.model.PasswordResetToken;
import io.barracks.authorizationservice.utils.PasswordResetTokenUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private MongoOperations operations;

    @Before
    public void setUp() {
        operations.remove(new Query(), operations.getCollectionName(PasswordResetToken.class));
    }

    @Test
    public void findByToken_WhenTokenExists_ShouldReturnPasswordResetToken() {
        // Given
        final String token = UUID.randomUUID().toString();
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token(token).build();
        passwordResetTokenRepository.save(passwordResetToken);

        // When
        final Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken(token);
        //Then
        assertThat(result).isPresent().contains(passwordResetToken);
    }

    @Test
    public void findByToken_WhenTokenDoesNotExists_ShouldReturnEmpty() {
        // Given
        final String token = UUID.randomUUID().toString();

        // When
        final Optional<PasswordResetToken> result = passwordResetTokenRepository.findByToken(token);
        //Then
        assertThat(result).isEmpty();
    }

}
