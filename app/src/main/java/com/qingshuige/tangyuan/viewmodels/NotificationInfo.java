package com.qingshuige.tangyuan.viewmodels;

import com.qingshuige.tangyuan.network.NewNotification;

import java.util.Date;

public class NotificationInfo {
    private NewNotification notification;
    private String title;
    private String message;
    private String avatarGuid;

    public NotificationInfo(NewNotification notification, String title, String message, String avatarGuid) {
        this.notification = notification;
        this.title = title;
        this.message = message;
        this.avatarGuid = avatarGuid;
    }

    public String getMessage() {
        return message;
    }

    public NewNotification getNotification() {
        return notification;
    }

    public String getAvatarGuid() {
        return avatarGuid;
    }

    public String getTitle() {
        return title;
    }
}
