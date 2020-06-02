package com.nxtech.app.alldemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Handler;

import com.nxtech.app.alldemo.Config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class AsyncImageLoader {
    //异步获取网络图片并加载

    Handler handler = new Handler();
    HashMap<Integer,Boolean> imageError;

    public AsyncImageLoader() {
        imageError = new HashMap<>();
    }

    // 回调函数
    public interface ImageCallback {
        void onImageLoad(Integer t, Bitmap bitmap);
        void onError(Integer t);
    }

    //加载视频
    public Bitmap loadDrawable(final Integer pos, final String imageUrl, final Boolean bool, final ImageCallback imageCallback) {
        new Thread() {
            @Override
            public void run() {
                LoadImg(pos, imageUrl,bool, imageCallback);
            }
        }.start();
        return null;
    }

    public void LoadImg(final Integer pos, final String imageUrl, final Boolean bool, final ImageCallback imageCallback) {
        if (imageError.containsKey(pos)){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageCallback.onError(pos);
                }
            });
            return;
        }
        final Bitmap bitmap;
        if (bool){
            bitmap = getVideoThumb(imageUrl);
        }else{
            bitmap = httpBitmap(imageUrl);
        }
        if (bitmap != null) {
            Config.imageCache.put(imageUrl, new SoftReference<>(bitmap));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageCallback.onImageLoad(pos, bitmap);
                }
            });
        }else{
            imageError.put(pos,false);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageCallback.onError(pos);
                }
            });
        }
    }
    //获取本地视频的第一帧
    public static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        Bitmap bitmap =  media.getFrameAtTime();
        return bitmap;
    }
    //获取网络视频的第一帧
    public static Bitmap httpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
