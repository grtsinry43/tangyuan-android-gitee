package com.qingshuige.tangyuan.network;

import java.util.Date;

public class CreateCommentDto {
    public Date commentDateTime;
    public String content;
    public String imageGuid;
    public long parentCommentId;
    public long postId;
    public long userId;
}
