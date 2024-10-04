package com.jing

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.alibaba.fastjson2.JSONObject
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class HomeActivity : ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        loadHomeContent()
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val sharedPreferences = getSharedPreferences("lingjing", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)
        val code = sharedPreferences.getString("code", null)

        if (userId == null || code == null) {
            // 用户未登录，跳转到登录界面
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // 验证用户状态
            validateUserStatus(userId, code)
        }
    }
    private fun validateUserStatus(userId: String, code: String) {

        val okHttpClient = OkHttpClient()
        val requestBody: RequestBody = JSONObject.of("userId", userId, "code", code).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            //todo 修改地址
            .url("")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@HomeActivity, "用户状态无效，请重新登录", Toast.LENGTH_SHORT).show()
                            redirectToLogin()
                        }
                    } else {
                        val responseData = response.body?.string()
                        val jsonObject = JSONObject.parseObject(responseData)

                        val status = jsonObject.getString("code")
                        if (status == "10000") {
                            // 用户状态有效，继续停留在主界面
                            runOnUiThread {
                                Toast.makeText(this@HomeActivity, "用户登录成功", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 用户状态无效，跳回登录界面
                            runOnUiThread {
                                Toast.makeText(this@HomeActivity, "用户已无效", Toast.LENGTH_SHORT).show()
                                redirectToLogin()
                            }
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

    private fun loadHomeContent() {

        val buttonCoyote = findViewById<Button>(R.id.buttonCoyote)
        val buttonKeyBox = findViewById<Button>(R.id.buttonKeyBox)
        val buttonVibratingEgg = findViewById<Button>(R.id.buttonVibratingEgg)
        val buttonSexMachine = findViewById<Button>(R.id.buttonSexMachine)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        buttonCoyote.setOnClickListener {

        }
        buttonKeyBox.setOnClickListener {
            // TODO: 蓝牙钥匙盒按钮点击处理
        }

        buttonVibratingEgg.setOnClickListener{
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
