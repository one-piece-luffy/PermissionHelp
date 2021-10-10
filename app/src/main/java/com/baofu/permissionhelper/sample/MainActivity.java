package com.baofu.permissionhelper.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baofu.permissionhelper.PermissionUtil;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView tvDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);

    }


    public void requestPermission(View view) {
        PermissionUtil.getInstance().request(this,"需要读取手机信息以及文件读写权限",
                PermissionUtil.asArray(Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                new PermissionUtil.RequestPermissionListener() {
                    @Override
                    public void callback(boolean granted, boolean isAlwaysDenied) {
                        if (granted) {
                            getFilePath();
                            TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (phone != null) {
                                try {
                                    @SuppressLint({"MissingPermission", "HardwareIds"})
                                    String deviceId = phone.getDeviceId();
                                    tvDeviceInfo.setText(deviceId);
                                    Toast.makeText(MainActivity.this, "权限申请成功", Toast.LENGTH_SHORT).show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "deviceId is null", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (isAlwaysDenied) {
                                Toast.makeText(MainActivity.this, "权限申请失败，用户已拒绝且不提示，请自行到设置中修改", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "权限申请失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void getFilePath() {
        File externalFilesDir = getExternalFilesDir(null);
        File externalCacheDir = getExternalCacheDir();

        String filePath = getFilesDir().getAbsolutePath();
        String cachePath = getCacheDir().getAbsolutePath();
        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String externalFilePath = externalFilesDir == null ? "" : externalFilesDir.getAbsolutePath();
        String externalCachePath = externalCacheDir == null ? "" : externalCacheDir.getAbsolutePath();

        Log.d("filePath = ", filePath);
        Log.d("cachePath = ", cachePath);
        Log.d("externalPath = ", externalPath);
        Log.d("externalFilePath = ", externalFilePath);
        Log.d("externalCachePath = ", externalCachePath);

        openCamera(externalCachePath);
    }

    private void openCamera(String dir) {
        File file = new File(dir, System.currentTimeMillis() + ".jpg");
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, PermissionUtil.getUri(/*context*/this, intent, file, getPackageName()+".fileprovider"));
        startActivity(intent);
    }



    private void install(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = PermissionUtil.getUri(this, apk, getPackageName()+".fileprovider");
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public void openSetting(View v) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PermissionUtil.getInstance().destroy();
    }

}
