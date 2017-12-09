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
import io.barracks.authorizationservice.exception.BarracksAuthenticationException;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.rest.DeviceAuthenticationResource;
import io.barracks.authorizationservice.rest.entity.DeviceAuthenticationEntity;
import io.barracks.authorizationservice.utils.UserUtils;
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

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = DeviceAuthenticationResource.class, outputDir = "build/generated-snippets/deviceauthentication")
public class DeviceAuthenticationResourceConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DeviceAuthenticationResource deviceAuthenticationResource;

    @Test
    public void documentAuthenticateDevice() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final User user = UserUtils.getUser().toBuilder().apiKey("User's api key").build();
        final DeviceAuthenticationEntity entity = new DeviceAuthenticationEntity(user.getApiKey());
        final ArgumentCaptor<DeviceAuthenticationEntity> argumentCaptorForMyObject = ArgumentCaptor.forClass(DeviceAuthenticationEntity.class);

        doReturn(user).when(deviceAuthenticationResource).authenticateDevice(argumentCaptorForMyObject.capture());

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/device/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entity))
        );

        // Then
        verify(deviceAuthenticationResource).authenticateDevice(argumentCaptorForMyObject.capture());
        result.andExpect(status().isOk())
                .andDo(document(
                        "authenticate",
                        requestFields(
                                fieldWithPath("apiKey").description("User's api key")
                        )
                ));

    }


    @Test
    public void authenticateDevice_whenAnApiKeyIsProvidedAndUserExists_shouldReturnAUser() throws Exception {
        // Given
        final String apiKey = "apiKey";
        ObjectMapper objectMapper = new ObjectMapper();
        final User savedUser = UserUtils.getUser();
        final DeviceAuthenticationEntity deviceAuthenticationEntity = new DeviceAuthenticationEntity(apiKey);
        final String requestContent = objectMapper.writeValueAsString(deviceAuthenticationEntity);
        doReturn(savedUser).when(deviceAuthenticationResource).authenticateDevice(deviceAuthenticationEntity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/device/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
        );

        // Then
        verify(deviceAuthenticationResource).authenticateDevice(deviceAuthenticationEntity);
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.firstName").value(savedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(savedUser.getLastName()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.apiKey").value(savedUser.getApiKey()))
                .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    public void authenticateDevice_whenApiKeyIsMissing_shouldReturn400() throws Exception {
        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/device/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void authenticateDevice_whenAnApiKeyIsProvidedAndUserDoesNotExist_shouldReturn401() throws Exception {
        // Given
        final ObjectMapper objectMapper = new ObjectMapper();
        final String apiKey = UUID.randomUUID().toString();
        final DeviceAuthenticationEntity deviceAuthenticationEntity = new DeviceAuthenticationEntity(apiKey);
        final String requestContent = objectMapper.writeValueAsString(deviceAuthenticationEntity);
        doThrow(BarracksAuthenticationException.class).when(deviceAuthenticationResource).authenticateDevice(deviceAuthenticationEntity);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/device/authenticate")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
        );

        // Then
        verify(deviceAuthenticationResource).authenticateDevice(deviceAuthenticationEntity);
        result.andExpect(status().isUnauthorized());
    }


}
