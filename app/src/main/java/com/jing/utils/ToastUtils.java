package com.jing.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @Author：静
 * @Package：com.jing.utils
 * @Project：灵静
 * @name：ToastUtils
 * @Date：2024/10/15 下午12:43
 * @Filename：ToastUtils
 * @Version：1.0.0
 */
public class ToastUtils {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
