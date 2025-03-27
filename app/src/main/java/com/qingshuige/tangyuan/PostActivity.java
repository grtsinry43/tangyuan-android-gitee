package com.qingshuige.tangyuan;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.Comment;
import com.qingshuige.tangyuan.network.CreateCommentDto;
import com.qingshuige.tangyuan.viewmodels.CommentInfo;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    private PostInfo postInfo;
    private int postId;
    private RecyclerView gallery;
    private RecyclerView commentsRcv;
    private ProgressBar pgBar;
    private ProgressBar pgBarCommentSend;
    private EditText editComment;
    private Button buttonSendComment;
    private TextView textCommentCounter;

    CommentCardAdapter commentAdapter;
    TokenManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postId = getIntent().getIntExtra("postId", 1);

        gallery = findViewById(R.id.imageGallery);
        commentsRcv = findViewById(R.id.commentRecyclerView);
        pgBar = findViewById(R.id.progressBar);
        editComment = findViewById(R.id.editComment);
        buttonSendComment = findViewById(R.id.buttonSendComment);
        textCommentCounter = findViewById(R.id.textCommentCounter);
        pgBarCommentSend = findViewById(R.id.progressBarCommentSend);

        tm = TangyuanApplication.getTokenManager();

        gallery.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Resources.getSystem().getDisplayMetrics().widthPixels * 3 / 4));
        //以4:3比例设置gallery
        gallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(gallery);

        initializeUI(postId);
    }

    private void initializeUI(int postId) {
        //正文区
        ApiHelper.getPostInfoByIdAsync(postId, result -> {
            postInfo = result;
            runOnUiThread(() -> {
                //UI
                pgBar.setVisibility(View.GONE);
                Picasso.get()
                        .load(ApiHelper.getFullImageURL(postInfo.getUserAvatarGUID()))
                        .into(((ImageView) findViewById(R.id.avatarView)));
                ((TextView) findViewById(R.id.nicknameView)).setText(postInfo.getUserNickname());
                ((TextView) findViewById(R.id.contentView)).setText(postInfo.getTextContent());
                ((TextView) findViewById(R.id.dateTimeView)).setText(postInfo.getPostDate().toLocaleString());
                ((TextView) findViewById(R.id.tidView)).setText("TID:" + postInfo.getPostId());
                ///Gallery
                if (postInfo.getImage1GUID() != null) {
                    //gallery可见化
                    gallery.setVisibility(View.VISIBLE);
                    //沉浸状态栏
                    findViewById(R.id.main).requestLayout();
                    ArrayList<String> images = new ArrayList<>();
                    images.add(postInfo.getImage1GUID());
                    if (postInfo.getImage2GUID() != null) {
                        images.add(postInfo.getImage2GUID());
                        if (postInfo.getImage3GUID() != null) {
                            images.add(postInfo.getImage3GUID());
                        }
                    }
                    gallery.setAdapter(new GalleryAdapter(images));
                }
                ///Avatar
                findViewById(R.id.userBar).setOnClickListener(view -> {
                    Intent intent = new Intent(PostActivity.this, UserActivity.class);
                    intent.putExtra("userId", postInfo.getUserId());
                    startActivity(intent);
                });
            });
        });

        //评论区
        commentAdapter = new CommentCardAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentsRcv.setLayoutManager(layoutManager);
        commentsRcv.setAdapter(commentAdapter);
        commentsRcv.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        updateComment();

        //输入评论区
        buttonSendComment.setOnClickListener(view -> {
            trySendComment();
        });
    }

    private void trySendComment() {
        if (tm.getToken() != null) {
            CreateCommentDto dto = new CreateCommentDto();

            if (TextUtils.isEmpty(editComment.getText())) {
                Toast.makeText(this, R.string.text_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            buttonSendComment.setVisibility(View.GONE);
            pgBarCommentSend.setVisibility(View.VISIBLE);

            dto.userId = DataTools.decodeJwtTokenUserId(tm.getToken());
            dto.imageGuid = null;//发图功能后期可以加
            dto.commentDateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
            dto.postId = postId;
            dto.parentCommentId = 0;
            dto.content = editComment.getText().toString();
            TangyuanApplication.getApi().postComment(dto).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.code() == 200) {
                        runOnUiThread(() -> {
                            Toast.makeText(PostActivity.this, R.string.comment_sent, Toast.LENGTH_SHORT).show();
                            buttonSendComment.setVisibility(View.VISIBLE);
                            pgBarCommentSend.setVisibility(View.GONE);
                            editComment.setText("");
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(editComment.getWindowToken(), 0);
                            updateComment();
                        });
                    } else {
                        Toast.makeText(PostActivity.this, R.string.send_comment_error, Toast.LENGTH_SHORT).show();
                        buttonSendComment.setVisibility(View.VISIBLE);
                        pgBarCommentSend.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable throwable) {

                }
            });
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void updateComment() {
        commentAdapter.clearData();
        textCommentCounter.setText(R.string.loading_comments);
        TangyuanApplication.getApi().getCommentForPost(postId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.code() != 404) {
                    //有评论
                    for (Comment m : response.body()) {
                        ApiHelper.getCommentInfoByIdAsync(m.commentId, info -> {
                            runOnUiThread(() -> {
                                if (m.parentCommentId == 0) { //如果是一级评论则显示
                                    commentAdapter.appendData(info);
                                }
                            });
                        });
                    }
                    runOnUiThread(() -> {
                        textCommentCounter.setText(response.body().size() + getString(R.string.of_comments));
                    });
                } else {
                    runOnUiThread(() -> {
                        textCommentCounter.setText(R.string.no_comment);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable throwable) {

            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}