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

package io.barracks.authorizationservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.barracks.authorizationservice.exception.UnknownUserStatusException;
import io.barracks.authorizationservice.model.utils.UserStatusSerializer;

@JsonSerialize(using = UserStatusSerializer.class)
public enum UserStatus {

    EMAIL_VERIFICATION_PENDING("email_verification_pending"),
    SUBSCRIPTION_PENDING("subscription_pending"),
    PAYMENT_PENDING("payment_pending"),
    ACTIVE("active");

    private String name;

    UserStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static UserStatus fromName(String statusName) {
        UserStatus statusFound = null;
        for (UserStatus status : UserStatus.values()) {
            if (statusName.equals(status.name)) {
                statusFound = status;
                break;
            }
        }

        if (statusFound == null) {
            throw new UnknownUserStatusException(statusName);
        }

        return statusFound;
    }
}