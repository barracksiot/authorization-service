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

package io.barracks.authorizationservice.rest;

import io.barracks.authorizationservice.manager.UserManager;
import io.barracks.authorizationservice.rest.entity.EmailEntity;
import io.barracks.authorizationservice.rest.entity.ResetPasswordConfirmationEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PasswordResetResourceTest {

    @Mock
    private UserManager manager;

    @InjectMocks
    private PasswordResetResource passwordResetResource;


    @Test
    public void resetPassword_shouldReturnStatusCreated_whenUserExists() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);

        doNothing().when(manager).resetPassword(entity.getEmail());

        // When
        final String result = passwordResetResource.resetPassword(entity);

        // Then
        verify(manager).resetPassword(entity.getEmail());
        assertThat(result).isEqualTo(resultString);
    }

    @Test
    public void setPassword_shouldReturnStatusCreated_whenUserExists() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);

        doNothing().when(manager).initPassword(entity.getEmail());

        // When
        final String result = passwordResetResource.setPassword(entity);

        // Then
        verify(manager).initPassword(entity.getEmail());
        assertThat(result).isEqualTo(resultString);
    }

    @Test
    public void confirmResetPassword_shouldReturnStatus200_whenTokenAndAssociatedUserExist() throws Exception {
        // Given
        final String token = "87568687GG";
        final String password = "Password123";
        final String resultString = "Email sent";

        final ResetPasswordConfirmationEntity entity = new ResetPasswordConfirmationEntity(token, password);
        doNothing().when(manager).confirmResetPassword(entity.getToken(), entity.getPassword());

        // When
        final String result = passwordResetResource.confirmResetPassword(entity);

        // Then
        verify(manager).confirmResetPassword(entity.getToken(), entity.getPassword());
        assertThat(result).isEqualTo(resultString);
    }

}