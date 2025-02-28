package com.qingshuige.tangyuan.models;

import java.util.Date;

/**
 * 帖子元数据（不含正文）
 */
public class PostMetadata {
    /**
     * 帖子唯一ID
     */
    private int postId;

    /**
     * 发帖用户唯一ID
     */
    private int userId;

    /**
     * 发帖时间ID
     */
    private Date postDate;

    /**
     * 这篇帖子所处的板块的ID
     */
    private int sectionId;

    public int getPostId() {
        return postId;
    }

    public int getUserId() {
        return userId;
    }

    public Date getPostDate() {
        return postDate;
    }

    public int getSectionId() {
        return sectionId;
    }

    private PostMetadata(){}

    public PostMetadata(String json){

    }
}
