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

import io.barracks.authorizationservice.manager.TokenManager;
import io.barracks.authorizationservice.model.Token;
import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.rest.entity.TokenEntity;
import io.barracks.authorizationservice.security.BarracksUserDetails;
import io.barracks.authorizationservice.security.UserAuthentication;
import io.barracks.authorizationservice.utils.TokenUtils;
import io.barracks.commons.test.PagedResourcesUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TokenResourceTest {

    @Mock
    private TokenManager manager;

    private PagedResourcesAssembler<Token> tokenPagedResourcesAssembler = PagedResourcesUtils.getPagedResourcesAssembler();

    @InjectMocks
    private TokenResource tokenResource;

    private User user;
    private Authentication auth;

    @Before
    public void setUp() {
        this.tokenResource = new TokenResource(manager, tokenPagedResourcesAssembler);

        user = User.builder().email("test@barracks.io").id(UUID.randomUUID().toString()).build();
        auth = new UserAuthentication(new BarracksUserDetails(user));
    }

    @Test
    public void createToken_whenAllFieldsAreProvided_shouldReturnToken() throws Exception {
        // Given
        final TokenEntity tokenEntity = new TokenEntity("My test token");
        final Token savedToken = TokenUtils.getToken().toBuilder()
                .label("My test token")
                .build();

        when(manager.createToken(user, tokenEntity.getLabel())).thenReturn(savedToken);

        // When
        final Token result = tokenResource.createToken(tokenEntity, auth);

        // Then
        verify(manager).createToken(user, tokenEntity.getLabel());
        assertThat(result).isEqualTo(savedToken);
    }


    @Test
    public void getTokens_whenAllIsFine_shouldCallManagerAndReturnTokensList() {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final Token token1 = Token.builder().userId(user.getId()).label("token1").build();
        final Token token2 = Token.builder().userId(user.getId()).label("token22").build();
        final Page<Token> page = new PageImpl<>(Lists.newArrayList(token1, token2));
        final PagedResources<Resource<Token>> expected = tokenPagedResourcesAssembler.toResource(page);

        when(manager.getTokensForUser(user, pageable)).thenReturn(page);

        // When
        final PagedResources<Resource<Token>> result = tokenResource.getTokens(auth, pageable);

        // Then
        verify(manager).getTokensForUser(user, pageable);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getTokens_whenAllIsFineWithNoTokens_shouldCallManagerAndReturnEmptyList() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final Page<Token> page = new PageImpl<>(Lists.newArrayList(Collections.emptyList()));
        final PagedResources<Resource<Token>> expected = tokenPagedResourcesAssembler.toResource(page);

        when(manager.getTokensForUser(user, pageable)).thenReturn(page);

        // When
        final PagedResources<Resource<Token>> result = tokenResource.getTokens(auth, pageable);

        // Then
        verify(manager).getTokensForUser(user, pageable);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void revokeToken_whenAllIsFine_shouldCallManagerAndReturnRevokedTokenOfCurrentUser() {
        // Given
        final Token token = TokenUtils.getToken();
        doReturn(token).when(manager).revokeToken(token.getValue());

        // When
        final Token result = tokenResource.revokeToken(token.getValue());

        // Then
        verify(manager).revokeToken(token.getValue());
        assertThat(result).isEqualTo(token);
    }

}
