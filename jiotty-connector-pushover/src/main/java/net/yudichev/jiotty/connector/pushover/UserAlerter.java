package net.yudichev.jiotty.connector.pushover;

import net.pushover.client.MessagePriority;

public interface UserAlerter {
    void sendAlert(User user, MessagePriority priority, String text);

    interface User {
        String token();
    }
}
