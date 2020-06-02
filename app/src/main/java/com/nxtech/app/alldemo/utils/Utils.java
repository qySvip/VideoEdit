package com.nxtech.app.alldemo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.nxtech.app.alldemo.bean.MediaBean;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Utils {

    public static final String POSTFIX = ".jpeg";
    private static ContentResolver mContentResolver;
    private static final String TRIM_PATH = "small_video";
    private static final String THUMB_PATH = "thumb";

    private static FileFilter ff = new FileFilter() {
        public boolean accept(File pathname) {
            return !pathname.isHidden();//过滤隐藏文件
        }
    };

    //获取本地视频文件(文件夹格式)
    public static void searchFile( String path, String Type, List<MediaBean> tempList){
        File file = new File(path);
        File[] subFile = file.listFiles(ff);
        if (subFile != null){
            for (File f:subFile) {
                String filename = f.getName();
                if (!f.isDirectory()) {
                    if (filename.contains(Type)){
                        FileInputStream fis = null;
                        long size = 0;
                        try{
                            fis = new FileInputStream(f);
                            size = fis.available();
                            String pathAndName = path+filename;
                            int dur = getLocalVideoDuration(pathAndName);
                            tempList.add(new MediaBean( filename, pathAndName, size, dur));
                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            if(fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }else{
                    searchFile(path+filename+"/",Type,tempList);
                }
            }
        }
    }

    //获取视频时长
    public static int getLocalVideoDuration(String videoPath) {
        int duration;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return duration;
    }

    //获取本地视频（数据库形式）
    public static void getAllVideo(Context context,List<MediaBean> tempList){
        mContentResolver = context.getContentResolver();
        Cursor c = null;
        try {
            c = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));// 路径
                if (!new File(path).exists()) {
                    continue;
                }
                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));// 视频的id
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)); // 视频名称
                String resolution = c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION)); //分辨率
                long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));// 大小
                long duration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));// 时长
                long date = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));//修改时间
                tempList.add(new MediaBean(name,path,size,(int)duration));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public static String saveImageToSDForEdit(Bitmap bmp, String dirPath, String fileName) {
        if (bmp == null) {
            return "";
        }
        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static void deleteFile(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; ++i) {
                    deleteFile(files[i]);
                }
            }
        }
        f.delete();
    }

    public static String getSaveEditThumbnailDir(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? context.getExternalCacheDir() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + File.separator + TRIM_PATH + File.separator + THUMB_PATH);
        if (folderDir == null) {
            folderDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "videoeditor" + File.separator + "picture");
        }
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        return folderDir.getAbsolutePath();
    }

}
