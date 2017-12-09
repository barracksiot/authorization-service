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

package io.barracks.authorizationservice.utils;

import javax.validation.ConstraintViolation;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ViolationPathMatcher<T> implements Consumer<ConstraintViolation<T>> {
    private final String path;

    private ViolationPathMatcher(String path) {
        this.path = path;
    }

    public static <U> ViolationPathMatcher<U> violatesConstraintForPath(String path) {
        return new ViolationPathMatcher<>(path);
    }

    @Override
    public void accept(ConstraintViolation<T> constraintViolation) {
        assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo(path);
    }
}