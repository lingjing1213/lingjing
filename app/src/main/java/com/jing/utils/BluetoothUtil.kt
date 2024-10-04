package com.jing.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothUtil {

    companion object {
        const val REQUEST_CODE = 1001
        private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
        private var bluetoothLeScanner: BluetoothLeScanner? = null
        private val discoveredDevices = mutableSetOf<String>()
        private var scanningHandler: Handler? = null
        private var scanCallback: ScanCallback? = null

        fun init(activity: AppCompatActivity) {
            enableBluetoothLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(activity, "蓝牙已开启", Toast.LENGTH_SHORT).show()
                    startScanning(activity) // 蓝牙开启后开始扫描
                } else {
                    Toast.makeText(activity, "蓝牙未开启", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun checkAndRequestPermissions(activity: AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        REQUEST_CODE
                    )
                } else {
                    checkAndEnableBluetooth(activity)
                }
            } else {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_CODE
                    )
                } else {
                    checkAndEnableBluetooth(activity)
                }
            }
        }

        private fun checkAndEnableBluetooth(activity: AppCompatActivity) {
            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                Toast.makeText(activity, "蓝牙设备不可用", Toast.LENGTH_SHORT).show()
                return
            }
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(activity, "蓝牙未开启，正在开启...", Toast.LENGTH_SHORT).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            } else {
                Toast.makeText(activity, "蓝牙已开启", Toast.LENGTH_SHORT).show()
                startScanning(activity)
            }
        }

        @SuppressLint("MissingPermission")
        fun startScanning(activity: AppCompatActivity) {
            // 检查权限
            checkAndRequestPermissions(activity)

            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

            if (bluetoothLeScanner != null) {
                Toast.makeText(activity, "开始扫描设备...", Toast.LENGTH_SHORT).show()

                // 清空之前的发现设备
                discoveredDevices.clear()

                // 定义扫描回调
                scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        super.onScanResult(callbackType, result)
                        val deviceAddress = result.device.address

                        // 只处理新设备
                        if (discoveredDevices.add(deviceAddress)) {
                            val deviceName = result.device.name ?: "未知设备"
                            Toast.makeText(activity, "发现设备: $deviceName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onBatchScanResults(results: List<ScanResult>) {
                        super.onBatchScanResults(results)
                        for (result in results) {
                            val deviceName = result.device.name ?: "未知设备"
                            Toast.makeText(activity, "发现设备: $deviceName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        Toast.makeText(activity, "扫描失败: $errorCode", Toast.LENGTH_SHORT).show()
                    }
                }

                // 开始扫描
                bluetoothLeScanner!!.startScan(scanCallback)

                // 开始计时，20秒后停止扫描
                scanningHandler = Handler(Looper.getMainLooper())
                scanningHandler?.postDelayed({
                    stopScanning(activity)
                }, 20000) // 20秒
            } else {
                Toast.makeText(activity, "无法获取蓝牙扫描器", Toast.LENGTH_SHORT).show()
            }
        }

        @SuppressLint("MissingPermission")
        fun stopScanning(activity: AppCompatActivity) {
            bluetoothLeScanner?.stopScan(scanCallback!!) // 使用定义的 scanCallback 实例
            Toast.makeText(activity, "停止扫描", Toast.LENGTH_SHORT).show()
            discoveredDevices.clear() // 清空已发现设备列表
            scanningHandler?.removeCallbacksAndMessages(null) // 清除所有回调
        }
    }

}

