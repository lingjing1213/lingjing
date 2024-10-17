灵静app

已经使用GATT成功与郊狼通信！
服务: 00001800-0000-1000-8000-00805f9b34fb
特征: 00002a00-0000-1000-8000-00805f9b34fb
特征: 00002a01-0000-1000-8000-00805f9b34fb
特征: 00002a04-0000-1000-8000-00805f9b34fb
特征: 00002aa6-0000-1000-8000-00805f9b34fb
服务: 00001801-0000-1000-8000-00805f9b34fb

电量服务: 955a180a-0fe2-f5aa-a094-84b8d4f3e8ad
电量特征: 955a1500-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1501-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1502-0fe2-f5aa-a094-84b8d4f3e8ad


AB通道服务: 955a180b-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1503-0fe2-f5aa-a094-84b8d4f3e8ad
强度特征: 955a1504-0fe2-f5aa-a094-84b8d4f3e8ad
波形A特征: 955a1505-0fe2-f5aa-a094-84b8d4f3e8ad
波形B特征: 955a1506-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1507-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1508-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a1509-0fe2-f5aa-a094-84b8d4f3e8ad


服务: 955a180c-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a150a-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a150b-0fe2-f5aa-a094-84b8d4f3e8ad
特征: 955a150c-0fe2-f5aa-a094-84b8d4f3e8ad
服务: 8e400001-f315-4f60-9fb8-838830daea50
特征: 8e400001-f315-4f60-9fb8-838830daea50


if (isRunning) {
// 暂停脉冲发送
isRunning = false;
pulseHandler.removeCallbacks(pulseRunnable);
Log.d("CoyoteTwo", "脉冲发送已暂停");
ToastUtils.showToast(this, "已暂停");
} else {
// 开始脉冲发送
isRunning = true;
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

            elapsedTime = 0; // 重置已过时间
            pulseHandler.post(pulseRunnable); // 开始脉冲发送
            Log.d("CoyoteTwo", "脉冲发送已开始");
            ToastUtils.showToast(this, "已开始");
        }


    @SuppressLint("MissingPermission")
    private void sendPulseParameters(int X, int Y, int Z) {
        if (bluetoothGatt == null) return;

        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_PWM_AB2_UUID));
        if (service != null) {
            BluetoothGattCharacteristic pwmA34 = service.getCharacteristic(UUID.fromString("955a1506-0fe2-f5aa-a094-84b8d4f3e8ad"));

            // 构造数据
            byte[] data = new byte[3];
            data[0] = (byte) ((X & 0x1F) | ((Y & 0x3FF) << 5) | ((Z & 0x1F) << 15)); // 按位打包

            // 写入特性
            pwmA34.setValue(data);
            bluetoothGatt.writeCharacteristic(pwmA34);
        }
    }


