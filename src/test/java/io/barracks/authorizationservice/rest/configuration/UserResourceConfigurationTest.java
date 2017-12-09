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
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.model.UserStatus;
import io.barracks.authorizationservice.rest.UserResource;
import io.barracks.authorizationservice.security.UserAuthentication;
import io.barracks.authorizationservice.security.jwt.CredentialsEntity;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = UserResource.class, outputDir = "build/generated-snippets/users")
public class UserResourceConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserResource userResource;

    private UserAuthentication auth;
    private User user;

    @Before
    public void setUp() {
        user = UserUtils.getUser();
        auth = new UserAuthentication(user);
    }

    @Test
    public void documentGetCurrentUser() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        when(userResource.getCurrentUser(auth)).thenReturn(user);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.get("/me")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(userResource).getCurrentUser(auth);
        result.andExpect(status().isOk())
                .andDo(document(
                        "get-authenticated",
                        responseFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("firstName").description("User's first name"),
                                fieldWithPath("lastName").description("User's last name"),
                                fieldWithPath("email").description("User's email address"),
                                fieldWithPath("apiKey").description("User's api key"),
                                fieldWithPath("status").description("User's status"),
                                fieldWithPath("company").description("User's company"),
                                fieldWithPath("phone").description("User's phone number")
                        )
                ));
    }

    @Test
    public void documentLoginUser() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final CredentialsEntity credentials = CredentialsEntity.builder().username("test@barracks.io").password("P4ssw0rd").build();
        when(userResource.getCurrentUser(auth)).thenReturn(user);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/login")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials))
        );

        // Then
        verify(userResource).getCurrentUser(auth);
        result.andExpect(status().isOk())
                .andDo(document(
                        "login",
                        requestFields(
                                fieldWithPath("username").description("The user's email"),
                                fieldWithPath("password").description("The user's password")
                        ),
                        responseFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("firstName").description("User's first name"),
                                fieldWithPath("lastName").description("User's last name"),
                                fieldWithPath("email").description("User's email address"),
                                fieldWithPath("apiKey").description("User's api key"),
                                fieldWithPath("status").description("User's status"),
                                fieldWithPath("company").description("User's company"),
                                fieldWithPath("phone").description("User's phone number")
                        )
                ));
    }

    @Test
    public void documentGetUserById() throws Exception {
        //  Given
        final String userId = user.getId();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        when(userResource.getUserById(userId)).thenReturn(user);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.get("/users/{userId}", userId)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(userResource).getUserById(userId);
        result.andExpect(status().isOk())
                .andDo(document(
                        "get-by-id",
                        pathParameters(
                                parameterWithName("userId").description("User's ID")
                        ),
                        responseFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("firstName").description("User's first name"),
                                fieldWithPath("lastName").description("User's last name"),
                                fieldWithPath("email").description("User's email address"),
                                fieldWithPath("apiKey").description("User's api key"),
                                fieldWithPath("status").description("User's status"),
                                fieldWithPath("company").description("User's company"),
                                fieldWithPath("phone").description("User's phone number")
                        )
                ));
    }

    @Test
    public void documentEditUserStatus() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final String userId = user.getId();
        final UserStatus status = UserStatus.PAYMENT_PENDING;
        when(userResource.editUserStatus(userId, status.name())).thenReturn(user.toBuilder().status(status).build());

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.put("/users/{userId}/status/{status}", userId, status)
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(userResource).editUserStatus(userId, status.name());
        result.andExpect(status().isOk())
                .andDo(document(
                        "edit-status",
                        pathParameters(
                                parameterWithName("userId").description("User's ID"),
                                parameterWithName("status").description("The new status")
                        ),
                        responseFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("firstName").description("User's first name"),
                                fieldWithPath("lastName").description("User's last name"),
                                fieldWithPath("email").description("User's email address"),
                                fieldWithPath("apiKey").description("User's api key"),
                                fieldWithPath("status").description("User's status"),
                                fieldWithPath("company").description("User's company"),
                                fieldWithPath("phone").description("User's phone number")
                        )
                ));
    }

    @Test
    public void getCurrentUser_whenUserExists_shouldReturnUser() throws Exception {
        // Give
        when(userResource.getCurrentUser(auth)).thenReturn(user);

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/me")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON));

        // Then
        verify(userResource).getCurrentUser(auth);
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    public void postCurrentUser_whenUserExists_shouldReturnUser() throws Exception {
        // Give
        doReturn(user).when(userResource).getCurrentUser(auth);

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/login")
                        .principal(auth)
                        .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON));

        // Then
        verify(userResource).getCurrentUser(auth);
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    public void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        // Give
        final String userId = UUID.randomUUID().toString();
        final User user = UserUtils.getUser().toBuilder().id(userId).build();
        when(userResource.getUserById(userId)).thenReturn(user);

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON));

        // Then
        verify(userResource).getUserById(userId);
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    public void getUserById_whenUserIsNotFound_shouldReturnUnauthorized() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        doThrow(UserNotFoundException.class).when(userResource).getUserById(userId);

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/users/" + userId)
                        .accept(MediaType.APPLICATION_JSON_UTF8, MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isUnauthorized());
    }


    @Test
    public void editUserStatus_whenUserIdAndStatusAreValid_shouldUpdateTheUserAndReturnIt() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UserStatus status = UserStatus.ACTIVE;
        final User user = UserUtils.getUser().toBuilder().id(userId).build();
        doReturn(user).when(userResource).editUserStatus(userId, status.name());

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put("/users/" + userId + "/status/" + status.name())
                        .accept(MediaType.APPLICATION_JSON_UTF8));

        // Then
        verify(userResource).editUserStatus(userId, status.name());
        result.andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(user)));
    }

    @Test
    public void editUserStatus_whenUserIsNotFound_shouldReturnUserNotFoundException() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UserStatus status = UserStatus.ACTIVE;
        doThrow(UserNotFoundException.class).when(userResource).editUserStatus(userId, status.name());

        // When
        ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put("/users/" + userId + "/status/" + status.name())
                        .accept(MediaType.APPLICATION_JSON_UTF8));

        // Then
        result.andExpect(status().isUnauthorized());
        verify(userResource).editUserStatus(userId, status.name());
    }
}
