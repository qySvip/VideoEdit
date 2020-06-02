package com.nxtech.app.alldemo.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nxtech.app.alldemo.Config;
import com.nxtech.app.alldemo.R;
import com.nxtech.app.alldemo.adapter.TrimVideoAdapter;
import com.nxtech.app.alldemo.bean.MediaBean;
import com.nxtech.app.alldemo.bean.VideoEditInfo;
import com.nxtech.app.alldemo.ui.RangeSeekBar;
import com.nxtech.app.alldemo.ui.VideoThumbSpacingItemDecoration;
import com.nxtech.app.alldemo.utils.ExtractFrameWorkThread;
import com.nxtech.app.alldemo.utils.TimeUtil;
import com.nxtech.app.alldemo.utils.UIUtils;
import com.nxtech.app.alldemo.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import VideoHandle.EpVideo;

@SuppressLint("SetTextI18n")
public class VideoActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    //控件
    private Button videoBack;
    private Button videoOut;
    private VideoView videoView;
    private Button videoStatus;
    private TextView videoTime;
    private RecyclerView editorRecycler;
    private LinearLayout seekLayout;
    //剪辑数据
    private String videoPath;
    private long videoDur;
    private EpVideo epVideo;
    private long startPos;
    private long scrollPos;

    private float averageMsPx;//每毫秒所占的px
    private int mMaxWidth; //可裁剪区域的最大宽度
    private long MAX_CUT_DURATION = 1000L;//视频最多剪切多长时间
    private static final int MAX_COUNT_RANGE = 10;//seekBar的区域内一共有多少张图片
    private static final int MARGIN = UIUtils.dp2Px(56); //左右两边间距
    private TrimVideoAdapter videoEditAdapter;
    private ExtractFrameWorkThread mExtractFrameWorkThread;
    private RangeSeekBar seekBar;

    private List<String> videoKey = new ArrayList<>();
    private final int SIN_MSG_UPDATE = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage( Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SIN_MSG_UPDATE:
                    updateProgress();
                    handler.sendEmptyMessageDelayed(SIN_MSG_UPDATE, 40);
                    break;
                case ExtractFrameWorkThread.MSG_SAVE_SUCCESS:
                    if (videoEditAdapter != null) {
                        VideoEditInfo info = (VideoEditInfo) msg.obj;
                        videoEditAdapter.addItemVideoInfo(info);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        initView();
        initVideo();
    }

    private void initView(){
        videoBack = (Button)findViewById(R.id.edit_back);
        videoOut = (Button)findViewById(R.id.edit_out);
        videoView = (VideoView) findViewById(R.id.edit_video);
        videoStatus = (Button)findViewById(R.id.edit_status);
        videoTime = (TextView)findViewById(R.id.edit_time);
        editorRecycler = (RecyclerView)findViewById(R.id.editor_recycler);
        seekLayout = (LinearLayout)findViewById(R.id.sin_seekBarLayout);

        videoView.setOnCompletionListener(this);
        videoBack.setOnClickListener(this);
        videoOut.setOnClickListener(this);
        videoStatus.setOnClickListener(this);
    }


    private void initVideo(){
        for (String s: Config.videoPath.keySet()){
            videoKey.add(s);
        }

        long duration = getIntent().getLongExtra("duration",1L);
//        MIN_CUT_DURATION = duration;
        MAX_CUT_DURATION *= duration;
        startPos = 0;

        int num = getIntent().getIntExtra("path",0);
        if (num < videoKey.size()){
            MediaBean mediaBean = Config.videoPath.get(videoKey.get(num));
            videoPath = mediaBean.getPath();
            videoDur = mediaBean.getDuration();
            videoTime.setText(TimeUtil.getTimeSmartFormat(0)+"/"+TimeUtil.getTimeSmartFormat(videoDur));

            mMaxWidth = UIUtils.getScreenWidth() - MARGIN * 2;
            editorRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            videoEditAdapter = new TrimVideoAdapter(this, mMaxWidth / 10);
            initEdit();
            editorRecycler.setAdapter(videoEditAdapter);
            editorRecycler.addOnScrollListener(scrollListener);
            videoView.setVideoPath(videoPath);
            videoView.start();
            handler.sendEmptyMessage(SIN_MSG_UPDATE);
            epVideo = new EpVideo(videoPath);
        }else{
            videoStatus.setClickable(false);
            videoOut.setClickable(false);
            Toast.makeText(this,"数据错误",Toast.LENGTH_SHORT).show();
        }
    }

    private void initEdit(){
        String OutPutFileDirPath = Utils.getSaveEditThumbnailDir(this);
        int extractW = mMaxWidth / MAX_COUNT_RANGE;
        int extractH = UIUtils.dp2Px(62);
        long startPosition = 0;
        long endPosition = videoDur;
        int thumbnailsCount;
        int rangeWidth;
        if (endPosition <= MAX_CUT_DURATION){
            thumbnailsCount = MAX_COUNT_RANGE;
            rangeWidth = mMaxWidth;
        }else{
            thumbnailsCount = (int) (endPosition * 1f /  1000f);
            rangeWidth = mMaxWidth / MAX_COUNT_RANGE * thumbnailsCount;
        }
        Log.d("TAG", "initEdit: "+rangeWidth);
        editorRecycler.addItemDecoration(new VideoThumbSpacingItemDecoration(MARGIN, thumbnailsCount));
        seekBar = new RangeSeekBar(this, 0L, MAX_CUT_DURATION);
        seekBar.setSelectedMinValue(0L);
        seekBar.setSelectedMaxValue(MAX_CUT_DURATION);
        seekBar.setMin_cut_time(MAX_CUT_DURATION);//设置最小裁剪时间
        seekBar.setNotifyWhileDragging(true);
        seekLayout.addView(seekBar);
        averageMsPx = videoDur * 1.0f / rangeWidth * 1.0f;

        mExtractFrameWorkThread = new ExtractFrameWorkThread(extractW, extractH, handler, videoPath, OutPutFileDirPath, startPosition, endPosition, thumbnailsCount);
        mExtractFrameWorkThread.start();
    }

    private void updateProgress(){
        int currentPos = videoView.getCurrentPosition();
        videoTime.setText(TimeUtil.getTimeSmartFormat(currentPos) + "/" + TimeUtil.getTimeSmartFormat(videoDur));
//        int currentScale = (int) ((float)currentPos / mulVideo.getDuration() * mulEditorView.getMaxScale());
//        mulEditorView.setCurrentScale(currentScale);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit_back:
                finish();
                break;
            case R.id.edit_out:
                Log.d("TAG", "onClick: 剪辑视频");
                Log.d("TAG", "onClick: "+startPos);
                videoView.pause();
//                epVideo.clip(startPos,endPos);
//                EpEditor.exec(epVideo, outputOption, new OnEditorListener() {
//                    @Override
//                    public void onSuccess() {
//
//                    }
//
//                    @Override
//                    public void onFailure() {
//
//                    }
//
//                    @Override
//                    public void onProgress(float progress) {
//
//                    }
//                });
                break;
            case R.id.edit_status:
                if(videoView.isPlaying()){
                    videoView.pause();
                }else{
                    videoView.start();
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("TAG", "onCompletion: 播放完毕");
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE){
                //停止滚动
                Log.d("TAG", "onScrollStateChanged: "+seekBar.getSelectedMinValue()+" "+scrollPos);
            }else {
                //正在滚动
                videoView.pause();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int scrollX = getScrollXDistance();
//            //达不到滑动的距离
//            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
//                isOverScaledTouchSlop = false;
//                return;
//            }
            if (scrollX == -MARGIN) {
                scrollPos = 0;
            } else {
                scrollPos = (long) (averageMsPx * (MARGIN + scrollX));
            }
            Log.d("TAG", "onScrolled: "+scrollPos);
        }
    };

    /**
     * 水平滑动了多少px
     * @return int px
     */
    private int getScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) editorRecycler.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }

    @Override
    protected void onDestroy() {
        if (mExtractFrameWorkThread != null) {
            mExtractFrameWorkThread.stopExtract();
        }
        //删除视频每一帧的预览图
        if (!TextUtils.isEmpty(Utils.getSaveEditThumbnailDir(this))) {
            Utils.deleteFile(new File(Utils.getSaveEditThumbnailDir(this)));
        }
        super.onDestroy();
    }
}
