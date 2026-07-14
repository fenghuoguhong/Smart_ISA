package com.huawei.maps.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.GnssStatusCompat;
import androidx.core.location.LocationManagerCompat;

import com.huawei.maps.app.utils.OfflineDataUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 功能描述
 *
 * @author pingguo
 * @since 2024-04-18
 */
public class MainActivity extends Activity {
    private static final String TAG = "KikaISA_MainActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST_CODE = 2;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startForegroundService(new Intent(MainActivity.this, HwLocationAndIsaService.class));
        finish();
    }

    // 检查并请求权限的方法
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        } else {
            // 用户授予了所有权限，可以执行相关操作
            startForegroundService(new Intent(MainActivity.this, HwLocationAndIsaService.class));
            finish();
            //checkBatteryOptimization();
        }
    }

    private void initUI() {
        Button button = findViewById(R.id.button);
        button.setOnClickListener(view -> button.setText(OfflineDataUtils.getInstance().getIsaDataVersion()));
        Button reStart = findViewById(R.id.reStart);
        reStart.setOnClickListener(view -> {
            // 创建一个新的 Intent 启动主 Activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("RESTART", true); // 可选，用于区分是否是重启操作
            // 结束当前进程并启动新的 Intent
            startActivity(intent);
            Runtime.getRuntime().exit(0);
        });
        checkAndRequestPermissions();
        //startService(new Intent(MainActivity.this, HwLocationAndIsaService.class));
        //finish();

        GnssStatusCompat.Callback callback = new GnssStatusCompat.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatusCompat status) {
                super.onSatelliteStatusChanged(status);
                int satelliteCount = status.getSatelliteCount();
                boolean hasSignal = satelliteCount > 0;
                Button hasGps = findViewById(R.id.hasGps);
                // 根据卫星数量判断是否有GPS信号
                if (hasSignal) {
                    // GPS信号存在
                    hasGps.setVisibility(View.VISIBLE);
                    hasGps.setText("当前有gps信号，可见卫星数为：" + satelliteCount);
                } else {
                    // GPS信号不存在
                    //hasGps.setVisibility(View.GONE);
                    hasGps.setText("GPS信号不存在");
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManagerCompat.registerGnssStatusCallback((LocationManager) getSystemService(Context.LOCATION_SERVICE), callback, new Handler(Looper.getMainLooper()));
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Button hasNetwork = findViewById(R.id.hasNetwork);
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // 网络可用
                hasNetwork.setText("网络可用");
            }

            @Override
            public void onLost(Network network) {
                // 网络丢失
                hasNetwork.setText("网络丢失");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // 用户授予了所有权限，可以执行相关操作
                startForegroundService(new Intent(MainActivity.this, HwLocationAndIsaService.class));
                finish();
                //checkBatteryOptimization();
            } else {
                // 用户拒绝了部分或全部权限
                startForegroundService(new Intent(MainActivity.this, HwLocationAndIsaService.class));
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                //finish(); // 退出应用或返回主界面
            }
        }
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST_CODE);
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
