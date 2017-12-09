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

import io.barracks.authorizationservice.utils.EmailEntityUtils;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static io.barracks.authorizationservice.utils.ViolationPathMatcher.violatesConstraintForPath;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailEntityValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void getEmailEntity_whenAllFieldsAreProvided_shouldCallResourceAndReturnResult() throws Exception {
        // Given
        final EmailEntity emailEntity = getPredefinedEmailEntityBuilder().build();

        // When
        final Set<ConstraintViolation<EmailEntity>> violations = validator.validate(emailEntity);

        // Then
        assertThat(violations).isEmpty();

    }

    @Test
    public void whenEmailNull_shouldNotValidate() {
        // Given
        final EmailEntity emailEntity = getPredefinedEmailEntityBuilder().email(null).build();

        // When
        final Set<ConstraintViolation<EmailEntity>> violations = validator.validate(emailEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("email"));
    }

    @Test
    public void whenEmailEmpty_shouldNotValidate() {
        // Given
        final EmailEntity emailEntity = getPredefinedEmailEntityBuilder().email("").build();

        // When
        final Set<ConstraintViolation<EmailEntity>> violations = validator.validate(emailEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("email"));
    }

    @Test
    public void whenEmailBlank_shouldNotValidate() {
        // Given
        final EmailEntity emailEntity = getPredefinedEmailEntityBuilder().email(" ").build();

        // When
        final Set<ConstraintViolation<EmailEntity>> violations = validator.validate(emailEntity);

        // Then
        assertThat(violations).hasSize(2).allSatisfy(violatesConstraintForPath("email"));
    }


    @Test
    public void whenEmailNotValid_shouldNotValidate() {
        // Given
        final EmailEntity emailEntity = getPredefinedEmailEntityBuilder().email("!@#$%ˆ**(({}[]").build();

        // When
        final Set<ConstraintViolation<EmailEntity>> violations = validator.validate(emailEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("email"));
    }

    private EmailEntity.EmailEntityBuilder getPredefinedEmailEntityBuilder() {
        return EmailEntityUtils.getEmailEntity().toBuilder();
    }

}
