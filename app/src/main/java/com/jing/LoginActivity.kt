package com.jing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.alibaba.fastjson2.JSONObject
import com.jing.utils.RSAUtils
import com.jing.utils.ToastUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 *@Author：静
 *@Package：com.jing
 *@Project：灵静
 *@Date：2024/10/5  下午11:59
 *@Filename：LoginActivity
 *@Version：1.0.0
 */
class LoginActivity : ComponentActivity() {

    companion object {
        //todo: 填写你的服务器地址
        private const val BASE_URL = "https://noticeably-positive-bird.ngrok-free.app/app/login"
        private const val SHARED_PREFS_NAME = "lingjing"
        private const val USER_ID_KEY = "userId"
        private const val CODE_KEY = "code"
        private const val EXPIRATION_TIME = "expirationTime"
    }

    private lateinit var editTextUserId: EditText

    private lateinit var editTextCode: EditText

    private lateinit var buttonLogin: Button

    private lateinit var rsaUtils: RSAUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUserId = findViewById(R.id.editTextUserId)
        editTextCode = findViewById(R.id.editTextCode)
        buttonLogin = findViewById(R.id.buttonLogin)
        rsaUtils = RSAUtils()
        try {
            rsaUtils.generateKeyPair()
        } catch (e: Exception) {
            Log.e("LoginActivity", "密钥生成失败", e)
        }

        buttonLogin.setOnClickListener {
            val userId = editTextUserId.text.toString()
            val code = editTextCode.text.toString()

            if (userId.isNotEmpty() && code.isNotEmpty() && userId.toLongOrNull() != null && code.toIntOrNull() != null) {
                // 登录逻辑
                loginUser(userId, code)
            } else {
                Toast.makeText(this, "请输入正确的用户名和密码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(userId: String, code: String) {
        val okHttpClient = OkHttpClient()
        val requestBody: RequestBody = JSONObject.of("userId", userId, "code", code).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BASE_URL)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonObject = JSONObject.parseObject(responseBody)
                        val result = jsonObject.getString("code")
                        if (result == "10000") {
                            try {
                                val sharedPreferences =
                                    getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                                with(sharedPreferences.edit()) {
                                    // 使用 RSA 公钥加密 userId 和 code
                                    val encryptedUserId = rsaUtils.encrypt(userId)
                                    val encryptedCode = rsaUtils.encrypt(code)

                                    putString(USER_ID_KEY, encryptedUserId)
                                    putString(CODE_KEY, encryptedCode)
                                    putLong(
                                        EXPIRATION_TIME,
                                        System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                                    )
                                    apply()
                                }
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                                Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: Exception) {
                                Log.e("LoginActivity", "加密失败", e)

                            }
                        } else {
                            ToastUtils.showToast(this@LoginActivity, "登陆失败")
                        }
                    } else {
                        ToastUtils.showToast(this@LoginActivity, "网络连接失败")
                    }
                }
            }
        })
    }
}