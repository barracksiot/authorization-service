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

import io.barracks.authorizationservice.exception.*;
import io.barracks.authorizationservice.mail.PasswordConfirmAccountMailer;
import io.barracks.authorizationservice.mail.PasswordResetMailer;
import io.barracks.authorizationservice.model.PasswordResetToken;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.model.UserStatus;
import io.barracks.authorizationservice.repository.PasswordResetTokenRepository;
import io.barracks.authorizationservice.repository.UserRepository;
import io.barracks.authorizationservice.security.ApiKeyGenerator;
import io.barracks.authorizationservice.utils.PasswordResetTokenUtils;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserManagerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApiKeyGenerator apiKeyGenerator;

    @Mock
    private PasswordConfirmAccountMailer passwordConfirmAccountMailer;

    @Mock
    private PasswordResetMailer passwordResetMailer;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private UserManager userManager = new UserManager();

    @Test
    public void registerUser_whenAllIsFine_shouldInsertUser() throws Exception {
        // Given
        final User.UserBuilder base = User.builder().firstName("Joe").lastName("Dalton").email("joe@dalton.fw").company("McDonald");
        final User user = base.build();
        final User toInsert = base.status(UserStatus.EMAIL_VERIFICATION_PENDING).build();
        final User insertedUser = base.id(UUID.randomUUID().toString()).build();
        when(userRepository.insert(toInsert)).thenReturn(insertedUser);

        // When
        final User result = userManager.registerUser(user);

        // Then
        verify(userRepository).insert(toInsert);
        verifyZeroInteractions(passwordEncoder);
        assertThat(result).isNotNull().isEqualTo(insertedUser);
    }

    @Test
    public void registerUser_whenUserEmailAlreadyExists_shouldThrowAnException() throws Exception {
        // Given
        final User user = User.builder().firstName("Joe").lastName("Dalton").email("joe@dalton.fw")
                .company("McDonald").status(UserStatus.EMAIL_VERIFICATION_PENDING).build();
        final DuplicateKeyException duplicateKeyException = new DuplicateKeyException("");
        when(userRepository.insert(user)).thenThrow(duplicateKeyException);

        // When / Then
        assertThatExceptionOfType(UserAlreadyExistsException.class)
                .isThrownBy(() -> userManager.registerUser(user))
                .withCause(duplicateKeyException)
                .has(new Condition<>((UserAlreadyExistsException e) -> e.getUser().equals(user), "Exception contains user"));
        verify(userRepository).insert(user);
    }

    @Test
    public void editUserStatus_whenUserIsNotFound_shouldReturnUserNotFoundException() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UserStatus status = UserStatus.ACTIVE;
        when(userRepository.updateUserStatus(userId, status)).thenReturn(Optional.empty());

        // When - Then
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userManager.editUserStatus(userId, status));
        verify(userRepository).updateUserStatus(userId, status);
    }

    @Test
    public void editUserStatus_whenUserIdAndStatusAreValid_shouldUpdateTheUserAndReturnIt() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UserStatus status = UserStatus.ACTIVE;
        final User user = User.builder().id(userId).firstName("Joe").lastName("Dalton").email("joe@dalton.fw")
                .encryptedPassword("234567uhgfdewert").apiKey("key").disabled(false).status(UserStatus.EMAIL_VERIFICATION_PENDING).company("McDonald").build();
        when(userRepository.updateUserStatus(userId, status)).thenReturn(Optional.of(user));

        // When
        User response = userManager.editUserStatus(userId, status);

        // Then
        verify(userRepository).updateUserStatus(userId, status);
        assertThat(response).isEqualTo(user);
    }

    @Test
    public void setPassword_whenUserEmailDoesNotExist_shouldThrowAnException() throws Exception {
        // Given
        final String email = "joe@dalton.fw";
        final UsernameNotFoundException exception = new UsernameNotFoundException("");
        when(userRepository.findByEmail(email)).thenThrow(exception);

        // When / Then
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userManager.initPassword(email))
                .withCause(exception);
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void setPassword_whenUserEmailExists_shouldSendAnEmailAndCreateToken() throws Exception {
        // Given
        final User user = User.builder().id("123").firstName("Joe").lastName("Dalton").email("joe@dalton.fw")
                .encryptedPassword("").disabled(false).status(UserStatus.ACTIVE).company("McDonald").build();
        final PasswordResetToken insertedPasswordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token("&^*^^*^").userEmail(user.getEmail()).build();
        when(passwordResetTokenRepository.insert(any(PasswordResetToken.class))).thenReturn(insertedPasswordResetToken);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // When
        userManager.initPassword(user.getEmail());

        // Then
        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordResetTokenRepository).insert(any(PasswordResetToken.class));
        verify(passwordConfirmAccountMailer).send(insertedPasswordResetToken);
    }

    @Test
    public void resetPassword_whenUserEmailDoesNotExist_shouldThrowAnException() throws Exception {
        // Given
        final String email = "joe@dalton.fw";
        final UsernameNotFoundException exception = new UsernameNotFoundException("");
        when(userRepository.findByEmail(email)).thenThrow(exception);

        // When / Then
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userManager.resetPassword(email))
                .withCause(exception);
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void resetPassword_whenUserEmailExists_shouldSendAnEmailAndCreateToken() throws Exception {
        // Given
        final User user = User.builder().id("123").firstName("Joe").lastName("Dalton").email("joe@dalton.fw").disabled(false).status(UserStatus.ACTIVE).build();
        final PasswordResetToken insertedPasswordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token("&^*^^*^").userEmail(user.getEmail()).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.insert(any(PasswordResetToken.class))).thenReturn(insertedPasswordResetToken);

        // When
        userManager.resetPassword(user.getEmail());

        // Then
        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordResetTokenRepository).insert(any(PasswordResetToken.class));
        verify(passwordResetMailer).send(insertedPasswordResetToken);
    }

    @Test
    public void confirmResetPassword_whenTokenAndUserExist_shouldEncryptPasswordAndUpdateUserAndUpdateToken() throws Exception {
        // Given
        final User user = User.builder().id("123").firstName("Joe").lastName("Dalton").email("joe@dalton.fw").disabled(false).status(UserStatus.ACTIVE).build();
        final String password = "password";
        final String encodedPassword = "encodedPassword";
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token("&^*^^*^").userEmail(user.getEmail()).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByToken(passwordResetToken.getToken())).thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // When
        userManager.confirmResetPassword(passwordResetToken.getToken(), password);

        // Then
        verify(passwordEncoder).encode(password);
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).updateUserPassword(user.getId(), encodedPassword);
        final ArgumentCaptor<PasswordResetToken> passwordResetTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(passwordResetTokenArgumentCaptor.capture());
        verify(passwordResetTokenRepository).findByToken(passwordResetToken.getToken());

        assertThat(passwordResetTokenArgumentCaptor.getValue().getExpiryDate()).isBefore(new Date());
    }

    @Test
    public void confirmResetPassword_shouldUpdateUserStatus_whenUserIsPendingVerification() throws Exception {
        // Given
        final User user = User.builder().id("123").firstName("Joe").lastName("Dalton").email("joe@dalton.fw").disabled(false).status(UserStatus.EMAIL_VERIFICATION_PENDING).build();
        final String password = "password";
        final String encodedPassword = "encodedPassword";
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token("&^*^^*^").userEmail(user.getEmail()).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByToken(passwordResetToken.getToken())).thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // When
        userManager.confirmResetPassword(passwordResetToken.getToken(), password);

        // Then
        verify(passwordEncoder).encode(password);
        verify(userRepository).findByEmail(user.getEmail());
        verify(userRepository).updateUserPassword(user.getId(), encodedPassword);
        verify(userRepository).updateUserStatus(user.getId(), UserStatus.SUBSCRIPTION_PENDING);
        final ArgumentCaptor<PasswordResetToken> passwordResetTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(passwordResetTokenArgumentCaptor.capture());
        verify(passwordResetTokenRepository).findByToken(passwordResetToken.getToken());
    }

    @Test
    public void confirmResetPassword_whenUserEmailDoesNotExist_shouldThrowAnException() throws Exception {
        // Given
        final String password = "password";
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder()
                .token("token")
                .userEmail("joe@dalton.fw")
                .build();
        when(passwordResetTokenRepository.findByToken(passwordResetToken.getToken())).thenReturn(Optional.of(passwordResetToken));
        when(userRepository.findByEmail(passwordResetToken.getUserEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatExceptionOfType(DataIntegrityException.class)
                .isThrownBy(() -> userManager.confirmResetPassword(passwordResetToken.getToken(), password));
        verify(passwordResetTokenRepository).findByToken(passwordResetToken.getToken());
        verify(userRepository).findByEmail(passwordResetToken.getUserEmail());
    }

    @Test
    public void confirmResetPassword_whenTokenDoesNotExist_shouldThrowAnException() throws Exception {
        // Given
        final String password = "password";
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().token("token").userEmail("joe@dalton.fw").build();
        when(passwordResetTokenRepository.findByToken(passwordResetToken.getToken())).thenReturn(Optional.empty());

        // When / Then
        assertThatExceptionOfType(PasswordResetTokenNotFoundException.class)
                .isThrownBy(() -> userManager.confirmResetPassword(passwordResetToken.getToken(), password));
        verify(passwordResetTokenRepository).findByToken(passwordResetToken.getToken());
    }

    @Test
    public void confirmResetPassword_whenTokenIsExpired_shouldThrowAnException() throws Exception {
        // Given
        final String password = "password";
        final PasswordResetToken passwordResetToken = PasswordResetTokenUtils.getPasswordResetToken().toBuilder().expiryDate(new Date()).build();
        when(passwordResetTokenRepository.findByToken(passwordResetToken.getToken())).thenReturn(Optional.of(passwordResetToken));

        // When / Then
        assertThatExceptionOfType(PasswordResetTokenExpiredException.class)
                .isThrownBy(() -> userManager.confirmResetPassword(passwordResetToken.getToken(), password));
        verify(passwordResetTokenRepository).findByToken(passwordResetToken.getToken());
    }
}