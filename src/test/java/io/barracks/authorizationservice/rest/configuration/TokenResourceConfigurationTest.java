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
import io.barracks.authorizationservice.exception.UnknownTokenException;
import io.barracks.authorizationservice.model.Token;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.rest.TokenResource;
import io.barracks.authorizationservice.rest.entity.TokenEntity;
import io.barracks.authorizationservice.security.UserAuthentication;
import io.barracks.authorizationservice.utils.PageableHelper;
import io.barracks.authorizationservice.utils.TokenUtils;
import io.barracks.authorizationservice.utils.UserUtils;
import io.barracks.commons.test.PagedResourcesUtils;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@BarracksResourceTest(controllers = TokenResource.class, outputDir = "build/generated-snippets/tokens")
public class TokenResourceConfigurationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TokenResource tokenResource;

    private UserAuthentication auth;
    private User user;

    @Before
    public void setUp() {
        user = UserUtils.getUser();
        auth = new UserAuthentication(user);
    }

    @Test
    public void documentCreateToken() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final TokenEntity tokenEntity = TokenEntity.builder()
                .label("The label used to create a token entity !")
                .build();
        final Token response = Token.builder()
                .id(UUID.randomUUID().toString())
                .label(tokenEntity.getLabel())
                .value("The value of the token that has been created.")
                .userId(UUID.randomUUID().toString())
                .startDate(new Date())
                .build();
        doReturn(response).when(tokenResource).createToken(tokenEntity, auth);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.post("/tokens")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(response))
        );

        // Then
        verify(tokenResource).createToken(tokenEntity, auth);
        result.andExpect(status().isCreated())
                .andDo(document(
                        "create",
                        requestFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("userId").ignored(),
                                fieldWithPath("label").description("The token's label that was used to create it."),
                                fieldWithPath("value").description("The token's value"),
                                fieldWithPath("startDate").description("The token's date, which is equal to the moment it was created."),
                                fieldWithPath("revoked").description("Indicates if the token is revoked or not")
                        )
                ));
    }

    @Test
    public void documentGetTokens() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final Pageable pageable = new PageRequest(0, 10);
        final Token response = Token.builder()
                .id(UUID.randomUUID().toString())
                .label("The label of the token that is considered.")
                .value("The value of the token that is considered.")
                .userId(UUID.randomUUID().toString())
                .startDate(new Date())
                .build();
        final Page<Token> page = new PageImpl<>(Collections.singletonList(response));
        final PagedResources<Resource<Token>> expected = PagedResourcesUtils.<Token>getPagedResourcesAssembler().toResource(page);
        doReturn(expected).when(tokenResource).getTokens(auth, pageable);

        // When
        final ResultActions result = mvc.perform(
                RestDocumentationRequestBuilders.get("/tokens")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .params(PageableHelper.queryFrom(pageable))
        );

        // Then
        verify(tokenResource).getTokens(auth, pageable);
        result.andExpect(status().isOk())
                .andDo(document(
                        "list",
                        responseFields(
                                fieldWithPath("_embedded.tokens[]").description("The tokens list"),
                                fieldWithPath("_embedded.tokens[].label").description("The token's label"),
                                fieldWithPath("_embedded.tokens[].value").description("The token's value"),
                                fieldWithPath("_embedded.tokens[].startDate").description("The token's creation date"),
                                fieldWithPath("_embedded.tokens[].revoked").description("Indicates if the token is revoked or not"),
                                fieldWithPath("_links").ignored(),
                                fieldWithPath("page").ignored()
                        )
                ));
    }

    @Test
    public void documentRevokeToken() throws Exception {
        //  Given
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final Token token = TokenUtils.getToken().toBuilder()
                .label("Token's label.")
                .value("The value of the token that is considered.")
                .build();
        doReturn(token).when(tokenResource).revokeToken(token.getValue());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put("/tokens/{token}/revoke", token.getValue())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(tokenResource).revokeToken(token.getValue());
        result.andExpect(status().isOk())
                .andDo(document(
                        "revoke",
                        responseFields(
                                fieldWithPath("id").ignored(),
                                fieldWithPath("userId").ignored(),
                                fieldWithPath("label").description("The token's label."),
                                fieldWithPath("value").description("The token's value."),
                                fieldWithPath("startDate").description("The token's date, which is equal to the moment it was created."),
                                fieldWithPath("revoked").description("Indicates if the token is revoked or not")
                        )
                ));
    }

    @Test
    public void getTokens_whenAllFieldsAreProvided_shouldCallResourceAndReturnResult() throws Exception {
        //Given
        final Pageable pageable = new PageRequest(0, 10);
        final Token token1 = Token.builder().id(UUID.randomUUID().toString()).build();
        final Token token2 = Token.builder().id(UUID.randomUUID().toString()).build();
        final Page<Token> page = new PageImpl<>(Arrays.asList(token1, token2));
        final PagedResources<Resource<Token>> expected = PagedResourcesUtils.<Token>getPagedResourcesAssembler().toResource(page);
        doReturn(expected).when(tokenResource).getTokens(auth, pageable);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/tokens")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .params(PageableHelper.queryFrom(pageable))
                        .principal(auth)
        );

        //Then
        verify(tokenResource).getTokens(auth, pageable);
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tokens", hasSize(page.getNumberOfElements())))
                .andExpect(jsonPath("$._embedded.tokens[0]").value(IsMapContaining.hasEntry("id", token1.getId())))
                .andExpect(jsonPath("$._embedded.tokens[1]").value(IsMapContaining.hasEntry("id", token2.getId())));
    }

    @Test
    public void getTokens_whenAllIsFineWithNoTokens_shouldCallResourceAndReturnEmptyList() throws Exception {
        //Given
        final Pageable pageable = new PageRequest(0, 10);
        final Page<Token> page = new PageImpl<>(Collections.emptyList());
        final PagedResources<Resource<Token>> expected = PagedResourcesUtils.<Token>getPagedResourcesAssembler().toResource(page);

        doReturn(expected).when(tokenResource).getTokens(auth, pageable);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get("/tokens")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .params(PageableHelper.queryFrom(pageable))
                        .principal(auth)

        );

        //Then
        verify(tokenResource).getTokens(auth, pageable);
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tokens").doesNotExist());
    }

    @Test
    public void postToken_whenAllIsFine_shouldCreateToken() throws Exception {
        // Given
        final TokenEntity tokenEntity = TokenEntity.builder().label("coucou").build();
        final String requestContent = "{ \"label\":\"coucou\"}";
        final Token token = TokenUtils.getToken();

        doReturn(token).when(tokenResource).createToken(tokenEntity, auth);
        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/tokens")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
                        .principal(auth)
        );

        // Then
        verify(tokenResource).createToken(tokenEntity, auth);
        result.andExpect(status().isCreated());
    }

    @Test
    public void postToken_whenLabelIsMissing_shouldReturnBadRequest() throws Exception {
        // Given
        final String requestContent = "{}";

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post("/tokens")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(requestContent)
                        .principal(auth)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void revokeToken_whenAllIsFine_shouldCallResourceAndReturnRevokedToken() throws Exception {
        // Given
        final Token token = Token.builder()
                .label("Test label")
                .value(UUID.randomUUID().toString())
                .id(UUID.randomUUID().toString())
                .userId(user.getId()).build();
        doReturn(token).when(tokenResource).revokeToken(token.getValue());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put("/tokens/{token}/revoke", token.getValue())
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(token.getId()))
                .andExpect(jsonPath("$.userId").value(token.getUserId()));
        verify(tokenResource).revokeToken(token.getValue());
    }

    @Test
    public void revokeToken_whenTokenDoesNotExist_shouldReturnTokenNotFound() throws Exception {
        // Given"
        final String wrongValue = "wrongValue";
        doThrow(UnknownTokenException.class).when(tokenResource).revokeToken(wrongValue);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put("/tokens/{token}/revoke", wrongValue)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Then
        verify(tokenResource).revokeToken(wrongValue);
        result.andExpect(status().isNotFound());
    }

}
