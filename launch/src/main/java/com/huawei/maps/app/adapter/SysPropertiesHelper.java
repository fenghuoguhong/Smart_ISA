package com.huawei.maps.app.adapter;

import com.huawei.maps.app.utils.LogUtils;

import java.lang.reflect.Method;

/**
 * 测试数据获取能力
 */
public class SysPropertiesHelper {

    public static final String TAG = "SysPropertiesHelper";

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, ""));
            return value;
        } catch (Exception e) {
            LogUtils.getInstance().i(TAG, e.getMessage());
            return value;
        }
    }
}
