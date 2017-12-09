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

import io.barracks.authorizationservice.utils.ResetPasswordConfirmationEntityUtils;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static io.barracks.authorizationservice.utils.ViolationPathMatcher.violatesConstraintForPath;
import static org.assertj.core.api.Assertions.assertThat;

public class ResetPasswordConfirmationEntityValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void whenAllFieldsAreProvided_shouldValidate() throws Exception {
        // Given
        final ResetPasswordConfirmationEntity resetPasswordConfirmationEntity = getPredefinedResetPasswordConfirmationEntityBuilder().build();

        // When
        final Set<ConstraintViolation<ResetPasswordConfirmationEntity>> violations = validator.validate(resetPasswordConfirmationEntity);

        // Then
        assertThat(violations).isEmpty();

    }

    @Test
    public void whenTokenNull_shouldNotValidate() {
        // Given
        final ResetPasswordConfirmationEntity resetPasswordConfirmationEntity = getPredefinedResetPasswordConfirmationEntityBuilder().token(null).build();

        // When
        final Set<ConstraintViolation<ResetPasswordConfirmationEntity>> violations = validator.validate(resetPasswordConfirmationEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("token"));
    }

    @Test
    public void whenPasswordNull_shouldNotValidate() {
        // Given
        final ResetPasswordConfirmationEntity resetPasswordConfirmationEntity = getPredefinedResetPasswordConfirmationEntityBuilder().password(null).build();

        // When
        final Set<ConstraintViolation<ResetPasswordConfirmationEntity>> violations = validator.validate(resetPasswordConfirmationEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("password"));
    }

    @Test
    public void whenPasswordWeak_shouldNotValidate() {
        // Given
        final ResetPasswordConfirmationEntity resetPasswordConfirmationEntity = getPredefinedResetPasswordConfirmationEntityBuilder().password("weak").build();

        // When
        final Set<ConstraintViolation<ResetPasswordConfirmationEntity>> violations = validator.validate(resetPasswordConfirmationEntity);

        // Then
        assertThat(violations).hasOnlyOneElementSatisfying(violatesConstraintForPath("password"));
    }


    private ResetPasswordConfirmationEntity.ResetPasswordConfirmationEntityBuilder getPredefinedResetPasswordConfirmationEntityBuilder() {
        return ResetPasswordConfirmationEntityUtils.getResetPasswordConfirmationEntity().toBuilder();
    }

}
