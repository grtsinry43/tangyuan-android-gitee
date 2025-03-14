package com.qingshuige.tangyuan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.qingshuige.tangyuan.network.LoginDto;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.EncryptedPrivateKeyInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private EditText editTextPhone;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonLogin = findViewById(R.id.buttonLogin);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextTextPassword);

        buttonLogin.setOnClickListener(view -> {
            if (editTextPhone.getText().length() != 0 && editTextPassword.getText().length() != 0) {
                TangyuanApplication.getSharedPreferences()
                        .edit()
                        .putString("phoneNumber", editTextPhone.getText().toString())
                        .putString("password", editTextPassword.getText().toString())
                        .apply();
                login();
            }
        });
    }

    private void login() {
        LoginDto dto = new LoginDto();
        dto.phoneNumber = TangyuanApplication.getSharedPreferences().getString("phoneNumber", null);
        dto.password = TangyuanApplication.getSharedPreferences().getString("password", null);
        TangyuanApplication.getApi().login(dto).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 400) {
                    //Permission denied
                    Toast.makeText(LoginActivity.this, getString(R.string.phone_number_password_not_match), Toast.LENGTH_SHORT).show();
                } else {
                    //TODO:修改后端接口返回JSON
                    try {
                        TangyuanApplication.getSharedPreferences().edit()
                                .putString("JwtToken", response.body().string())
                                .apply();
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("JWT Token")
                                .setMessage(response.body().string())
                                .show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {

            }
        });
    }
}