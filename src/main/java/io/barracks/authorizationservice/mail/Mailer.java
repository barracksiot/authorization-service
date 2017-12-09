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

package io.barracks.authorizationservice.mail;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.barracks.authorizationservice.mail.util.MimeMessageBuilder;
import io.barracks.authorizationservice.mail.util.UrlHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import java.io.IOException;

public abstract class Mailer<T> {

    public static final String TEMPLATES_EMAILS_BASE_FOLDER = "/templates/emails/";

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${io.barracks.authorizationservice.reset_password.mail.from}")
    private String from;

    @Value("${io.barracks.authorizationservice.reset_password.mail.reply_to}")
    private String replyTo;

    @Value("${io.barracks.authorizationservice.base_url}")
    private String baseUrl;

    private Handlebars handlebars;

    public Mailer() {
        this.handlebars = new Handlebars();
        handlebars.registerHelper(UrlHelper.NAME, new UrlHelper());
    }

    public void send(T object) {
        try {
            final HandleBarTemplateContext<T> context = new HandleBarTemplateContext<>(object, baseUrl);
            final MimeMessageBuilder messageBuilder = getMessageBuilder()
                    .text(getTextContent(context))
                    .html(getHtmlContent(context))
                    .subject(getSubject(context))
                    .to(getRecipients(context));
            this.send(messageBuilder);
        } catch (IOException e) {
            throw new EmailTemplateException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String[] getRecipients(HandleBarTemplateContext<T> object);

    protected abstract String getSubject(HandleBarTemplateContext<T> object);

    private MimeMessageBuilder getMessageBuilder() throws MessagingException {
        return new MimeMessageBuilder(javaMailSender.createMimeMessage())
                .from(from)
                .replyTo(replyTo);
    }

    private void send(MimeMessageBuilder messageBuilder) {
        try {
            javaMailSender.send(messageBuilder.build());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHtmlContent(HandleBarTemplateContext<T> object) throws IOException {
        final String className = this.getClass().getSimpleName();
        final Template template = handlebars.compile(TEMPLATES_EMAILS_BASE_FOLDER + className + "/html");
        return template.apply(object);
    }

    private String getTextContent(HandleBarTemplateContext<T> object) throws IOException {
        final String className = this.getClass().getSimpleName();
        final Template template = handlebars.compile(TEMPLATES_EMAILS_BASE_FOLDER + className + "/text");
        return template.apply(object);
    }

}
