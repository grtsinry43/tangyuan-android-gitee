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

    private int userId;

    /**
     * 发帖用户昵称
     */
    private String userNickname;

    private String userAvatarGUID;

    /**
     * 发帖时间
     */
    private Date postDate;

    /**
     * 正文内容
     */
    private String textContent;

    private String image1GUID;

    private String image2GUID;

    private String image3GUID;

    private int sectionId;

    private int categoryId;

    private String categoryName;


    public int getPostId() {
        return postId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public Date getPostDate() {
        return postDate;
    }

    public PostInfo(int postId, int userId, String userNickname, String userAvatarGUID, Date postDate, String textContent, String image1GUID, String image2GUID, String image3GUID, int sectionId, int categoryId, String categoryName) {
        this.postId = postId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.userAvatarGUID = userAvatarGUID;
        this.postDate = postDate;
        this.textContent = textContent;
        this.image1GUID = image1GUID;
        this.image2GUID = image2GUID;
        this.image3GUID = image3GUID;
        this.sectionId = sectionId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getImage1GUID() {
        return image1GUID;
    }

    public String getImage2GUID() {
        return image2GUID;
    }

    public String getImage3GUID() {
        return image3GUID;
    }

    public String getUserAvatarGUID() {
        return userAvatarGUID;
    }

    public int getUserId() {
        return userId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
