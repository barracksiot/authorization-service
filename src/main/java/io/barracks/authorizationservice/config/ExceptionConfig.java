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

package io.barracks.authorizationservice.config;

import cz.jirutka.spring.exhandler.RestHandlerExceptionResolverBuilder;
import io.barracks.authorizationservice.exception.*;
import io.barracks.commons.configuration.ExceptionHandlingConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ExceptionConfig extends ExceptionHandlingConfiguration {
    @Override
    public RestHandlerExceptionResolverBuilder restExceptionResolver() {
        return super.restExceptionResolver()
                .addErrorMessageHandler(UserNotFoundException.class, HttpStatus.UNAUTHORIZED)
                .addErrorMessageHandler(PasswordResetTokenNotFoundException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(UnknownTokenException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(UnknownUserStatusException.class, HttpStatus.BAD_REQUEST)
                .addErrorMessageHandler(BarracksAuthenticationException.class, HttpStatus.UNAUTHORIZED)
                .addErrorMessageHandler(UserAlreadyExistsException.class, HttpStatus.BAD_REQUEST)
                .addErrorMessageHandler(PasswordResetTokenExpiredException.class, HttpStatus.GONE)
                .addErrorMessageHandler(DataIntegrityException.class, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
