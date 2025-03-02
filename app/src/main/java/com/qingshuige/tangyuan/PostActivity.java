package com.qingshuige.tangyuan;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

public class PostActivity extends AppCompatActivity {

    private PostInfo postInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int postId = getIntent().getIntExtra("postId", 1);
        ApiHelper.getPostInfoByIdAsync(postId, new ApiHelper.ApiCallback<PostInfo>() {
            @Override
            public void onComplete(PostInfo result) {
                postInfo = result;
                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.nicknameView)).setText(postInfo.getUserNickname());
                    ((TextView) findViewById(R.id.contentView)).setText(postInfo.getTextContent());
                    ((TextView) findViewById(R.id.dateTimeView)).setText(postInfo.getPostDate().toLocaleString());
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}