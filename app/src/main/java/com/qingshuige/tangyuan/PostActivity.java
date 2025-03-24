package com.qingshuige.tangyuan;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostActivity extends AppCompatActivity {

    private PostInfo postInfo;

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

        int postId = getIntent().getIntExtra("postId", 1);

        RecyclerView gallery = findViewById(R.id.imageGallery);
        gallery.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Resources.getSystem().getDisplayMetrics().widthPixels * 3 / 4));
        //以4:3比例设置gallery
        gallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(gallery);

        ApiHelper.getPostInfoByIdAsync(postId, result -> {
            postInfo = result;
            runOnUiThread(() -> {
                //UI
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
                    ((ScrollView) findViewById(R.id.main)).setFitsSystemWindows(false);
                    ((ScrollView) findViewById(R.id.main)).requestLayout();
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

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}