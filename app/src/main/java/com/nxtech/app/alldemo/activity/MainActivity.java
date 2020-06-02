package com.nxtech.app.alldemo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nxtech.app.alldemo.Config;
import com.nxtech.app.alldemo.R;
import com.nxtech.app.alldemo.utils.Utils;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mainBtn;

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[0]);
            int l = ContextCompat.checkSelfPermission(getApplicationContext(), permissions[1]);
            if (i != PackageManager.PERMISSION_GRANTED || l != PackageManager.PERMISSION_GRANTED) {
                startRequestPermission();
            }
        }
    }

    private void initView(){
        mainBtn = (Button)findViewById(R.id.main_btn);

        mainBtn.setOnClickListener(this);

        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            //获得SD卡根目录路径
            File sdDir =Environment.getExternalStorageDirectory();
            String sdpath = sdDir.getAbsolutePath();
            Config.sdPath = sdpath;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_btn:
                startActivity(new Intent(this, GetActivity.class));
                break;
        }
    }

    private void startRequestPermission(){
        ActivityCompat.requestPermissions(this, permissions, 321);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "onRequestPermissionsResult: 手动设置");
                } else {
                    //获取权限成功提示，可以不要
                    Toast.makeText(this, "获取权限成功", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
