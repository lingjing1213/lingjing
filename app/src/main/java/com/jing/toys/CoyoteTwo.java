package com.jing.toys;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

import java.util.Arrays;
import java.util.UUID;

/**
 * @Author：静
 * @Package：com.jing
 * @Project：灵静
 * @Date：2024/10/5 下午11:59
 * @Filename：CoyoteTwo
 * @Description：郊狼2.0
 * @Version：1.0.0
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

    private final Handler handler = new Handler();

    private Runnable batteryLevelRunnable;

    private final String SERVICE_BATTERY_LEVEL_UUID = "955a180a-0fe2-f5aa-a094-84b8d4f3e8ad";
    private final String CHARACTERISTIC_BATTERY_LEVEL_UUID = "955a1500-0fe2-f5aa-a094-84b8d4f3e8ad";

    private static final String SERVICE_PWM_AB2_UUID = "955a180b-0fe2-f5aa-a094-84b8d4f3e8ad";
    private static final String CHARACTERISTIC_PWM_AB2_UUID = "955a1504-0fe2-f5aa-a094-84b8d4f3e8ad";

    private boolean isRunning = false; // 用于标识当前状态
    private int totalTime = 20; // 总时间为20秒
    private int elapsedTime = 0; // 已经过的时间

    private Handler pulseHandler; // 用于定时发送脉冲
    private Runnable pulseRunnable; // 脉冲发送任务

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
        Button pendingButton = findViewById(R.id.btn_start_pause);

        selfEntertainmentButton.setOnClickListener(view -> handleSelfEntertainment());
        remoteControlButton.setOnClickListener(view -> handleRemoteControl());
        environmentButton.setOnClickListener(view -> handleEnvironment());
        exerciseButton.setOnClickListener(view -> handleExercise());
        musicWaveformButton.setOnClickListener(view -> handleMusicWaveform());
        pendingButton.setOnClickListener(view -> handleStartOrPause());

        // Initialize SeekBar listeners
        aIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aIntensityValue = progress;
                aIntensityTextView.setText("A 强度：" + aIntensityValue + " 上限 290");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户开始滑动时
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户停止滑动时
                writeIntensityToDevice(aIntensityValue, bIntensityValue);
            }
        });

        bIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bIntensityValue = progress;
                bIntensityTextView.setText("B 强度：" + bIntensityValue + " 上限 290");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户开始滑动时
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 可以添加代码在用户停止滑动时
                writeIntensityToDevice(aIntensityValue, bIntensityValue);
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

        if (bluetoothGatt != null) {
            batteryLevelRunnable = new Runnable() {
                @Override
                public void run() {
                    if (bluetoothGatt != null) {

                        BluetoothGattCharacteristic batteryCharacteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_BATTERY_LEVEL_UUID)).getCharacteristic(UUID.fromString(CHARACTERISTIC_BATTERY_LEVEL_UUID));
                        if (batteryCharacteristic != null) {
                            bluetoothGatt.readCharacteristic(batteryCharacteristic);
                        }
                        handler.postDelayed(this, 60000);
                    }
                }
            };
        }

        pulseHandler = new Handler();
        pulseRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && elapsedTime < totalTime) {
                    sendPulseParameters(5, 95, 20); // 发送脉冲参数
                    elapsedTime++;
                    pulseHandler.postDelayed(this, 100); // 每秒发送一次
                } else {
                    isRunning = false; // 停止运行
                    elapsedTime = 0; // 重置经过时间
                }
            }
        };

    }



    @SuppressLint("MissingPermission")
    private void writeIntensityToDevice(int aIntensityValue, int bIntensityValue) {
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic pwmAB2Characteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_PWM_AB2_UUID)).getCharacteristic(UUID.fromString(CHARACTERISTIC_PWM_AB2_UUID));
            if (pwmAB2Characteristic != null) {
                int aStrength = aIntensityValue * 7;
                int bStrength = bIntensityValue * 7;
                if (aStrength < 0 || aStrength > 2047 || bStrength < 0 || bStrength > 2047) {
                    Log.e("Bluetooth", "强度值必须在0到290之间");
                    return;
                }
                int combinedValue = (bStrength << 11) | aStrength;
                byte[] value = new byte[3];
                value[0] = (byte) ((combinedValue >> 16) & 0xFF);  // 高位字节
                value[1] = (byte) ((combinedValue >> 8) & 0xFF);   // 中间字节
                value[2] = (byte) (combinedValue & 0xFF);
                int i = (combinedValue >> 8) & 0xFF;
                int y = combinedValue & 0xFF;
                pwmAB2Characteristic.setValue(value);
                boolean writeSuccess = bluetoothGatt.writeCharacteristic(pwmAB2Characteristic);
                Log.d("CoyoteTwo", "Write success: " + writeSuccess + ", Values: " + Arrays.toString(value) +"i值"+ i +" Yzhi"+y );
                Log.d("Bluetooth", "写入成功: A通道强度=" + aIntensityValue + ", B通道强度=" + bIntensityValue);
            } else {
                Log.e("CoyoteTwo", "未找到用于写入的PWM特性。");
            }
        } else {
            Log.e("CoyoteTwo", "BluetoothGatt为空，无法写入强度。");
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
                BluetoothGattCharacteristic batteryCharacteristic = gatt
                        .getService(UUID.fromString(SERVICE_BATTERY_LEVEL_UUID))
                        .getCharacteristic(UUID.fromString(CHARACTERISTIC_BATTERY_LEVEL_UUID));


                if (batteryCharacteristic != null) {
                    gatt.readCharacteristic(batteryCharacteristic);
                } else {
                    Log.e("CoyoteTwo", "Characteristic not found.");
                }

                BluetoothGattCharacteristic pwmCharacteristic = gatt
                        .getService(UUID.fromString(SERVICE_PWM_AB2_UUID))
                        .getCharacteristic(UUID.fromString(CHARACTERISTIC_PWM_AB2_UUID));

                handler.postDelayed(() -> {
                    if (pwmCharacteristic != null) {
                        gatt.readCharacteristic(pwmCharacteristic);
                    }
                }, 3000);
            } else {
                Log.w("CoyoteTwo", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("CoyoteTwo", "Characteristic UUID: " + characteristic.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (CHARACTERISTIC_BATTERY_LEVEL_UUID.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length > 0) {
                        int batteryLevel = value[0] & 0xFF;
                        Log.d("CoyoteTwo", "Battery Level: " + batteryLevel);
                        runOnUiThread(() -> batteryTextView.setText("电量:" + batteryLevel + "%"));
                    }
                } else if (CHARACTERISTIC_PWM_AB2_UUID.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length == 3) {
                        int combinedValue = ((value[0] & 0xFF) << 16) | ((value[1] & 0xFF) << 8) | (value[2] & 0xFF);
                        int aChannelStrength = combinedValue & 0x07FF;  // A通道强度 (10-0位)
                        int bChannelStrength = (combinedValue >> 11) & 0x07FF;
                        aIntensityValue = aChannelStrength / 7;
                        bIntensityValue = bChannelStrength / 7;
                        runOnUiThread(() -> {
                            aIntensitySeekBar.setProgress(aIntensityValue);
                            bIntensitySeekBar.setProgress(bIntensityValue);
                        });
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
        if (bluetoothGatt == null) {
            bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt().getDevice().connectGatt(this, false, bluetoothGattCallback);
        }
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
            bluetoothGatt.close();  // 释放Gatt资源
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

    private void handleStartOrPause() {

        if (isRunning) {
            // 暂停脉冲发送
            isRunning = false;
            pulseHandler.removeCallbacks(pulseRunnable);
            Log.d("CoyoteTwo", "脉冲发送已暂停");
            ToastUtils.showToast(this, "已暂停");
        } else {
            // 开始脉冲发送
            isRunning = true;
            elapsedTime = 0; // 重置已过时间
            pulseHandler.post(pulseRunnable); // 开始脉冲发送
            Log.d("CoyoteTwo", "脉冲发送已开始");
            ToastUtils.showToast(this, "已开始");
        }
    }

    private void pauseOutput() {
    }

    private void startInput() {
        elapsedTime = 0; // 重置已过去时间


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

    @SuppressLint("MissingPermission")
    private void sendPulseParameters(int X, int Y, int Z) {
        if (bluetoothGatt == null) return;

        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString("955a180b-0fe2-f5aa-a094-84b8d4f3e8ad"));
        if (service != null) {
            BluetoothGattCharacteristic pwmA34 = service.getCharacteristic(UUID.fromString("955a1505-0fe2-f5aa-a094-84b8d4f3e8ad"));

            // 构造数据
            byte[] data = new byte[3];
            data[0] = (byte) ((X & 0x1F) | ((Y & 0x3FF) << 5) | ((Z & 0x1F) << 15)); // 按位打包

            // 写入特性
            pwmA34.setValue(data);
            bluetoothGatt.writeCharacteristic(pwmA34);
        }
    }
}
