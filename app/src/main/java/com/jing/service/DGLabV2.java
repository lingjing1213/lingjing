package com.jing.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jing.R;
import com.jing.utils.BluetoothGattManager;
import com.jing.utils.CoyoteConstant;
import com.jing.utils.ToastUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author：静
 * @Package：com.jing.service
 * @Project：灵静
 * @name：DglabV2
 * @Date：2024/10/19 下午2:42
 * @Filename：DglabV2
 * @Version：1.0.0
 */
public class DGLabV2 extends AppCompatActivity {

    private static final String TAG = "DGLabV2";

    private BluetoothGatt bluetoothGatt;

    private TextView batteryView;

    private TextView AChannelView;

    private TextView BChannelView;

    private SeekBar AChannel;

    private SeekBar BChannel;

    private int aIntensityValue = 0;

    private int bIntensityValue = 0;

    private final Handler handler = new Handler();

    private Runnable batteryLevelRunnable;

    private ScheduledExecutorService scheduler;


    @SuppressLint({"MissingPermission"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coyotetwo);

        batteryView = findViewById(R.id.battery_level);
        AChannelView = findViewById(R.id.a_intensity_text);
        AChannel = findViewById(R.id.a_intensity);
        BChannelView = findViewById(R.id.b_intensity_text);
        BChannel = findViewById(R.id.b_intensity);

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


        scheduler = Executors.newScheduledThreadPool(1);

        bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt().getDevice().connectGatt(this, false, bluetoothGattCallback);
        if (bluetoothGatt == null) {
            Intent intent = new Intent(this, CoyoteActivity.class);
            startActivity(intent);
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
            batteryLevelRunnable = new Runnable() {
                @Override
                public void run() {
                    BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(CoyoteConstant.DGLabV2_BATTERY_SERVICE)).getCharacteristic(UUID.fromString(CoyoteConstant.DGLabV2_BATTERY_CHARACTERISTIC));
                    if (characteristic != null) {
                        bluetoothGatt.readCharacteristic(characteristic);
                    }
                    handler.postDelayed(this, 60000);
                }
            };
        } else {
            ToastUtils.showToast(this, "没有蓝牙连接信息");
        }

        AChannel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                aIntensityValue = progress;
                AChannelView.setText(MessageFormat.format("A通道：{0}", aIntensityValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writeIntensityToDevice(aIntensityValue, bIntensityValue);
            }
        });

        BChannel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bIntensityValue = progress;
                BChannelView.setText(MessageFormat.format("B通道：{0}", bIntensityValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                writeIntensityToDevice(aIntensityValue, bIntensityValue);
            }
        });
    }

    private void writeIntensityToDevice(int aIntensityValue, int bIntensityValue) {
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic pwmAB2Characteristic = bluetoothGatt.getService(UUID.fromString(CoyoteConstant.DGLabV2_PWM_AB_SERVICE))
                    .getCharacteristic(UUID.fromString(CoyoteConstant.DGLabV2_PWM_AB_STRENGTH_CHARACTERISTIC));
            if (pwmAB2Characteristic != null) {
                int aStrength = aIntensityValue * 7;
                int bStrength = bIntensityValue * 7;
                if (aStrength < 0 || aStrength > 2047 || bStrength < 0 || bStrength > 2047) {
                    ToastUtils.showToast(this, "强度值设置错误");
                }
                byte[] abPowerToByte = StrengthAndWave.abPowerToByte(aStrength, bStrength);
                pwmAB2Characteristic.setValue(abPowerToByte);
                @SuppressLint("MissingPermission") boolean writeSuccess = bluetoothGatt.writeCharacteristic(pwmAB2Characteristic);
                Log.d(TAG, "Write success: " + writeSuccess + ", Values: " + Arrays.toString(abPowerToByte));
            }
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
                ToastUtils.showToast(DGLabV2.this, "蓝牙连接断开");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic batteryCharacteristic = gatt
                        .getService(UUID.fromString(CoyoteConstant.DGLabV2_BATTERY_SERVICE))
                        .getCharacteristic(UUID.fromString(CoyoteConstant.DGLabV2_BATTERY_CHARACTERISTIC));


                if (batteryCharacteristic != null) {
                    gatt.readCharacteristic(batteryCharacteristic);
                } else {
                    Log.e(TAG, "特征未找到。");
                }

                BluetoothGattCharacteristic pwmCharacteristic = gatt
                        .getService(UUID.fromString(CoyoteConstant.DGLabV2_PWM_AB_SERVICE))
                        .getCharacteristic(UUID.fromString(CoyoteConstant.DGLabV2_PWM_AB_STRENGTH_CHARACTERISTIC));

                handler.postDelayed(() -> {
                    if (pwmCharacteristic != null) {
                        gatt.readCharacteristic(pwmCharacteristic);
                    }
                }, 3000);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "Characteristic UUID: " + characteristic.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (CoyoteConstant.DGLabV2_BATTERY_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length > 0) {
                        int batteryLevel = value[0] & 0xFF;
                        Log.d(TAG, "Battery Level: " + batteryLevel);
                        runOnUiThread(() -> batteryView.setText(MessageFormat.format("电量：{0}%", batteryLevel)));
                    }
                } else if (CoyoteConstant.DGLabV2_PWM_AB_STRENGTH_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length == 3) {
                        int result = ((value[0] & 0xFF)) |
                                ((value[1] & 0xFF) << 8) |
                                ((value[2] & 0xFF) << 16);
                        int aChannelBits = result & 0x7FF; // 取低11位
                        int bChannelBits = (result >> 11) & 0x7FF; // 取11-21位
                        aIntensityValue = aChannelBits / 7;
                        bIntensityValue = bChannelBits / 7;
                        runOnUiThread(() -> {
                            AChannel.setProgress(aIntensityValue);
                            BChannel.setProgress(bIntensityValue);
                        });
                    }
                }
            } else {
                Log.e(TAG, "Characteristic read failed, status: " + status);
            }
        }
    };


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
        if (bluetoothGatt == null) return;
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(CoyoteConstant.DGLabV2_PWM_AB_SERVICE));
        if (service != null) {
            UUID aChannelCharacteristicUUid = UUID.fromString(CoyoteConstant.DGLabV2_WAVE_A_DIRECTION_CHARACTERISTIC);
            UUID bChannelCharacteristicUUid = UUID.fromString(CoyoteConstant.DGLabV2_WAVE_B_DIRECTION_CHARACTERISTIC);
            BluetoothGattCharacteristic aChannelCharacteristic = null;
            BluetoothGattCharacteristic bChannelCharacteristic = null;
            aChannelCharacteristic = service.getCharacteristic(aChannelCharacteristicUUid);
            bChannelCharacteristic = service.getCharacteristic(bChannelCharacteristicUUid);

            if (aChannelCharacteristic != null || bChannelCharacteristic != null) {
                BluetoothGattCharacteristic finalAChannelCharacteristic = aChannelCharacteristic;
                BluetoothGattCharacteristic finalBChannelCharacteristic = bChannelCharacteristic;
                scheduler = Executors.newScheduledThreadPool(1);
                Log.d(TAG, "开始发送波形数据");
                ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
                        sendPulseParameters(finalAChannelCharacteristic, finalBChannelCharacteristic,
                                StrengthAndWave.wave(3, 90, 10)), 0, 100, TimeUnit.MILLISECONDS);
                scheduler.schedule(() -> {
                    scheduledFuture.cancel(false);
                    scheduler.shutdown(); // 关闭调度器
                }, 10, TimeUnit.SECONDS);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void sendPulseParameters(BluetoothGattCharacteristic aChannelCharacteristic, BluetoothGattCharacteristic bChannelCharacteristic, byte[] bytes) {

        if (aChannelCharacteristic != null) {
            aChannelCharacteristic.setValue(bytes);
            if (bChannelCharacteristic != null) {
                bluetoothGatt.setCharacteristicNotification(bChannelCharacteristic, true);
            }
            boolean writeAChannelIsSuccess = bluetoothGatt.writeCharacteristic(aChannelCharacteristic);
            Log.d(TAG, "A通道是否写入成功: " + writeAChannelIsSuccess + "  bytes: " + Arrays.toString(bytes));
        }
        if (bChannelCharacteristic != null) {
            bChannelCharacteristic.setValue(bytes);
            if (aChannelCharacteristic != null) {
                bluetoothGatt.setCharacteristicNotification(aChannelCharacteristic, true);
            }
            boolean writeBChannelIsSuccess = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
            Log.d(TAG, "B通道是否写入成功: " + writeBChannelIsSuccess + "  bytes: " + Arrays.toString(bytes));
        }

        if (aChannelCharacteristic == null) {
            return;
        }
        if (bChannelCharacteristic == null) {
            return;
        }
    }
}

