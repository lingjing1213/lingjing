package com.jing.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothUtils {

    companion object {
        private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
        private val mainHandler = Handler(Looper.getMainLooper())

        fun init(activity: AppCompatActivity) {
            enableBluetoothLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    mainHandler.post {
                        Toast.makeText(activity, "蓝牙已开启", Toast.LENGTH_SHORT).show()
                        // 检查位置服务
                        checkLocationServices(activity)

                    }
                } else {
                    mainHandler.post {
                        Toast.makeText(activity, "蓝牙未开启", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fun checkAndRequestPermissions(activity: AppCompatActivity) {
            val permissionsNeeded = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsNeeded.addAll(
                    listOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            } else {
                permissionsNeeded.addAll(
                    listOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }

            // 请求未授予的权限
            if (permissionsNeeded.any {
                    ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
                }) {
                ActivityCompat.requestPermissions(
                    activity,
                    permissionsNeeded.toTypedArray(),
                    1001
                )
            } else {
                checkLocationServices(activity) // 检查位置服务
            }
        }

        private fun checkLocationServices(activity: AppCompatActivity) {
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (!isLocationEnabled) {
                mainHandler.post {
                    Toast.makeText(activity, "请开启位置服务", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    activity.startActivity(intent) // 引导用户打开位置服务
                    checkAndEnableBluetooth(activity)
                }
            } else {
                // 位置服务已开启，接着检查蓝牙
                checkAndEnableBluetooth(activity)
            }
        }

        private fun checkAndEnableBluetooth(activity: AppCompatActivity) {
            val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter ?: run {
                mainHandler.post {
                    Toast.makeText(activity, "蓝牙设备不可用", Toast.LENGTH_SHORT).show()
                }
                return
            }

            if (!bluetoothAdapter.isEnabled) {
                mainHandler.post {
                    Toast.makeText(activity, "请开启蓝牙", Toast.LENGTH_SHORT).show()
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }
            } else {
                mainHandler.post {
                    Toast.makeText(activity, "蓝牙和位置服务已开启", Toast.LENGTH_SHORT).show()
                    openBluetoothSettings(activity)
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun openBluetoothSettings(activity: AppCompatActivity) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            activity.startActivity(intent)
            Toast.makeText(activity, "请在蓝牙设置中连接设备", Toast.LENGTH_SHORT).show()
        }
    }
}
