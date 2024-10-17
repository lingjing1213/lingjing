package com.jing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.alibaba.fastjson2.JSONObject
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jing.service.CoyoteActivity
import com.jing.utils.RSAUtils
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
 * @Author：静
 * @Package：com.jing
 * @Project：灵静
 * @Date：2024/10/5  下午11:59
 * @Filename：HomeActivity
 * @Version：1.0.0
 */
class HomeActivity : ComponentActivity(){
    companion object {
        private const val SHARED_PREFS_NAME = "lingjing"
        private const val USER_ID_KEY = "userId"
        private const val CODE_KEY = "code"
        private const val EXPIRATION_TIME = "expirationTime"
        private const val BASE_URL = "https://noticeably-positive-bird.ngrok-free.app/app/checkUser"
    }

    private lateinit var rsaUtils: RSAUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        rsaUtils = RSAUtils()

        loadHomeContent()
        checkUserStatus()
    }

    /**
     * 检查用户状态
     * 此方法用于检查用户是否已经登录，以及登录后的状态验证
     */
    private fun checkUserStatus() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val encryptedUserId = sharedPreferences.getString(USER_ID_KEY, null)
        val encryptedCode = sharedPreferences.getString(CODE_KEY, null)
        val expirationTime = sharedPreferences.getLong(EXPIRATION_TIME, 0)
        val currentTime = System.currentTimeMillis()
        if (encryptedUserId == null || encryptedCode == null) {
            // 用户未登录，跳转到登录界面
            redirectToLogin()
        } else {
            // 验证用户状态
            if (currentTime > expirationTime) {
                Toast.makeText(this@HomeActivity, "用户已无效", Toast.LENGTH_SHORT).show()
                // 清除用户信息和密钥
                clearUserData()
                redirectToLogin()
            } else {
                val userId = rsaUtils.decrypt(encryptedUserId)
                val code = rsaUtils.decrypt(encryptedCode)
                validateUserStatus(userId, code)

            }
        }
    }

    private fun clearUserData() {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(USER_ID_KEY)
            remove(CODE_KEY)
            remove(EXPIRATION_TIME)
            apply()
        }
        // 从 KeyStore 中删除密钥
        try {
            rsaUtils.deleteKeyPair() // 假设 RSAKeyStoreUtil 中有 deleteKeyPair 方法
        } catch (e: Exception) {
            Log.e("HomeActivity", "清除密钥失败", e)
        }
    }

    /**
     * 验证用户状态
     *
     * 此方法用于向服务器发送用户ID和验证码，以验证用户是否 still 登录。
     * 如果用户状态无效，则跳转到登录界面。
     *
     * @param userId 用户ID
     * @param code 验证码
     */
    private fun validateUserStatus(userId: String, code: String) {
        val okHttpClient = OkHttpClient()
        val requestBody: RequestBody = JSONObject.of("userId", userId, "code", code).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BASE_URL)
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(
                                this@HomeActivity,
                                "用户状态无效，请重新登录",
                                Toast.LENGTH_SHORT
                            ).show()
                            redirectToLogin()
                        }
                    } else {
                        val responseData = response.body?.string()
                        val jsonObject = JSONObject.parseObject(responseData)

                        val status = jsonObject.getString("code")
                        if (status != "10000") {
                            // 用户状态无效，跳回登录界面
                            runOnUiThread{
                                Toast.makeText(this@HomeActivity, "用户已无效", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            redirectToLogin()

                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * 跳转到登录界面
     *
     * 此方法用于将当前活动（Activity）切换到登录界面它创建了一个指向LoginActivity的Intent，
     * 启动该Intent以开启新的Activity，并在启动后结束了当前的Activity这种跳转方式通常用于
     * 当前页面需要用户登录才能继续操作的情况
     */
    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 加载主页内容
     */
    private fun loadHomeContent() {

        val buttonCoyote = findViewById<Button>(R.id.buttonCoyote)
        val buttonKeyBox = findViewById<Button>(R.id.buttonKeyBox)
        val buttonVibratingEgg = findViewById<Button>(R.id.buttonVibratingEgg)
        val buttonSexMachine = findViewById<Button>(R.id.buttonSexMachine)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        buttonCoyote.setOnClickListener {
            val intent = Intent(this, CoyoteActivity::class.java)
            startActivity(intent)
            finish()

        }
        buttonKeyBox.setOnClickListener {
            // TODO: 蓝牙钥匙盒按钮点击处理
        }

        buttonVibratingEgg.setOnClickListener {
            // TODO: 跳蛋按钮点击处理

        }

        buttonSexMachine.setOnClickListener {
            //TODO: 炮机

        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_DMs -> true
                R.id.navigation_activity -> true
                R.id.navigation_my -> true
                else -> false
            }
        }
    }
}