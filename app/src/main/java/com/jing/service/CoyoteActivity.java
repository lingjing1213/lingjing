package com.jing.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.jing.R;
import com.jing.toys.CoyoteTwo;
import com.jing.utils.BluetoothGattManager;
import com.jing.utils.BluetoothUtils;
import com.jing.utils.CoyoteConstant;
import com.jing.utils.ToastUtils;

/**
 * @Author：静
 * @Package：com.jing.service
 * @Project：灵静
 * @name：CoyoteActivity
 * @Date：2024/10/15 下午12:34
 * @Filename：CoyoteActivity
 * @Version：1.0.0
 */
public class CoyoteActivity extends AppCompatActivity {

    private BluetoothDevice mDevice;

    private String coyoteName;

    private String coyoteAddress;

    private BluetoothGatt mBluetoothGatt;

    private BluetoothUtils bluetoothUtils;

    private Button coyoteTwo, coyoteThree;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coyote);

        coyoteTwo = findViewById(R.id.button2);
        coyoteThree = findViewById(R.id.button3);

        ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            bluetoothUtils.handleEnableBluetoothResult(result.getResultCode(), CoyoteActivity.this);
        });

        bluetoothUtils = new BluetoothUtils(this,intentActivityResultLauncher );

        bluetoothUtils.checkBluetoothAndRequestPermissions(this);

        coyoteTwo.setOnClickListener(v -> checkAndConnect("2.0"));
        coyoteThree.setOnClickListener(v -> checkAndConnect("3.0"));
    }

    @SuppressLint("MissingPermission")
    private void checkAndConnect(String version) {
        if (mBluetoothGatt != null && mBluetoothGatt.connect()) {
            if (mBluetoothGatt.getDevice().getName().equals(coyoteName) && mBluetoothGatt.getDevice().getAddress().equals(coyoteAddress) && version.equals("2.0")){
                Intent intent = new Intent(CoyoteActivity.this, CoyoteTwo.class);
                intent.putExtra("deviceName", coyoteName);
                intent.putExtra("deviceAddress", coyoteAddress);
                intent.putExtra("version", version);
                startActivity(intent);
            }

        } else {
            // 未连接，提醒用户
            ToastUtils.showToast(CoyoteActivity.this, "请先连接蓝牙设备");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBluetoothScan();
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothScan() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothLeScanner bluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null && device.getName().equals(CoyoteConstant.COYOTE_TWO_NAME)) {
                    mDevice = device;
                    coyoteName = device.getName();
                    coyoteAddress = device.getAddress();
                    ToastUtils.showToast(CoyoteActivity.this, "找到设备");
                    bluetoothLeScanner.stopScan(this);
                    connectToDevice(mDevice);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                ToastUtils.showToast(CoyoteActivity.this, "未找到设备");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice mDevice) {
       mBluetoothGatt= mDevice.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    runOnUiThread(() -> ToastUtils.showToast(CoyoteActivity.this, "连接成功"));
                    BluetoothGattManager.getInstance().setBluetoothGatt(gatt);
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    ToastUtils.showToast(CoyoteActivity.this, "连接断开");
                }
            }

           @Override
           public void onServicesDiscovered(BluetoothGatt gatt, int status) {
               if (status == BluetoothGatt.GATT_SUCCESS) {
                   displayAvailableServices(gatt);
               } else {
                   Log.w("CoyoteActivity", "服务发现失败: $status");
               }
           }
       });




    }

    private void displayAvailableServices(BluetoothGatt gatt) {
        Log.d("CoyoteActivity", "开始显示可用服务");
       gatt.getServices().forEach(service -> {
           Log.d("CoyoteActivity", "服务: " + service.getUuid().toString());
           service.getCharacteristics().forEach(characteristic -> {
               Log.d("CoyoteActivity", "特征: " + characteristic.getUuid().toString());
           });
       });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bluetoothUtils.handleEnableBluetoothResult(resultCode, this);
    }


}
