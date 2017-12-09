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

package io.barracks.authorizationservice.rest.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.barracks.authorizationservice.exception.UserNotFoundException;
import io.barracks.authorizationservice.rest.PasswordResetResource;
import io.barracks.authorizationservice.rest.entity.EmailEntity;
import io.barracks.authorizationservice.rest.entity.ResetPasswordConfirmationEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = PasswordResetResource.class, outputDir = "build/generated-snippets/passwords")
public class PasswordResetResourceConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PasswordResetResource passwordResetResource;

    @Test
    public void documentSetUserPassword() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String email = "testSetPassword@barracks.io";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);
        final ArgumentCaptor<EmailEntity> argumentCaptorForMyObject = ArgumentCaptor.forClass(EmailEntity.class);

        doReturn(resultString).when(passwordResetResource).setPassword(argumentCaptorForMyObject.capture());

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/password/set")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity))
        );

        // Then
        verify(passwordResetResource).setPassword(argumentCaptorForMyObject.capture());
        result.andExpect(status().isCreated())
                .andDo(document(
                        "set",
                        requestFields(
                                fieldWithPath("email").description("User's email")
                        )
                ));

    }

    @Test
    public void documentResetUserPassword() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String email = "testResetPassword@barracks.io";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);
        final ArgumentCaptor<EmailEntity> argumentCaptorForMyObject = ArgumentCaptor.forClass(EmailEntity.class);

        doReturn(resultString).when(passwordResetResource).resetPassword(argumentCaptorForMyObject.capture());

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity))
        );

        // Then
        verify(passwordResetResource).resetPassword(argumentCaptorForMyObject.capture());
        result.andExpect(status().isCreated())
                .andDo(document(
                        "reset",
                        requestFields(
                                fieldWithPath("email").description("User's email")
                        )
                ));

    }

    @Test
    public void documentConfirmResetUserPassword() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String token = "Token generated with the password";
        final String password = "Passw0rd1234";
        final String resultString = "Email sent";
        final ResetPasswordConfirmationEntity entity = new ResetPasswordConfirmationEntity(token, password);
        final ArgumentCaptor<ResetPasswordConfirmationEntity> argumentCaptorForMyObject = ArgumentCaptor.forClass(ResetPasswordConfirmationEntity.class);

        doReturn(resultString).when(passwordResetResource).confirmResetPassword(argumentCaptorForMyObject.capture());

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity))
        );

        // Then
        verify(passwordResetResource).confirmResetPassword(argumentCaptorForMyObject.capture());
        result.andExpect(status().isOk())
                .andDo(document(
                        "confirmReset",
                        requestFields(
                                fieldWithPath("password").description("New password for the user"),
                                fieldWithPath("token").description("Token associated with the password.")
                        )
                ));

    }

    @Test
    public void resetPassword_shouldReturnStatusCreated_whenUserExists() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);
        doReturn(resultString).when(passwordResetResource).resetPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/reset")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"email\": \"" + email + "\" }")
        );

        // Then
        verify(passwordResetResource).resetPassword(entity);
        result.andExpect(status().isCreated());
    }

    @Test
    public void resetPassword_shouldReturnStatus401_whenUserDoesNotExist() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final EmailEntity entity = new EmailEntity(email);
        doThrow(new UserNotFoundException()).when(passwordResetResource).resetPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/reset")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"email\": \"" + email + "\" }")
        );

        // Then
        verify(passwordResetResource).resetPassword(entity);
        result.andExpect(status().isUnauthorized());
    }


    @Test
    public void setPassword_shouldReturnStatusCreated_whenUserExists() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final String resultString = "Email sent";
        final EmailEntity entity = new EmailEntity(email);

        doReturn(resultString).when(passwordResetResource).setPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/reset")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"email\": \"" + email + "\" }")
        );

        // Then
        verify(passwordResetResource).resetPassword(entity);
        result.andExpect(status().isCreated());
    }

    @Test
    public void setPassword_shouldReturnStatus401_whenUserDoesNotExist() throws Exception {
        // Given
        final String email = "coucou@plop.de";
        final EmailEntity entity = new EmailEntity(email);

        doThrow(new UserNotFoundException()).when(passwordResetResource).setPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/set")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"email\": \"" + email + "\" }")
        );

        // Then
        verify(passwordResetResource).setPassword(entity);
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void confirmResetPassword_shouldReturnStatus200_whenTokenAndAssociatedUserExist() throws Exception {
        // Given
        final String token = "87568687GG";
        final String password = "Password123";
        final String resultString = "Email sent";

        final ResetPasswordConfirmationEntity entity = new ResetPasswordConfirmationEntity(token, password);
        doReturn(resultString).when(passwordResetResource).confirmResetPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"password\": \"" + password + "\", \"token\": \"" + token + "\" }")
        );

        // Then
        verify(passwordResetResource).confirmResetPassword(entity);
        result.andExpect(status().isOk());
    }

    @Test
    public void confirmResetPassword_shouldReturnStatus400_whenTheTokenIsMissing() throws Exception {
        // Given
        final String password = "password123";
        final String resultString = "Email sent";

        final ResetPasswordConfirmationEntity entity = new ResetPasswordConfirmationEntity(null, password);
        doReturn(resultString).when(passwordResetResource).confirmResetPassword(entity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{\"password\": \"" + password + "\" }")
        );

        // Then
        verifyZeroInteractions(passwordResetResource);
        result.andExpect(status().isBadRequest());
    }

}
