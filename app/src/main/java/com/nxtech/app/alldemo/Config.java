package com.nxtech.app.alldemo;

import android.graphics.Bitmap;

import com.nxtech.app.alldemo.bean.MediaBean;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Config {

    //短视频图片缓存
    public static HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<>();
    //本地所有视频的集合
    public static List<MediaBean> allList = new ArrayList<>();
    //存储选中视频的路径
    public static HashMap<String, MediaBean> videoPath = new HashMap<>();
    //手机根目录
    public static String sdPath = "";

}
