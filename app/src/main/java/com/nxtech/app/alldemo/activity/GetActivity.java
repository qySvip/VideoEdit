package com.nxtech.app.alldemo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.nxtech.app.alldemo.Config;
import com.nxtech.app.alldemo.MyApplication;
import com.nxtech.app.alldemo.R;
import com.nxtech.app.alldemo.adapter.GridAdapter;
import com.nxtech.app.alldemo.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class GetActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    //获取视频类，只获取 mp4 格式文件

    private Button sinBtn;
    private Button mulBtn;
    private Button videotapeBtn;
    private GridView gridView;
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);

        initView();
    }

    private void initView(){
        gridView = (GridView)findViewById(R.id.get_grid);
        sinBtn = (Button)findViewById(R.id.sin_btn);
        mulBtn = (Button)findViewById(R.id.mul_btn);
        videotapeBtn = (Button)findViewById(R.id.videotape_btn);

        sinBtn.setOnClickListener(this);
        mulBtn.setOnClickListener(this);
        videotapeBtn.setOnClickListener(this);

        gridView.setOnItemClickListener(this);

        if (Config.sdPath != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Config.allList.size() <= 0 ){
                        Utils.getAllVideo(GetActivity.this,Config.allList);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridAdapter = new GridAdapter(GetActivity.this, Config.allList, gridView);
                            gridView.setAdapter(gridAdapter);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //点击视频开始播放
        if (Config.allList.size() > position){
            //调用视频播放器
            String path = Config.allList.get(position).getPath();
            File file = new File(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(),"com.nxtech.app.alldemo.fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "video/*");
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sin_btn:
                if (Config.videoPath.size() > 0){
                    //传输路径
                    Intent intent = new Intent(GetActivity.this,VideoActivity.class);
                    intent.putExtra("path",0);
                    intent.putExtra("duration",5);
                    startActivity(intent);
                }else {
                    Toast.makeText(GetActivity.this,"请选择视频",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mul_btn:
                if (Config.videoPath.size() > 1){
                    startActivity(new Intent(GetActivity.this,VideosActivity.class));
                }else {
                    Toast.makeText(GetActivity.this,"请至少选择两个视频",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.videotape_btn:
                Log.d("TAG", "onClick: 拍摄视频");
                Intent intent=new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                // 文件根据当前的毫秒数给自己命名
                String timeStamp = String.valueOf(System.currentTimeMillis());
                timeStamp = timeStamp.substring(7);
                String imageFileName = "V" + timeStamp + ".mp4";

                File mediaFile = new File(MyApplication.getSavePath()+imageFileName);
                Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),"com.nxtech.app.alldemo.fileprovider", mediaFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                startActivityForResult(intent,1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "拍摄视频完毕", Toast.LENGTH_SHORT).show();
    }
}
