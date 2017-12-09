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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Service
public class UserManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordConfirmAccountMailer passwordConfirmAccountMailer;

    @Autowired
    private PasswordResetMailer passwordResetMailer;

    @Autowired
    private ApiKeyGenerator apiKeyGenerator;

    public User registerUser(User user) {
        try {
            User toSave = User.builder()
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .apiKey(apiKeyGenerator.generate())
                    .status(UserStatus.EMAIL_VERIFICATION_PENDING)
                    .company(user.getCompany())
                    .phone(user.getPhone())
                    .build();
            return userRepository.insert(toSave);
        } catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException(user, e);
        }
    }

    public User editUserStatus(String userId, UserStatus status) {
        return userRepository.updateUserStatus(userId, status).orElseThrow(UserNotFoundException::new);
    }

    public void initPassword(String userEmail) {
        final PasswordResetToken insertedResetToken = createPasswordResetToken(userEmail);
        passwordConfirmAccountMailer.send(insertedResetToken);
    }

    public void resetPassword(String userEmail) {
        final PasswordResetToken insertedResetToken = createPasswordResetToken(userEmail);
        passwordResetMailer.send(insertedResetToken);
    }

    private PasswordResetToken createPasswordResetToken(String userEmail) {
        try {
            getUserByEmail(userEmail);
            final PasswordResetToken token = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .userEmail(userEmail)
                    .expiryDate(Date.from(LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.UTC)))
                    .build();
            return passwordResetTokenRepository.insert(token);
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException(e);
        }
    }

    public void confirmResetPassword(String token, String password) {
        final PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new PasswordResetTokenNotFoundException(token));
        if (passwordResetToken.getExpiryDate().after(new Date())) {
            final User user = userRepository
                    .findByEmail(passwordResetToken.getUserEmail())
                    .orElseThrow(() -> new DataIntegrityException(PasswordResetToken.class, passwordResetToken.getId()));
            userRepository.updateUserPassword(user.getId(), passwordEncoder.encode(password));
            if (UserStatus.EMAIL_VERIFICATION_PENDING == user.getStatus()) {
                userRepository.updateUserStatus(user.getId(), UserStatus.SUBSCRIPTION_PENDING);
            }
            passwordResetTokenRepository.save(passwordResetToken.toBuilder().expiryDate(new Date()).build());
        } else {
            throw new PasswordResetTokenExpiredException(token);
        }
    }

    public User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey).orElseThrow(BarracksAuthenticationException::new);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

}
