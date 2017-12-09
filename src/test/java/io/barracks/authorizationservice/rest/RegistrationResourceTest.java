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
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.model.UserStatus;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RegistrationResourceTest {

    @Mock
    private UserManager userManager;

    @InjectMocks
    private RegistrationResource registrationResource;

    @Test
    public void register_whenAllTheFieldsAreProvided_shouldReturnAUser() throws Exception {
        // Given
        final User user = UserUtils.getUser();
        final User savedUser = UserUtils.getUser().toBuilder().id("ID").firstName("Pierre").lastName("Henry").email("pierr2@henry.com").encryptedPassword("JGUGgu654").apiKey("apiKey")
                .status(UserStatus.EMAIL_VERIFICATION_PENDING).company("McDonald").build();

        doReturn(savedUser).when(userManager).registerUser(user);

        // When
        final User result = registrationResource.register(user);

        // Then
        verify(userManager).registerUser(user);
        assertThat(result).isEqualTo(savedUser);
    }

}