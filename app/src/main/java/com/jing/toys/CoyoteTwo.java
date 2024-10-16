package com.jing.toys;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.jing.R;
import com.jing.utils.BluetoothGattManager;
import com.jing.utils.ToastUtils;
import java.util.UUID;

/**
 *@Author：静
 *@Package：com.jing
 *@Project：灵静
 *@Date：2024/10/5  下午11:59
 *@Filename：CoyoteTwo
 *@Description：郊狼2.0
 *@Version：1.0.0
 */
public class CoyoteTwo extends AppCompatActivity {

    private BluetoothGatt bluetoothGatt;
    private TextView batteryTextView;
    private TextView aIntensityTextView;
    private TextView bIntensityTextView;
    private SeekBar aIntensitySeekBar;
    private SeekBar bIntensitySeekBar;

    private int aIntensityValue = 0;
    private int bIntensityValue = 0;

    private Handler handler = new Handler();

    private Runnable batteryLevelRunnable ;

    private final String SERVICE_BATTERY_LEVEL_UUID = "955a180a-0fe2-f5aa-a094-84b8d4f3e8ad";
    private final String CHARACTERISTIC_BATTERY_LEVEL_UUID = "955a1500-0fe2-f5aa-a094-84b8d4f3e8ad";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coyotetwo);

        batteryTextView = findViewById(R.id.battery_level);
        aIntensityTextView = findViewById(R.id.a_intensity_text); // Assume you add this TextView in XML
        bIntensityTextView = findViewById(R.id.b_intensity_text); // Assume you add this TextView in XML
        aIntensitySeekBar = findViewById(R.id.a_intensity);
        bIntensitySeekBar = findViewById(R.id.b_intensity);

        Button selfEntertainmentButton = findViewById(R.id.btn_self_entertain);
        Button remoteControlButton = findViewById(R.id.btn_remote_control);
        Button environmentButton = findViewById(R.id.btn_environment);
        Button exerciseButton = findViewById(R.id.btn_exercise);
        Button musicWaveformButton = findViewById(R.id.btn_music_waveform);
        Button pendingButton = findViewById(R.id.btn_pending);

        selfEntertainmentButton.setOnClickListener(view -> handleSelfEntertainment());
        remoteControlButton.setOnClickListener(view -> handleRemoteControl());
        environmentButton.setOnClickListener(view -> handleEnvironment());
        exerciseButton.setOnClickListener(view -> handleExercise());
        musicWaveformButton.setOnClickListener(view -> handleMusicWaveform());
        pendingButton.setOnClickListener(view -> handlePending());

        // Initialize SeekBar listeners
        aIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aIntensityValue = progress;
                aIntensityTextView.setText("A 强度：" + aIntensityValue + "（用户自定义）上限 290");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户开始滑动时
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户停止滑动时
            }
        });

        bIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bIntensityValue = progress;
                bIntensityTextView.setText("B 强度：" + bIntensityValue + "（用户自定义）上限 290");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户开始滑动时
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户停止滑动时
            }
        });

        // Set click listeners on TextViews to allow modification
        aIntensityTextView.setOnClickListener(v -> showInputDialog("A 强度", aIntensityValue, newValue -> {
            aIntensityValue = newValue;
            aIntensitySeekBar.setProgress(aIntensityValue);
            aIntensityTextView.setText("A 强度：" + aIntensityValue + " 上限 290");
        }));

        bIntensityTextView.setOnClickListener(v -> showInputDialog("B 强度", bIntensityValue, newValue -> {
            bIntensityValue = newValue;
            bIntensitySeekBar.setProgress(bIntensityValue);
            bIntensityTextView.setText("B 强度：" + bIntensityValue + " 上限 290");
        }));

        bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt().getDevice().connectGatt(this, false, bluetoothGattCallback);
        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
        } else {
            ToastUtils.showToast(this, "没有蓝牙连接信息");
        }

        if (bluetoothGatt != null){
           batteryLevelRunnable = new Runnable() {
               @Override
               public void run() {
                   if (bluetoothGatt != null){

                       BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_BATTERY_LEVEL_UUID)).getCharacteristic(UUID.fromString(CHARACTERISTIC_BATTERY_LEVEL_UUID));
                       if (characteristic != null){
                           bluetoothGatt.readCharacteristic(characteristic);
                       }
                       handler.postDelayed(this, 5000);
                   }
               }
           };
        }

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                ToastUtils.showToast(CoyoteTwo.this, "蓝牙连接断开");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristic = gatt
                        .getService(UUID.fromString(SERVICE_BATTERY_LEVEL_UUID))
                        .getCharacteristic(UUID.fromString(CHARACTERISTIC_BATTERY_LEVEL_UUID));

                if (characteristic != null) {
                    gatt.readCharacteristic(characteristic);
                } else {
                    Log.e("CoyoteTwo", "Characteristic not found.");
                }
            } else {
                Log.w("CoyoteTwo", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (CHARACTERISTIC_BATTERY_LEVEL_UUID.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length > 0) {
                        int batteryLevel = value[0] & 0xFF;
                        Log.d("CoyoteTwo", "Battery Level: " + batteryLevel);
                        runOnUiThread(() -> batteryTextView.setText("电量:" + batteryLevel + "%"));
                    }
                }
            } else {
                Log.e("CoyoteTwo", "Characteristic read failed, status: " + status);
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
            handler.post(batteryLevelRunnable);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onPause() {
        super.onPause();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            handler.removeCallbacks(batteryLevelRunnable);
        }
    }

    // Button click handlers
    private void handleSelfEntertainment() {
        ToastUtils.showToast(this, "自娱自乐功能暂未实现");
    }

    private void handleRemoteControl() {
        ToastUtils.showToast(this, "远程控制功能暂未实现");
    }

    private void handleEnvironment() {
        ToastUtils.showToast(this, "环境功能暂未实现");
    }

    private void handleExercise() {
        ToastUtils.showToast(this, "运动功能暂未实现");
    }

    private void handleMusicWaveform() {
        ToastUtils.showToast(this, "音乐转波形功能暂未实现");
    }

    private void handlePending() {
        ToastUtils.showToast(this, "待定功能暂未实现");
    }

    private void showInputDialog(String title, int currentValue, OnValueSetListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(290);
        numberPicker.setMinValue(0);
        numberPicker.setValue(currentValue);
        builder.setView(numberPicker);

        builder.setPositiveButton("确定", (dialog, which) -> {
            int newValue = numberPicker.getValue();
            listener.onValueSet(newValue);
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    interface OnValueSetListener {
        void onValueSet(int newValue);
    }
}
