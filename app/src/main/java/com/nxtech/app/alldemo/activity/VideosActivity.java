package com.nxtech.app.alldemo.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nxtech.app.alldemo.Config;
import com.nxtech.app.alldemo.MyApplication;
import com.nxtech.app.alldemo.R;
import com.nxtech.app.alldemo.bean.MediaBean;
import com.nxtech.app.alldemo.bean.VideoPartInfo;
import com.nxtech.app.alldemo.opengl.TransitionRender;
import com.nxtech.app.alldemo.ui.EditorMediaTrackView;
import com.nxtech.app.alldemo.ui.EditorTrackView;
import com.nxtech.app.alldemo.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class VideosActivity extends AppCompatActivity implements View.OnClickListener,MediaPlayer.OnCompletionListener {

    private Button mulBack;
    private Button mulOut;
    private VideoView mulVideo;
    private GLSurfaceView mulGlView;
    private TextView mulTime;
    private Button mulStatus;
    private EditorTrackView mulEditorView;
    private Button mulText;
    private Button mulAudio;
    private Button mulOpenGl;

    private String videoPath;
    private List<EpEditor> epVideos;
    private Disposable observable;
    private List<VideoPartInfo> mVideoPartInfoList = new ArrayList<>();
    private long mVideoDuration = 0;
    private long mTotalTime = 0;

    private static final int MSG_UPDATE_PROGRESS = 1;
    //表示当前是第几段视频
    private int videoNum = -1;
    private List<String> videoKey = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage( Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_UPDATE_PROGRESS:
                    updateProgress();
                    handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 40);
                    break;
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        initView();
        initVideo();
    }

    private void initView(){
        mulBack = (Button)findViewById(R.id.mul_edit_back);
        mulOut = (Button)findViewById(R.id.mul_edit_out);
        mulVideo = (VideoView)findViewById(R.id.mul_edit_video);
        mulGlView = (GLSurfaceView)findViewById(R.id.edit_opengl);
        mulTime = (TextView)findViewById(R.id.mul_edit_time);
        mulStatus = (Button)findViewById(R.id.mul_edit_status);
        mulEditorView = (EditorTrackView)findViewById(R.id.mul_editor_video);
        mulText = (Button)findViewById(R.id.mul_edit_text);
        mulAudio = (Button)findViewById(R.id.mul_edit_audio);
        mulOpenGl = (Button)findViewById(R.id.mul_opengl_btn);

        mulBack.setOnClickListener(this);
        mulOut.setOnClickListener(this);
        mulStatus.setOnClickListener(this);
        mulText.setOnClickListener(this);
        mulAudio.setOnClickListener(this);
        mulOpenGl.setOnClickListener(this);
    }

    private void initVideo(){
        for (String s: Config.videoPath.keySet()){
            videoKey.add(s);
            MediaBean media = Config.videoPath.get(s);
            mVideoDuration += media.getDuration();
        }
        mTotalTime = mVideoDuration;

        if (Config.videoPath.size() > 0){
            videoNum = 0;
            MediaBean media = Config.videoPath.get(videoKey.get(0));
            videoPath = media.getPath();
            mulVideo.setVideoPath(videoPath);
            mulVideo.start();
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);

            mulEditorView.setVideoPath(videoPath);
            buildVideoPartInfo();
            //用于后期剪辑的类
//            epVideo = new EpVideo(videoPath);
        }
        //滚动视频片段部分
        mulEditorView.setVideoPartInfoList(mVideoPartInfoList);
        mulEditorView.setOnTrackViewChangeListener(new EditorMediaTrackView.OnTrackViewChangeListener() {
            @Override
            public void onStartTrackingTouch() {
                if(mulVideo.isPlaying()){
                    mulVideo.pause();
                    handler.removeMessages(MSG_UPDATE_PROGRESS);
                }
            }

            @Override
            public void onScaleChanged(int scale) {
                int currentPos = (int) ((float)scale / mulEditorView.getMaxScale() * mulVideo.getDuration());
                mulVideo.seekTo(scaleToCurrentTime(scale));
                updateProgressText(currentPos);
            }

            private int scaleToCurrentTime(int scale) {
                for (int i = 0; i < mVideoPartInfoList.size(); i++)  {
                    VideoPartInfo partInfo = mVideoPartInfoList.get(i);
                    if (partInfo.inScaleRange(scale)) {
                        return (int) (((float) (scale - partInfo.getStartScale()) / partInfo.getLength() * partInfo.getDuration()) + partInfo.getStartTime());
                    }
                }
                return (int) mTotalTime;
            }
        });
    }

    private void buildVideoPartInfo() {
        VideoPartInfo videoPartInfo = new VideoPartInfo();
        videoPartInfo.setStartTime(0);
        videoPartInfo.setEndTime(mVideoDuration);
        videoPartInfo.setStartScale(0);
        videoPartInfo.setEndScale(mulEditorView.getMaxScale());
        mVideoPartInfoList.add(videoPartInfo);
    }

    @SuppressLint("SetTextI18n")
    private void updateProgressText(long calcCurrentTime) {
        mulTime.setText(TimeUtil.getTimeSmartFormat(calcCurrentTime) + "/" + TimeUtil.getTimeSmartFormat(mTotalTime));
    }

    @SuppressLint("SetTextI18n")
    private void updateProgress(){
        int currentPos = mulVideo.getCurrentPosition();
        updateProgressText(currentPos);
        int currentScale = (int) ((float)currentPos / mulVideo.getDuration() * mulEditorView.getMaxScale());
        mulEditorView.setCurrentScale(currentScale);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mul_edit_back:
                finish();
                break;
            case R.id.mul_edit_out:
//                if (MyApplication.getSavePath() != null){
//                    String timeStamp = String.valueOf(System.currentTimeMillis());
//                    timeStamp = timeStamp.substring(7);
//                    String imageFileName = "V" + timeStamp + ".mp4";
//                    final String outPath = MyApplication.getSavePath() + imageFileName;
//                    Log.d("TAG", "onClick: "+outPath);
//                    Log.d("TAGGG", "onClick: "+new Date().getTime());
//                    EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
//                        @Override
//                        public void onSuccess() {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d("TAG", "run: 成功");
//                                    Log.d("TAGGG", "onClick: "+new Date().getTime());
//                                    Toast.makeText(VideoActivity.this, "编辑成功", Toast.LENGTH_SHORT).show();
//                                    mulVideo.setVideoPath(outPath);
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onFailure() {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d("TAG", "run: 失败");
//                                    Toast.makeText(VideoActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onProgress(float v) {
//                            Log.d("TAG", "onProgress: "+(int) (v * 100));
//                        }
//                    });
//                }else{
//                    Toast.makeText(VideosActivity.this,"存储位置错误",Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.mul_edit_status:
                if (mulVideo.isPlaying()){
                    mulVideo.pause();
                    handler.removeMessages(MSG_UPDATE_PROGRESS);
                }else{
                    mulVideo.start();
                    handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                }
                break;
            case R.id.mul_edit_text:
                Toast.makeText(VideosActivity.this,"增加文字",Toast.LENGTH_SHORT).show();
//                epVideo.addText( 30, 30, 30, "red", MyApplication.getSavePath() + "msyh.ttf", "这是测试的文字");
                break;
            case R.id.mul_edit_audio:
                Toast.makeText(VideosActivity.this,"增加音频",Toast.LENGTH_SHORT).show();
                break;
            case R.id.mul_opengl_btn:
                Toast.makeText(VideosActivity.this,"显示转场",Toast.LENGTH_SHORT).show();
                //测试
                if (observable == null){
                    mulVideo.setVisibility(View.GONE);
                    TransitionRender mTransitionRender = new TransitionRender(this);
                    mulGlView.setEGLContextClientVersion(2);
                    mulGlView.setRenderer(mTransitionRender);
                    mulGlView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    mulGlView.setVisibility(View.VISIBLE);

                    observable = Observable.interval(40, TimeUnit.MILLISECONDS).subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    mulGlView.requestRender();
                                }
                            });
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("TAG", "onCompletion: 播放完毕 "+videoNum);
        handler.removeMessages(MSG_UPDATE_PROGRESS);
        if (videoNum < videoKey.size() - 1){
            videoNum++;
            mulVideo.setVideoPath(Config.videoPath.get(videoKey.get(videoNum)).getPath());
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mulVideo.isPlaying()){
            mulVideo.pause();
            handler.removeMessages(MSG_UPDATE_PROGRESS);
        }
        if (observable!=null && !observable.isDisposed()){
            if (observable != null){
                observable.dispose();
                observable = null;
            }
        }
    }
}
