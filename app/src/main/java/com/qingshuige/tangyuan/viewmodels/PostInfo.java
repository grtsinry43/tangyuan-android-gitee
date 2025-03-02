package com.qingshuige.tangyuan.viewmodels;

import java.util.Date;

/**
 * 一篇帖子的所有信息。
 */
public class PostInfo {
    /**
     * 帖子唯一ID
     */
    private int postId;

    /**
     * 发帖用户昵称
     */
    private String userNickname;

    /**
     * 发帖时间
     */
    private Date postDate;

    /**
     * 正文内容
     */
    private String textContent;


    public int getPostId() {
        return postId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public Date getPostDate() {
        return postDate;
    }

    public PostInfo(int postId, String userNickname, Date postDate, String textContent){
        this.postId = postId;
        this.userNickname = userNickname;
        this.postDate = postDate;
        this.textContent = textContent;
    }

    public String getTextContent() {
        return textContent;
    }
}
