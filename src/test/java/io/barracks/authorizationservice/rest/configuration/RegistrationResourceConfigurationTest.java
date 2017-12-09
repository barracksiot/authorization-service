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
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.model.UserStatus;
import io.barracks.authorizationservice.rest.RegistrationResource;
import io.barracks.authorizationservice.utils.UserUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = RegistrationResource.class, outputDir = "build/generated-snippets/registrations")
public class RegistrationResourceConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RegistrationResource registrationResource;

    @Value("classpath:/io/barracks/authorizationservice/rest/configuration/user-register.json")
    private Resource userRegister;

    @Test
    public void documentRegisterUser() throws Exception {
        //  Given
        final User user = objectMapper.readValue(userRegister.getInputStream(), User.class);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        doReturn(user).when(registrationResource).register(user);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(FileCopyUtils.copyToByteArray(userRegister.getInputStream()))
        );

        // Then
        verify(registrationResource).register(user);
        result.andExpect(status().isCreated())
                .andDo(document(
                        "create",
                        requestFields(
                                fieldWithPath("firstName").description("User's first name"),
                                fieldWithPath("lastName").description("User's last name"),
                                fieldWithPath("email").description("User's email address"),
                                fieldWithPath("company").description("User's company"),
                                fieldWithPath("phone").description("User's phone number")
                        )
                ));
    }

    @Test
    public void register_whenAllTheFieldsAreProvided_shouldReturnAUser() throws Exception {
        // Given
        final User user = UserUtils.getUser().toBuilder().id(null).apiKey(null).status(null).phone(null).encryptedPassword(null).build();
        final User savedUser = UserUtils.getUser().toBuilder()
                .id("ID").encryptedPassword("JGUGgu654")
                .apiKey("apiKey")
                .status(UserStatus.EMAIL_VERIFICATION_PENDING)
                .build();
        final String requestContent = "{ " +
                "\"firstName\" : \"" + user.getFirstName() + "\", " +
                "\"lastName\" : \"" + user.getLastName() + "\", " +
                "\"email\" : \"" + user.getEmail() + "\", " +
                "\"company\" : \"" + user.getCompany() + "\"" +
                " }";

        doReturn(savedUser).when(registrationResource).register(user);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
        );

        // Then
        verify(registrationResource).register(user);
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.firstName").value(savedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(savedUser.getLastName()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.encryptedPassword").doesNotExist())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.status").value(UserStatus.EMAIL_VERIFICATION_PENDING.getName()));
    }

    @Test
    public void createUser_whenEmailIsInvalid_shouldReturnStatus400() throws Exception {
        // Given
        final User user = UserUtils.getUser().toBuilder().email(null).build();
        final String requestContent = "{ " +
                "\"firstName\" : \"" + user.getFirstName() + "\", " +
                "\"lastName\" : \"" + user.getLastName() + "\"" +
                " }";

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

}
