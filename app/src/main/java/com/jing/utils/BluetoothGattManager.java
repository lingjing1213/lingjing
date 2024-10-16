package com.jing.utils;

import android.bluetooth.BluetoothGatt;

/**
 * @Author：静
 * @Package：com.jing.utils
 * @Project：灵静
 * @name：BluetoothGattManager
 * @Date：2024/10/15 下午10:36
 * @Filename：BluetoothGattManager
 * @Version：1.0.0
 */
public class BluetoothGattManager {
    private static BluetoothGattManager instance = null;
    private BluetoothGatt bluetoothGatt;

    private BluetoothGattManager() {
        // 私有构造函数防止实例化
    }

    public static BluetoothGattManager getInstance() {
        if (instance == null) {
            instance = new BluetoothGattManager();
        }
        return instance;
    }

    public void setBluetoothGatt(BluetoothGatt gatt) {
        this.bluetoothGatt = gatt;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
