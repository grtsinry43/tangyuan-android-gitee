package com.qingshuige.tangyuan;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.CreateUserDto;
import com.qingshuige.tangyuan.network.LoginDto;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private EditText editTextPhone;
    private EditText editTextPassword;
    private EditText editTextNickname;

    private TokenManager tm;
    private AuthStage stage = AuthStage.REQUIRING_PHONE;

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
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNickname = findViewById(R.id.editTextNickname);

        tm = TangyuanApplication.getTokenManager();

        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stage = AuthStage.REQUIRING_PHONE;
                updateUIAccordingToStage();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        buttonLogin.setOnClickListener(view -> {
            if (editTextPhone.getText().length() != 0) {
                buttonLogin.setEnabled(false);
                switch (stage) {
                    case REQUIRING_PHONE:
                        if (!DataTools.isValidPhoneNumber(editTextPhone.getText().toString(), "CN")) {
                            Toast.makeText(this, R.string.valid_phone_number, Toast.LENGTH_SHORT).show();
                            buttonLogin.setEnabled(true);
                            break;
                        }
                        ApiHelper.judgeIfUserExistsAsync(editTextPhone.getText().toString(), result -> {
                            if (result) {
                                //账号存在
                                stage = AuthStage.REQUIRING_PASSWORD;
                            } else {
                                //账号不存在
                                stage = AuthStage.REQUIRING_REGISTER;
                            }
                            runOnUiThread(() -> {
                                updateUIAccordingToStage();
                                buttonLogin.setEnabled(true);
                            });
                        });
                        break;
                    case REQUIRING_PASSWORD:
                        if (editTextPassword.getText().length() != 0) {
                            login(editTextPhone.getText().toString(),
                                    editTextPassword.getText().toString());
                        }
                        break;
                    case REQUIRING_REGISTER:
                        if (editTextPassword.getText().length() != 0 &&
                                editTextNickname.getText().length() != 0) {
                            signUpAndLogIn(
                                    editTextPhone.getText().toString(),
                                    editTextPassword.getText().toString(),
                                    editTextNickname.getText().toString()
                            );
                        }
                        break;
                }
            }
        });
    }

    private void login(String phoneNumber, String password) {
        tm.setPhoneNumberAndPassword(phoneNumber, password);
        LoginDto dto = new LoginDto();
        dto.phoneNumber = phoneNumber;
        dto.password = password;
        TangyuanApplication.getApi().login(dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.code() == 400) {
                    //Permission denied
                    Toast.makeText(LoginActivity.this, getString(R.string.phone_number_password_not_match), Toast.LENGTH_SHORT).show();
                } else {
                    tm.setToken(response.body().values().iterator().next());
                    Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_LONG).show();
                    LoginActivity.this.finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable throwable) {

            }
        });
    }

    private void signUpAndLogIn(String phoneNumber, String password, String nickname) {
        CreateUserDto dto = new CreateUserDto();
        dto.phoneNumber = phoneNumber;
        dto.password = password;
        dto.nickName = nickname;
        dto.avatarGuid = "8f416888-2ca4-4cda-8882-7f06a89630a2";//默认头像
        dto.isoRegionName = "CN";//TODO:增加国际用户注册支持
        TangyuanApplication.getApi().postUser(dto).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                tm.setPhoneNumberAndPassword(phoneNumber, password);
                LoginDto loginDto = new LoginDto();
                loginDto.phoneNumber = phoneNumber;
                loginDto.password = password;
                TangyuanApplication.getApi().login(loginDto).enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                        tm.setToken(response.body().values().iterator().next());
                        Toast.makeText(LoginActivity.this, getString(R.string.signup_login_success), Toast.LENGTH_LONG).show();
                        LoginActivity.this.finish();
                    }

                    @Override
                    public void onFailure(Call<Map<String, String>> call, Throwable throwable) {

                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {

            }
        });
    }

    void updateUIAccordingToStage() {
        switch (stage) {
            case REQUIRING_PHONE:
                editTextPassword.setVisibility(View.GONE);
                editTextNickname.setVisibility(View.GONE);
                break;
            case REQUIRING_PASSWORD:
                editTextPassword.setVisibility(View.VISIBLE);
                editTextNickname.setVisibility(View.GONE);
                break;
            case REQUIRING_REGISTER:
                editTextPassword.setVisibility(View.VISIBLE);
                editTextNickname.setVisibility(View.VISIBLE);
                break;
        }

        editTextPassword.getText().clear();
        editTextNickname.getText().clear();
    }

    enum AuthStage {
        REQUIRING_PHONE,
        REQUIRING_PASSWORD,
        REQUIRING_REGISTER
    }
}