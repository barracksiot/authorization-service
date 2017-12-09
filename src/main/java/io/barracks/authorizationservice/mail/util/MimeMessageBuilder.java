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

package io.barracks.authorizationservice.mail.util;

import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MimeMessageBuilder {

    private MimeMessageHelper helper;
    private String[] to;
    private String replyTo;
    private String from;
    private String subject;
    private String text;
    private String html;

    public MimeMessageBuilder(MimeMessage mimeMessage) throws MessagingException {
        this.helper = new MimeMessageHelper(mimeMessage, true);
    }

    public MimeMessageBuilder to(String... to) {
        this.to = to;
        return this;
    }

    public MimeMessageBuilder replyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public MimeMessageBuilder from(String from) {
        this.from = from;
        return this;
    }

    public MimeMessageBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    public MimeMessageBuilder text(String text) {
        this.text = text;
        return this;
    }

    public MimeMessageBuilder html(String html) {
        this.html = html;
        return this;
    }

    public MimeMessage build() throws MessagingException {
        helper.setTo(to);
        helper.setReplyTo(replyTo);
        helper.setFrom(from);
        helper.setSubject(subject);
        helper.setText(text, html);
        return helper.getMimeMessage();
    }

}
