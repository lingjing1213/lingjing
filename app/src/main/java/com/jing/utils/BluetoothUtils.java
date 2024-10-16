package com.jing.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.jing.HomeActivity;

/**
 * @Author：静
 * @Package：com.jing.utils
 * @Project：灵静
 * @name：BluetoothUtils
 * @Date：2024/10/15 下午12:38
 * @Filename：BluetoothUtils
 * @Version：1.0.0
 */
public class BluetoothUtils {

    public static final int REQUEST_ENABLE_BLUETOOTH = 1001;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1002;

    private static final int REQUEST_LOCATION_PERMISSION = 1003;

    private final BluetoothAdapter bluetoothAdapter;

    private final Context context;

    private final ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;

    public BluetoothUtils(Context context, ActivityResultLauncher<Intent> bluetoothActivityResultLauncher) {
        this.context = context;
        this.bluetoothActivityResultLauncher = bluetoothActivityResultLauncher;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }

    public void checkBluetoothAndRequestPermissions(Activity activity){
        if (bluetoothAdapter==null){
            ToastUtils.showToast(context,"蓝牙不可用");
            return;
        }

        if (bluetoothAdapter.isEnabled()){
            requestPermissions(activity);
        }else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivityResultLauncher.launch(enableBtIntent);
        }

    }

    /**
     * 区分安卓12以上 和以下
     * @param activity
     */
    private void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSIONS
                );
            } else {
                // 如果已经授予权限，结束流程
                onPermissionsGranted();
            }
        }else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);

            }else {
                onPermissionsGranted();
            }
        }
    }

    public void handleRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            // 确保 grantResults 不为空并包含有效的权限结果
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    // 用户授予了所有蓝牙相关权限，继续请求位置权限
                    requestPermissions(activity);
                } else {
                    // 用户拒绝了其中一个或多个蓝牙权限，返回主界面
                    returnToMainScreen(activity);
                }
            } else {
                // 没有有效的权限结果，返回主界面
                returnToMainScreen(activity);
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了位置权限
                onPermissionsGranted();

            } else {
                // 用户拒绝位置权限，返回主界面
                returnToMainScreen(activity);
            }
        }
    }


    public  void handleEnableBluetoothResult(int resultCode, Activity activity) {
        if (resultCode == Activity.RESULT_OK) {
            // 用户允许启用蓝牙，继续请求权限
            requestPermissions(activity);
        } else {
            // 用户拒绝启用蓝牙，返回主界面
            ToastUtils.showToast(context,"蓝牙权限是必须的");
            returnToMainScreen(activity);
        }
    }


    private void onPermissionsGranted() {
        ToastUtils.showToast(context,"权限已授予,请打开蓝牙设备");
    }

    private void returnToMainScreen(Activity activity) {
        Intent intent = new Intent(activity, HomeActivity.class); // 确保 HomeActivity 是正确的类名
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish(); // 关闭当前Activity
        ToastUtils.showToast(context,"权限未授予");
    }
}
