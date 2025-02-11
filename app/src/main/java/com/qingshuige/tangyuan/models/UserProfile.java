package com.qingshuige.tangyuan.models;


/**
 * 用户资料
 */
public class UserProfile {
    /**
     * 用户唯一ID
     */
    private int userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户个性签名
     */
    private String bio;

    public int getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getBio() {
        return bio;
    }
}
