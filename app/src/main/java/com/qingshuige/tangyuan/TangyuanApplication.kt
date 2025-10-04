package com.qingshuige.tangyuan

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.GsonBuilder
import com.qingshuige.tangyuan.network.ApiInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.GeneralSecurityException

class TangyuanApplication : Application() {

    companion object {
        private lateinit var retrofit: Retrofit
        private lateinit var api: ApiInterface
        const val coreDomain = "https://ty.qingshuige.ink/"
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var tokenManager: TokenManager

        fun getRetrofit(): Retrofit = retrofit
        fun getApi(): ApiInterface = api
        fun getCoreDomain(): String = coreDomain
        fun getSharedPreferences(): SharedPreferences = sharedPreferences
        fun getTokenManager(): TokenManager = tokenManager
    }

    override fun onCreate() {
        super.onCreate()

        try {
            sharedPreferences = EncryptedSharedPreferences.create(
                "Tangyuan",
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        tokenManager = TokenManager()
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // 匹配 "2024-04-08T00:00:00"
            .create()
        val pureRetrofit = Retrofit.Builder()
            .baseUrl("${coreDomain}api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val pureApi = pureRetrofit.create(ApiInterface::class.java)
        val client = OkHttpClient.Builder()
            .addInterceptor(JwtInterceptor(tokenManager))
            .authenticator(JwtAuthenticator(tokenManager, pureApi))
//            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
        retrofit = Retrofit.Builder()
            .baseUrl("${coreDomain}api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        api = retrofit.create(ApiInterface::class.java)
    }
}