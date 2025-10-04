package com.qingshuige.tangyuan

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.CreateUserDto
import com.qingshuige.tangyuan.network.LoginDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var buttonLogin: Button
    private lateinit var editTextPhone: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextNickname: EditText

    private lateinit var tm: TokenManager
    private var stage = AuthStage.REQUIRING_PHONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_login)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        buttonLogin = findViewById(R.id.buttonLogin)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextNickname = findViewById(R.id.editTextNickname)

        tm = TangyuanApplication.getTokenManager()

        editTextPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                stage = AuthStage.REQUIRING_PHONE
                updateUIAccordingToStage()
            }

            override fun afterTextChanged(editable: Editable) {
            }
        })

        buttonLogin.setOnClickListener {
            if (editTextPhone.text.isNotEmpty()) {
                buttonLogin.isEnabled = false
                when (stage) {
                    AuthStage.REQUIRING_PHONE -> {
                        if (!DataTools.isValidPhoneNumber(editTextPhone.text.toString(), "CN")) {
                            Toast.makeText(this, R.string.valid_phone_number, Toast.LENGTH_SHORT)
                                .show()
                            buttonLogin.isEnabled = true
                            return@setOnClickListener
                        }
                        ApiHelper.judgeIfUserExistsAsync(editTextPhone.text.toString()) { result ->
                            if (result == null) {
                                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT)
                                    .show()
                            } else if (result) {
                                stage = AuthStage.REQUIRING_PASSWORD
                            } else {
                                stage = AuthStage.REQUIRING_REGISTER
                            }
                            runOnUiThread {
                                updateUIAccordingToStage()
                                buttonLogin.isEnabled = true
                            }
                        }
                    }

                    AuthStage.REQUIRING_PASSWORD -> {
                        if (!TextUtils.isEmpty(editTextPassword.text)) {
                            login(editTextPhone.text.toString(), editTextPassword.text.toString())
                        } else {
                            Toast.makeText(this, R.string.password_is_empty, Toast.LENGTH_SHORT)
                                .show()
                            buttonLogin.isEnabled = true
                        }
                    }

                    AuthStage.REQUIRING_REGISTER -> {
                        if (editTextPassword.text.isNotEmpty() && editTextNickname.text.isNotEmpty()) {
                            signUpAndLogIn(
                                editTextPhone.text.toString(),
                                editTextPassword.text.toString(),
                                editTextNickname.text.toString()
                            )
                        } else {
                            Toast.makeText(this, R.string.fields_unfinished, Toast.LENGTH_SHORT)
                                .show()
                            buttonLogin.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun login(phoneNumber: String, password: String) {
        tm.setPhoneNumberAndPassword(phoneNumber, password)
        val dto = LoginDto().apply {
            this.phoneNumber = phoneNumber
            this.password = password
        }
        TangyuanApplication.getApi().login(dto).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.code() == 400) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.phone_number_password_not_match),
                        Toast.LENGTH_SHORT
                    ).show()
                    runOnUiThread { buttonLogin.isEnabled = true }
                } else {
                    response.body()?.values?.iterator()?.next()?.let { token ->
                        tm.token = token
                        Toast.makeText(
                            this@LoginActivity,
                            getString(R.string.login_success),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, throwable: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.network_error),
                    Toast.LENGTH_SHORT
                ).show()
                runOnUiThread { buttonLogin.isEnabled = true }
            }
        })
    }

    private fun signUpAndLogIn(phoneNumber: String, password: String, nickname: String) {
        val dto = CreateUserDto().apply {
            this.phoneNumber = phoneNumber
            this.password = password
            this.nickName = nickname
            this.avatarGuid = "8f416888-2ca4-4cda-8882-7f06a89630a2"
            this.isoRegionName = "CN"
        }
        TangyuanApplication.getApi().postUser(dto).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                tm.setPhoneNumberAndPassword(phoneNumber, password)
                val loginDto = LoginDto().apply {
                    this.phoneNumber = phoneNumber
                    this.password = password
                }
                TangyuanApplication.getApi().login(loginDto)
                    .enqueue(object : Callback<Map<String, String>> {
                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {
                            response.body()?.values?.iterator()?.next()?.let { token ->
                                tm.token = token
                                Toast.makeText(
                                    this@LoginActivity,
                                    getString(R.string.signup_login_success),
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        }

                        override fun onFailure(
                            call: Call<Map<String, String>>,
                            throwable: Throwable
                        ) {
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.network_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }

            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.network_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun updateUIAccordingToStage() {
        when (stage) {
            AuthStage.REQUIRING_PHONE -> {
                editTextPassword.visibility = View.GONE
                editTextNickname.visibility = View.GONE
            }

            AuthStage.REQUIRING_PASSWORD -> {
                editTextPassword.visibility = View.VISIBLE
                editTextNickname.visibility = View.GONE
            }

            AuthStage.REQUIRING_REGISTER -> {
                editTextPassword.visibility = View.VISIBLE
                editTextNickname.visibility = View.VISIBLE
            }
        }

        editTextPassword.text.clear()
        editTextNickname.text.clear()
    }

    enum class AuthStage {
        REQUIRING_PHONE,
        REQUIRING_PASSWORD,
        REQUIRING_REGISTER
    }
}