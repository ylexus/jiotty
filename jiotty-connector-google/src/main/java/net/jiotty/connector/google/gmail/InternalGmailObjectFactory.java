package net.jiotty.connector.google.gmail;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

interface InternalGmailObjectFactory {
    InternalGmailMessage createMessage(Message message);

    InternalGmailMessageAttachment createAttachment(Message message, MessagePart messagePart);
}
