package com.qingshuige.tangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NewPostActivity extends AppCompatActivity {

    private EditText textEdit;
    private TextView byteCounter;

    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;

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
        getSupportActionBar().setTitle(R.string.new_post);

        textEdit = (EditText) findViewById(R.id.textEdit);
        byteCounter = (TextView) findViewById(R.id.byteCounter);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        imageView3 = (ImageView) findViewById(R.id.imageView3);

        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                byteCounter.setText(textEdit.getText().length() + " / 200");
                if (textEdit.getText().length() > 200) {
                    byteCounter.setTextColor(getColor(R.color.amour));
                } else {
                    byteCounter.setTextColor(getColor(R.color.imperial_primer));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        imageView1.setOnClickListener(new ImageViewOnClickListener());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_new_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
        return super.onOptionsItemSelected(item);
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
                    } else if (imageView2.getDrawable() == null){
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
}