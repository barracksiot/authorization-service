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

import org.apache.catalina.util.URLEncoder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.Locale;

public class PageableHelper {
    public static String toUriQuery(Pageable pageable) {
        StringBuilder builder = new StringBuilder();
        builder.append("page=").append(pageable.getPageNumber()).append("&size=").append(pageable.getPageSize());
        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                builder.append("&sort=").append(URLEncoder.DEFAULT.encode(order.getProperty())).append(",").append(order.getDirection().name().toLowerCase(Locale.US));
            }
        }
        return builder.toString();
    }

    public static MultiValueMap<String, String> queryFrom(Pageable pageable) {
        LinkedMultiValueMap result = new LinkedMultiValueMap();
        result.add("page", String.valueOf(pageable.getPageNumber()));
        result.add("size", String.valueOf(pageable.getPageSize()));
        if (pageable.getSort() != null) {
            Iterator var3 = pageable.getSort().iterator();

            while (var3.hasNext()) {
                Sort.Order order = (Sort.Order) var3.next();
                result.add("sort", order.getProperty() + "," + order.getDirection().name().toLowerCase(Locale.US));
            }
        }

        return result;
    }
}
