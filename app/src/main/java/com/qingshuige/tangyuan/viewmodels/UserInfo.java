package com.qingshuige.tangyuan.viewmodels;

import com.qingshuige.tangyuan.network.User;

public class UserInfo {
    private User user;
    private boolean isFollowed;

    public UserInfo(User user, boolean isFollowed) {
        this.user = user;
        this.isFollowed = isFollowed;
    }

    public User getUser() {
        return user;
    }

    public boolean isFollowed() {
        return isFollowed;
    }
}
