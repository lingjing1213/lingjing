package com.jing

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson2.JSONObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class LoginActivity : ComponentActivity() {


    private lateinit var editTextUserId: EditText

    private lateinit var editTextCode: EditText

    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUserId = findViewById(R.id.editTextUserId)
        editTextCode = findViewById(R.id.editTextCode)
        buttonLogin = findViewById(R.id.buttonLogin)

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
        val requestBody: RequestBody = JSONObject.of("userid", userId, "code", code).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            //todo
            .url("服务端地址")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback{
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
                           getSharedPreferences("user", MODE_PRIVATE).edit().
                           putString("userid", userId).putString("code", code).apply()
                           val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                           startActivity(intent)
                           overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
                           finish()
                           Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                       } else {
                           Toast.makeText(this@LoginActivity, "登录失败",Toast.LENGTH_SHORT).show()
                       }
                   }else{
                       Toast.makeText(this@LoginActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
                   }
               }
            }
        })
    }
}