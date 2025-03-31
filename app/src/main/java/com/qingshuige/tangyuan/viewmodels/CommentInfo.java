package com.qingshuige.tangyuan.viewmodels;

import java.util.Date;

public class CommentInfo {
    private String userAvatarGuid;
    private String userNickname;
    private String commentText;
    private Date commentDateTime;
    private int commentId;
    private boolean hasReplies;
    private int userId;

    public CommentInfo(String userAvatarGuid, String userNickname, String commentText, Date commentDateTime, int commentId, boolean hasReplies, int userId) {

        this.userAvatarGuid = userAvatarGuid;
        this.userNickname = userNickname;
        this.commentText = commentText;
        this.commentDateTime = commentDateTime;
        this.commentId = commentId;
        this.hasReplies = hasReplies;
        this.userId = userId;
    }

    public String getUserAvatarGuid() {
        return userAvatarGuid;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public String getCommentText() {
        return commentText;
    }

    public Date getCommentDateTime() {
        return commentDateTime;
    }

    public int getCommentId() {
        return commentId;
    }

    public boolean isHasReplies() {
        return hasReplies;
    }

    public int getUserId() {
        return userId;
    }
}
