package com.jing.toys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jing.R
import com.jing.utils.BluetoothUtil

/**
 * 郊狼
 */
class Coyote : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coyote)
        // 初始化蓝牙启动器
        BluetoothUtil.init(this)

        // 检查并启动蓝牙
        BluetoothUtil.checkAndRequestPermissions(this)

        BluetoothUtil.startScanning(this)

    }

}