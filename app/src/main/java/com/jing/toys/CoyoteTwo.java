package com.jing.toys;

import android.bluetooth.BluetoothGatt;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 *@Author：静
 *@Package：com.jing
 *@Project：灵境
 *@Date：2024/10/5  下午11:59
 *@Filename：CoyoteTwo
 *@Description：郊狼2.0
 *@Version：1.0.0
 */
public class CoyoteTwo extends AppCompatActivity {

    private BluetoothGatt bluetoothGatt;

    private TextView batteryLevelTextView;

    private final String SERVICE_UUID = "955A180A-0FE2-F5AA-A094-84B8D4F3E8AD";

    private final String CHARACTERISTIC_UUID = "955A1500-0FE2-F5AA-A094-84B8D4F3E8AD";

    private final String DESCRIPTOR_UUID = "955A180A-0FE2-F5AA-A094-84B8D4F3E8AD";

    private final String NOTIFICATION_UUID = "955A180D-0FE2-F5AA-A094-84B8D4F3E8AD";
}
