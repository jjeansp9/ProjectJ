package kr.jeet.edu.student.fcm;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(FCMManager.NOTIFICATION_ID_NONE);
    public static int getID() {
        return c.incrementAndGet();
    }
}
