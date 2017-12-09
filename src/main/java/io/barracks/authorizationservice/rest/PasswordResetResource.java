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
import io.barracks.authorizationservice.rest.entity.EmailEntity;
import io.barracks.authorizationservice.rest.entity.ResetPasswordConfirmationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/password")
public class PasswordResetResource {

    @Autowired
    private UserManager userManager;

    @RequestMapping(value = "/set", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String setPassword(@Valid @RequestBody EmailEntity entity) {
        userManager.initPassword(entity.getEmail());
        return "Email sent";
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public String resetPassword(@Valid @RequestBody EmailEntity entity) {
        userManager.resetPassword(entity.getEmail());
        return "Email sent";
    }

    @RequestMapping(value = "/reset/confirm", method = RequestMethod.POST)
    public String confirmResetPassword(@Valid @RequestBody ResetPasswordConfirmationEntity entity) {
        userManager.confirmResetPassword(entity.getToken(), entity.getPassword());
        return "Email sent";
    }
}
