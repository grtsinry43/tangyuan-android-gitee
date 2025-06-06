package com.qingshuige.tangyuan.viewmodels;

import com.qingshuige.tangyuan.network.NewNotification;

import java.util.Date;

public class NotificationInfo {
    private NewNotification notification;
    private String title;
    private String type;
    private String message;
    private String avatarGuid;
    private int relatedPostId;
    private int relatedUserId;
    private int typeColor;

    public NotificationInfo(NewNotification notification, String title, String type, String message, String avatarGuid, int relatedPostId, int relatedUserId, int typeColor) {
        this.notification = notification;
        this.title = title;
        this.type = type;
        this.message = message;
        this.avatarGuid = avatarGuid;
        this.relatedPostId = relatedPostId;
        this.relatedUserId = relatedUserId;
        this.typeColor = typeColor;
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

    public String getType() {
        return type;
    }


    public int getRelatedPostId() {
        return relatedPostId;
    }

    public int getRelatedUserId() {
        return relatedUserId;
    }

    public int getTypeColor() {
        return typeColor;
    }
}
