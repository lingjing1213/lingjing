package com.jing.toys

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jing.R
import com.jing.utils.BluetoothUtils
import java.util.UUID

/**
 *@Author：静
 *@Package：com.jing
 *@Project：灵境
 *@Date：2024/10/5  下午11:59
 *@Filename：HomeActivity
 *@Description：郊狼
 *@Version：1.0.0
 */
class CoyoteActivity : AppCompatActivity() {

    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var batteryLevelTextView: TextView
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coyote)
        batteryLevelTextView = findViewById(R.id.batteryLevelText)
        BluetoothUtils.init(this)
        BluetoothUtils.checkAndRequestPermissions(this)

        val bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceAddress = device.address
                bluetoothGatt = device.connectGatt(this, false, gattCallback)
                break // 如果只需连接第一个设备，可以在这里跳出循环
            }
        } else {
            Toast.makeText(this, "未配对设备", Toast.LENGTH_SHORT).show()
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 连接成功后发现服务
                gatt?.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取电量特征
                val batteryLevelCharacteristic: BluetoothGattCharacteristic? =
                    gatt?.getService(UUID.fromString("955A180A-0FE2-F5AA-A094-84B8D4F3E8AD"))
                        ?.getCharacteristic(UUID.fromString("955A1500-0FE2-F5AA-A094-84B8D4F3E8AD"))

                batteryLevelCharacteristic?.let {
                    // 读取电量
                    gatt.readCharacteristic(it)
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val batteryLevel: Int = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) ?: 0
                runOnUiThread {
                    batteryLevelTextView.text = "电量: $batteryLevel%"
                }
            }
        }
    }
}
