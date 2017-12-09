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
import io.barracks.authorizationservice.security.BarracksUserDetails;
import io.barracks.authorizationservice.security.UserAuthentication;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest {


    @Mock
    private UserManager userManager;

    @InjectMocks
    private UserResource userResource;

    private UserAuthentication auth;
    private User user;

    @Before
    public void setUp() {
        user = UserUtils.getUser();
        auth = new UserAuthentication(new BarracksUserDetails(user));
    }

    @Test
    public void getCurrentUser_whenUserExists_shouldReturnUserWithHiddenClientSecret() throws Exception {

        // When
        final User userResult = userResource.getCurrentUser(auth);

        // Then
        assertThat(userResult.toString()).isEqualTo(user.toString());
    }


    @Test
    public void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        // Give
        final String userId = UUID.randomUUID().toString();
        final User user = UserUtils.getUser().toBuilder().id(userId).build();
        doReturn(user).when(userManager).getUserById(userId);

        // When
        final User userResult = userResource.getUserById(userId);

        // Then
        verify(userManager).getUserById(userId);
        assertThat(userResult).isEqualTo(user);
    }


    @Test
    public void editUserStatus_whenUserIdAndStatusAreValid_shouldUpdateTheUserAndReturnIt() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UserStatus status = UserStatus.ACTIVE;
        final User user = UserUtils.getUser().toBuilder().id(userId).status(status).build();
        doReturn(user).when(userManager).editUserStatus(userId, status);

        // When
        final User result = userResource.editUserStatus(userId, status.getName());

        // Then
        verify(userManager).editUserStatus(userId, status);
        assertThat(result).isEqualTo(user);
    }

}
