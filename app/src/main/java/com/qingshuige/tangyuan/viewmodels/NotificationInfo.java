package com.qingshuige.tangyuan.viewmodels;

import java.util.Date;

public class NotificationInfo {
    private int notificationId;
    private String sourceUserAvatarGuid;
    private String sourceUserNickname;
    private String sourceCommentContent;
    private int targetPostId;
    private int targetCommentId;
    private Date dateTime;

    public NotificationInfo(Date dateTime, int notificationId, String sourceCommentContent, String sourceUserAvatarGuid, String sourceUserNickname, int targetCommentId, int targetPostId) {
        this.dateTime = dateTime;
        this.notificationId = notificationId;
        this.sourceCommentContent = sourceCommentContent;
        this.sourceUserAvatarGuid = sourceUserAvatarGuid;
        this.sourceUserNickname = sourceUserNickname;
        this.targetCommentId = targetCommentId;
        this.targetPostId = targetPostId;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public String getSourceCommentContent() {
        return sourceCommentContent;
    }

    public String getSourceUserAvatarGuid() {
        return sourceUserAvatarGuid;
    }

    public String getSourceUserNickname() {
        return sourceUserNickname;
    }

    public int getTargetCommentId() {
        return targetCommentId;
    }

    public int getTargetPostId() {
        return targetPostId;
    }
}
