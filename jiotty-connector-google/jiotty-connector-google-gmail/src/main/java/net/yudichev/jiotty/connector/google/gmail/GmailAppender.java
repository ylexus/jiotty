package net.yudichev.jiotty.connector.google.gmail;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import net.yudichev.jiotty.connector.google.common.GoogleApiSettings;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.yudichev.jiotty.connector.google.gmail.Constants.ME;

// TODO ensure it queues emails for a significant amount of time if unable to send, think internet connection interrupted
@Plugin(name = "GmailAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public final class GmailAppender extends AbstractAppender {
    private final String emailAddress;
    private final GoogleApiSettings googleApiSettings;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    private GmailAppender(String name,
                          Filter filter,
                          Layout<? extends Serializable> layout,
                          boolean ignoreExceptions,
                          String emailAddress,
                          String applicationName,
                          String credentialsResourcePath,
                          Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.emailAddress = checkNotNull(emailAddress, "emailAddress attribute is required");
        checkNotNull(credentialsResourcePath);
        URL credentialsUrl = GmailAppender.class.getClassLoader().getResource(credentialsResourcePath);
        checkArgument(credentialsUrl != null, "Credentials resource not found at %s", credentialsResourcePath);
        googleApiSettings = GoogleApiSettings.of(applicationName, credentialsUrl);
    }

    @SuppressWarnings({"BooleanParameter", "MethodWithTooManyParameters"})
    @PluginFactory
    public static GmailAppender createAppender(@PluginAttribute("name") String name,
                                               @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                               @PluginAttribute("emailAddress") String emailAddress,
                                               @PluginAttribute("applicationName") String applicationName,
                                               @PluginAttribute("credentialsResourcePath") String credentialsResourcePath,
                                               @PluginElement("Layout") Layout<? extends Serializable> layout,
                                               @PluginElement("Filters") Filter filter) {

        checkArgument(name != null, "No name provided for %s", GmailAppender.class.getSimpleName());

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new GmailAppender(name, filter, layout, ignoreExceptions, emailAddress, applicationName, credentialsResourcePath, null);
    }

    @Override
    public void append(LogEvent event) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);

            MimeMessage email = new MimeMessage(session);

            email.setFrom(new InternetAddress(emailAddress));
            email.addRecipient(RecipientType.TO, new InternetAddress(emailAddress));
            email.setSubject(googleApiSettings.applicationName() + " alert: " + event.getLevel());
            email.setText(toSerializable(event).toString());

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] bytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
            Message message = new Message();
            message.setRaw(encodedEmail);
            GmailProvider.getService(googleApiSettings).users().messages().send(ME, message).execute();
        } catch (MessagingException | IOException e) {
            //noinspection CallToPrintStackTrace OK for appender
            e.printStackTrace();
        }
    }
}
