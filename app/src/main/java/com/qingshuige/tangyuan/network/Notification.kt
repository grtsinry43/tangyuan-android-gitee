package com.qingshuige.tangyuan.network;

import java.util.Date;

public class Notification {
    public int notificationId;
    public int targetUserId;
    public int targetPostId;
    public int targetCommentId;
    public int sourceCommentId;
    public int sourceUserId;
    public boolean isRead;
    public Date notificationDateTime;
}
