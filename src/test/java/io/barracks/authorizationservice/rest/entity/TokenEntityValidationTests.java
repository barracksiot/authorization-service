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

package io.barracks.authorizationservice.rest.entity;

import io.barracks.authorizationservice.utils.TokenEntityUtils;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.barracks.authorizationservice.utils.ViolationPathMatcher.violatesConstraintForPath;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenEntityValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void whenAllFieldsAreProvided_shouldValidate() throws Exception {
        // Given
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).isEmpty();

    }

    @Test
    public void whenLabelNull_shouldNotValidate() {
        // Given
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().label(null).build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("label"));
    }

    @Test
    public void whenLabelEmpty_shouldNotValidate() {
        // Given
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().label("").build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("label"));
    }

    @Test
    public void whenLabelBlank_shouldNotValidate() {
        // Given
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().label(" ").build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("label"));
    }

    @Test
    public void whenLabelTooLong_shouldNotValidate() {
        // Given
        final String label = IntStream.rangeClosed(1, 51).mapToObj(e -> "a").collect(Collectors.joining(""));
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().label(label).build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("label"));
    }

    @Test
    public void whenLabelNotOnlyAscii_shouldNotValidate() {
        // Given
        final TokenEntity tokenEntity = getPredefinedTokenEntityBuilder().label("!@#$%ˆ**(({}[]").build();

        // When
        final Set<ConstraintViolation<TokenEntity>> violations = validator.validate(tokenEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("label"));
    }


    private TokenEntity.TokenEntityBuilder getPredefinedTokenEntityBuilder() {
        return TokenEntityUtils.getTokenEntity().toBuilder();
    }

}
