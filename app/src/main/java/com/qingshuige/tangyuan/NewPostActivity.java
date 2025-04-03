package com.qingshuige.tangyuan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.data.MediaTools;
import com.qingshuige.tangyuan.network.CreatPostMetadataDto;
import com.qingshuige.tangyuan.network.PostBody;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class NewPostActivity extends AppCompatActivity {

    private EditText textEdit;
    private TextView byteCounter;
    private ProgressBar pgBar;

    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private Spinner spinnerSection;
    private Menu menu;

    private ExecutorService es;
    private Handler handler;

    private TokenManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_post);
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

        textEdit = (EditText) findViewById(R.id.textEdit);
        byteCounter = (TextView) findViewById(R.id.byteCounter);
        pgBar = findViewById(R.id.progressBar);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);
        spinnerSection = findViewById(R.id.spinnerSection);

        es = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
        tm = TangyuanApplication.getTokenManager();

        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                byteCounter.setText(textEdit.getText().length() + " / 200");
                if (textEdit.getText().length() > 200) {
                    byteCounter.setTextColor(getColor(R.color.nanohanacha_gold));
                } else {
                    byteCounter.setTextColor(getColor(R.color.electromagnetic));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        imageView1.setOnClickListener(new ImageViewOnClickListener());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{getString(R.string.menu_normalchat), getString(R.string.menu_chitchat)});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSection.setAdapter(spinnerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_post_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //选择图片
        if (item.getItemId() == R.id.image_button) {
            //假如imageView有空闲
            if (imageView1.getDrawable() == null ||
                    imageView2.getDrawable() == null ||
                    imageView3.getDrawable() == null) {
                //选择图片
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        }

        //发帖
        if (item.getItemId() == R.id.send_post_button) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                View view = getCurrentFocus();
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            sendPostAsync(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendPostAsync(Context context) {
        //检查必要条件
        if (!(textEdit.getText().length() > 0)) {
            Toast.makeText(context, R.string.text_is_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(textEdit.getText().length() < 200)) {
            Toast.makeText(context, R.string.text_is_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        menu.findItem(R.id.send_post_button).setEnabled(false);
        pgBar.setVisibility(View.VISIBLE);

        es.execute(() -> {
            try {
                //1.上传图片
                List<String> guids = new ArrayList<>();
                List<ImageView> imageViewList = new ArrayList<>();
                imageViewList.add(imageView1);
                imageViewList.add(imageView2);
                imageViewList.add(imageView3);//这个List只是为了foreach方便
                for (ImageView v : imageViewList) {
                    if (v.getDrawable() != null) {
                        Bitmap bitmap = MediaTools.compressToSize(NewPostActivity.this,
                                ((BitmapDrawable) v.getDrawable()).getBitmap(),
                                1.5f);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
                        MultipartBody.Part part =
                                MultipartBody.Part.createFormData("file", "image.jpg", requestBody);
                        String guid =
                                new ArrayList<>(TangyuanApplication.getApi().postImage(part).execute().body().values())
                                        .get(0);
                        guids.add(guid);
                    }
                }

                //2.上传元数据
                int postId;
                CreatPostMetadataDto metadataDto = new CreatPostMetadataDto();
                metadataDto.userId = DataTools.decodeJwtTokenUserId(tm.getToken());
                metadataDto.postDateTime = new Date();
                metadataDto.sectionId = spinnerSection.getSelectedItemPosition() + 1;
                metadataDto.isVisible = true;
                postId = new ArrayList<>(TangyuanApplication.getApi().postPostMetadata(metadataDto).execute().body().values())
                        .get(0);

                //3.上传Body
                PostBody body = new PostBody();
                body.postId = postId;
                body.textContent = textEdit.getText().toString();
                if (!guids.isEmpty()) {
                    body.image1UUID = guids.get(0);
                    if (guids.size() >= 2) {
                        body.image2UUID = guids.get(1);
                    }
                    if (guids.size() == 3) {
                        body.image3UUID = guids.get(2);
                    }
                }
                TangyuanApplication.getApi().postPostBody(body).execute();

                //4.完成
                finish();
                handler.post(() -> {
                    Toast.makeText(context, R.string.post_sent, Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, PostActivity.class).putExtra("postId", postId));
                });
            } catch (Exception e) {
                handler.post(() -> {
                    Snackbar.make(findViewById(R.id.imageLayout),
                            getString(R.string.send_post_error),
                            BaseTransientBottomBar.LENGTH_LONG).show();
                    ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("error", e.getLocalizedMessage()));
                    menu.findItem(R.id.send_post_button).setEnabled(true);
                    pgBar.setVisibility(View.GONE);
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1://选择图片
                if (resultCode == RESULT_OK && data != null) {
                    //轮流填充三个imageView
                    if (imageView1.getDrawable() == null) {
                        imageView1.setImageURI(data.getData());
                        break;
                    } else if (imageView2.getDrawable() == null) {
                        imageView2.setImageURI(data.getData());
                        break;
                    } else if (imageView3.getDrawable() == null) {
                        imageView3.setImageURI(data.getData());
                        break;
                    }
                }
                break;
        }
    }

    private class ImageViewOnClickListener implements View.OnClickListener {
        private ImageView imageView;

        @Override
        public void onClick(View view) {
            imageView = (ImageView) view;
            if (imageView.getDrawable() != null) {
                imageView.setImageDrawable(null);
            }
        }
    }


    /**
     * @param context
     * @param uri
     * @return 图片大小Byte数。
     */
    static long getImageSize(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            // 查询文件大小的列
            String[] projection = {OpenableColumns.SIZE};
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeColumnIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeColumnIndex >= 0) {
                    return cursor.getLong(sizeColumnIndex); // 返回文件大小（字节）
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1; // 获取失败返回-1
    }

}