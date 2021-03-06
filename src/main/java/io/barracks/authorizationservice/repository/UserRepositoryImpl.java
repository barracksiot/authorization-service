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

package io.barracks.authorizationservice.repository;

import io.barracks.authorizationservice.model.User;
import io.barracks.authorizationservice.model.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public class UserRepositoryImpl implements UserRepositoryCustom {
    private final MongoOperations operations;

    @Autowired
    public UserRepositoryImpl(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public Optional<User> updateUserStatus(String userId, UserStatus status) {
        return Optional.ofNullable(updateUserField(userId, "status", status));
    }

    @Override
    public Optional<User> updateUserPassword(String userId, String password) {
        return Optional.ofNullable(updateUserField(userId, "encryptedPassword", password));
    }

    User updateUserField(String userId, String fieldName, Object field) {
        return operations.findAndModify(
                Query.query(where("_id").is(userId)),
                Update.update(fieldName, field),
                FindAndModifyOptions.options().returnNew(true),
                User.class
        );
    }
}
