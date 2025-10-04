package com.qingshuige.tangyuan;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qingshuige.tangyuan.data.CircleTransform;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.Comment;
import com.qingshuige.tangyuan.network.CreateCommentDto;
import com.qingshuige.tangyuan.ui.PhotoDialogFragment;
import com.qingshuige.tangyuan.viewmodels.CommentCardAdapter;
import com.qingshuige.tangyuan.viewmodels.CommentInfo;
import com.qingshuige.tangyuan.viewmodels.GalleryAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    CommentCardAdapter commentAdapter;
    TokenManager tm;
    private PostInfo postInfo;
    private int postId;
    private RecyclerView gallery;
    private RecyclerView commentsRcv;
    private ProgressBar pgBar;
    private ProgressBar pgBarCommentSend;
    private EditText editComment;
    private Button buttonSendComment;
    private TextView textCommentCounter;
    private BottomSheetDialog replyDialog;
    private Menu menu;
    private int flagScrollToCommmentId;

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

        if (getIntent().getIntExtra("commentId", 0) != 0) {
            targetToComment(getIntent().getIntExtra("commentId", 0));
        }
    }

    private void targetToComment(int commentId) {
        TangyuanApplication.getApi().getComment(commentId).enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if (response.code() == 200) {
                    Comment comment = response.body();
                    assert comment != null;
                    if (comment.parentCommentId != 0) { //是子评论
                        ApiHelper.getCommentInfoByIdAsync(comment.parentCommentId, result -> runOnUiThread(() -> showReplyBottomSheet(result)));
                    } else { //不是子评论
                        flagScrollToCommmentId = commentId;
                    }
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable throwable) {

            }
        });
    }

    private void initializeUI(int postId) {
        //数据无关设置
        commentAdapter = new CommentCardAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentAdapter.setOnReplyButtonClickListener(this::showReplyBottomSheet);
        commentAdapter.setOnTextClickListener(this::showReplyBottomSheet);
        commentAdapter.setOnAvatarClickListener(this::startUserActivity);
        commentAdapter.setOnItemHoldListener(info -> showCommentControlDialog(info, null));
        commentsRcv.setLayoutManager(layoutManager);
        commentsRcv.setAdapter(commentAdapter);
        commentsRcv.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        ApiHelper.getPostInfoByIdAsync(postId, result -> {
            if (result == null) {
                runOnUiThread(() -> {
                    pgBar.setVisibility(View.GONE);
                    androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(PostActivity.this)
                            .setTitle(R.string.load_post_error)
                            .setMessage(R.string.post_may_be_deleted_or_network_error)
                            .setPositiveButton(R.string.ok, (dialogInterface, i) -> finish())
                            .create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                });
            } else {
                postInfo = result;
                runOnUiThread(() -> {
                    updateContent();
                    updateComment();

                    buttonSendComment.setOnClickListener(view ->
                            trySendComment(editComment, buttonSendComment, pgBarCommentSend, null));
                });
            }
        });
    }

    private void showCommentControlDialog(CommentInfo commentInfo, CommentInfo parentCommentInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.comment);
        String[] items = {getString(R.string.copy), getString(R.string.delete)};
        //是当前用户
        if (tm.getToken() != null && DataTools.decodeJwtTokenUserId(tm.getToken()) == commentInfo.getUserId()) {
            items = new String[]{getString(R.string.copy), getString(R.string.delete)};
        }
        //非当前用户或未登录
        else {
            items = new String[]{getString(R.string.copy)};
        }
        builder.setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                            .setPrimaryClip(ClipData.newPlainText("tyapp", commentInfo.getCommentText()));
                    break;
                case 1:
                    TangyuanApplication.getApi().deleteComment(commentInfo.getCommentId()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                runOnUiThread(() -> {
                                    Toast.makeText(PostActivity.this, R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                                    updateComment();
                                    if (parentCommentInfo != null) {
                                        showReplyBottomSheet(parentCommentInfo);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable throwable) {

                        }
                    });
                    break;
            }
        });
        builder.create().show();
    }

    private void startUserActivity(CommentInfo commentInfo) {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("userId", commentInfo.getUserId());
        startActivity(intent);
    }

    private void updateContent() {
        //UI
        pgBar.setVisibility(View.GONE);
        Picasso.get()
                .load(ApiHelper.getFullImageURL(postInfo.getUserAvatarGUID()))
                .transform(new CircleTransform())
                .into(((ImageView) findViewById(R.id.avatarView)));
        ((TextView) findViewById(R.id.nicknameView)).setText(postInfo.getUserNickname());
        //Section
        String sectionName = new String();
        switch (postInfo.getSectionId()) {
            case 0:
                sectionName = getString(R.string.notice);
                break;
            case 1:
                sectionName = getString(R.string.menu_normalchat);
                break;
            case 2:
                sectionName = getString(R.string.menu_chitchat);
                break;
        }
        ((TextView) findViewById(R.id.textSectionName)).setText(sectionName);
        //Category
        ((TextView) findViewById(R.id.textCategoryName)).setText(postInfo.getCategoryName());
        findViewById(R.id.imgCategoryIcon).setOnClickListener(view -> startCategoryActivity());
        findViewById(R.id.textCategoryName).setOnClickListener(view -> startCategoryActivity());
        //正文区
        ((TextView) findViewById(R.id.contentView)).setText(postInfo.getTextContent());
        ((TextView) findViewById(R.id.dateTimeView)).setText(DataTools.getLocalFriendlyDateTime(postInfo.getPostDate(), this));
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
            GalleryAdapter adapter = new GalleryAdapter(images);
            adapter.setOnItemClickListener(this::showImageWindow);
            gallery.setAdapter(adapter);
        }
        ///Avatar
        findViewById(R.id.avatarView).setOnClickListener(view -> startUserActivity());
        findViewById(R.id.nicknameView).setOnClickListener(view -> startUserActivity());
        //菜单栏
        if ((!(tm.getToken() == null)) && DataTools.decodeJwtTokenUserId(tm.getToken()) == postInfo.getUserId()) {
            menu.findItem(R.id.menuDelete).setVisible(true);
        }
    }

    private void showImageWindow(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        PhotoDialogFragment photoDialogFragment = PhotoDialogFragment.create(bitmap);
        photoDialogFragment.show(getSupportFragmentManager(), null);
    }

    private void trySendComment(EditText input, Button sendButton, ProgressBar pgBar, CommentInfo parentCommentInfo) {
        if (tm.getToken() != null) {
            CreateCommentDto dto = new CreateCommentDto();

            if (TextUtils.isEmpty(input.getText())) {
                Toast.makeText(this, R.string.text_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            sendButton.setVisibility(View.GONE);
            pgBar.setVisibility(View.VISIBLE);

            dto.userId = DataTools.decodeJwtTokenUserId(tm.getToken());
            dto.imageGuid = null;//发图功能后期可以加
            dto.commentDateTime = new Date();
            dto.postId = postId;
            dto.parentCommentId = parentCommentInfo == null ? 0 : parentCommentInfo.getCommentId();
            dto.content = DataTools.deleteBlankLines(input.getText().toString());
            TangyuanApplication.getApi().postComment(dto).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.code() == 200) {
                        runOnUiThread(() -> {
                            Toast.makeText(PostActivity.this,
                                    parentCommentInfo == null ? R.string.comment_sent : R.string.reply_sent,
                                    Toast.LENGTH_SHORT).show();
                            sendButton.setVisibility(View.VISIBLE);
                            pgBar.setVisibility(View.GONE);
                            input.setText("");
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            updateComment();
                            if (parentCommentInfo != null) {
                                showReplyBottomSheet(parentCommentInfo);
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PostActivity.this, R.string.send_comment_error, Toast.LENGTH_SHORT).show();
                            sendButton.setVisibility(View.VISIBLE);
                            pgBar.setVisibility(View.GONE);
                        });
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable throwable) {
                    runOnUiThread(() -> {
                        Toast.makeText(PostActivity.this, R.string.send_comment_error, Toast.LENGTH_SHORT).show();
                        sendButton.setVisibility(View.VISIBLE);
                        pgBar.setVisibility(View.GONE);
                    });
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
                if (response.code() == 200 && response.body() != null) {
                    //有评论
                    AtomicInteger parentCount = new AtomicInteger();
                    AtomicInteger scrollToPosition = new AtomicInteger(); //用来记录需要滚动到的位置
                    for (Comment m : response.body()) {
                        ApiHelper.getCommentInfoByIdAsync(m.commentId, info -> {
                            if (m.parentCommentId == 0) { //如果是一级评论则显示
                                commentAdapter.appendData(info);
                                parentCount.getAndIncrement();

                                if (m.commentId == flagScrollToCommmentId) { //如果这条评论正是想要滚动到的评论
                                    scrollToPosition.set(commentAdapter.getPositionOf(info)); //记录这条评论在Adapter中的位置
                                }

                                runOnUiThread(() -> {
                                    textCommentCounter.setText(parentCount.get() + getString(R.string.of_comments));

                                    if (scrollToPosition.get() != 0) { //如果已经设置了滚动位置
                                        findViewById(R.id.main).postDelayed(() -> {
                                            //滚动到指定位置
                                            int distance = commentsRcv.getLayoutManager().findViewByPosition(scrollToPosition.get()).getTop() +
                                                    findViewById(R.id.contentView).getHeight();
                                            ((NestedScrollView) findViewById(R.id.main)).smoothScrollTo(0, distance);
                                            //每次新增一条父评论，都滚动到指定位置
                                        }, 2000);

                                    }
                                });


                            }
                        });
                    }
                } else {
                    runOnUiThread(() -> textCommentCounter.setText(R.string.no_comment));
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable throwable) {
                runOnUiThread(() -> textCommentCounter.setText(R.string.load_comments_error));
            }
        });
    }

    private void showReplyBottomSheet(CommentInfo info) {
        if (replyDialog != null) {
            replyDialog.dismiss();
        }
        replyDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_comment_layout, null);
        replyDialog.setContentView(dialogView);

        replyDialog.setCancelable(true);
        replyDialog.setCanceledOnTouchOutside(true);

        ProgressBar pgBar = dialogView.findViewById(R.id.pgBar);

        RecyclerView replyRcv = dialogView.findViewById(R.id.replyRecyclerView);
        CommentCardAdapter adapter = new CommentCardAdapter();
        adapter.setOnAvatarClickListener(this::startUserActivity);
        adapter.setOnItemHoldListener(subCommentInfo -> showCommentControlDialog(subCommentInfo, info));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        replyRcv.setAdapter(adapter);
        replyRcv.setLayoutManager(layoutManager);
        replyRcv.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        TangyuanApplication.getApi().getSubComment(info.getCommentId()).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.code() != 404) {
                    runOnUiThread(() ->
                            ((TextView) dialogView.findViewById(R.id.titleReply)).setText(response.body().size() + getString(R.string.of_replies)));
                    for (Comment c : response.body()) {
                        ApiHelper.getCommentInfoByIdAsync(c.commentId, adapter::appendData);
                    }

                } else {
                    runOnUiThread(() -> ((TextView) dialogView.findViewById(R.id.titleReply)).setText(R.string.no_reply));
                }
                pgBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable throwable) {
                runOnUiThread(() -> ((TextView) dialogView.findViewById(R.id.titleReply)).setText(R.string.network_error));
            }
        });

        dialogView.findViewById(R.id.buttonSendReply).setOnClickListener(view ->
                trySendComment(dialogView.findViewById(R.id.editReply),
                        dialogView.findViewById(R.id.buttonSendReply),
                        dialogView.findViewById(R.id.pgBar),
                        info));

        ((EditText) dialogView.findViewById(R.id.editReply)).setHint(getString(R.string.reply_to) + info.getCommentText());
        replyDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuDelete) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_post)
                    .setMessage(R.string.confirm_delete_post)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) ->
                            TangyuanApplication.getApi().deletePost(postId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        runOnUiThread(() -> {
                                            Intent intent = new Intent();
                                            intent.putExtra("deletedPostId", postId);
                                            setResult(RESULT_OK, intent);
                                            finish();

                                            Toast.makeText(PostActivity.this, R.string.post_deleted, Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                                    Toast.makeText(PostActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                                }
                            }))
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post_menu, menu);
        this.menu = menu;
        return true;
    }

    private void startUserActivity() {
        Intent intent = new Intent(PostActivity.this, UserActivity.class);
        intent.putExtra("userId", postInfo.getUserId());
        startActivity(intent);
    }

    private void startCategoryActivity() {
        Intent intent = new Intent(PostActivity.this, CategoryActivity.class);
        intent.putExtra("categoryId", postInfo.getCategoryId());
        startActivity(intent);
    }
}