package com.nxtech.app.alldemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxtech.app.alldemo.Config;
import com.nxtech.app.alldemo.R;
import com.nxtech.app.alldemo.utils.AsyncImageLoader;
import com.nxtech.app.alldemo.utils.TimeUtil;
import com.nxtech.app.alldemo.utils.Utils;
import com.nxtech.app.alldemo.bean.MediaBean;

import java.lang.ref.SoftReference;
import java.util.List;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private List<MediaBean> mLists;
    private AsyncImageLoader asyncImageLoader;
    private GridView mGridView;

    public GridAdapter(Context context, List<MediaBean> lists, GridView gridView){
        this.mContext = context;
        this.mLists = lists;
        this.mGridView = gridView;
        asyncImageLoader = new AsyncImageLoader();
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public Object getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_grid,null);
        }
        convertView.setTag(position);
        //设置界面数据
        final MediaBean mediaBean = mLists.get(position);

        TextView mainText = (TextView)convertView.findViewById(R.id.grid_dur);
        mainText.setText(TimeUtil.getTimeSmartFormat(mediaBean.getDuration()));
        ImageView mainImg = (ImageView)convertView.findViewById(R.id.grid_video);
        final ImageView mainOccl = (ImageView)convertView.findViewById(R.id.grid_img);
        mainOccl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.videoPath.containsKey(mediaBean.getPath())){
                    Config.videoPath.remove(mediaBean.getPath());
                    mainOccl.setBackgroundResource(R.drawable.img_bg1);
                }else{
                    Config.videoPath.put(mediaBean.getPath(),mediaBean);
                    mainOccl.setBackgroundResource(R.drawable.img_bg);
                }
            }
        });
        if (Config.imageCache.containsKey(mediaBean.getPath())) {
            SoftReference<Bitmap> softReference = Config.imageCache.get(mediaBean.getPath());
            final Bitmap bitmap = softReference.get();
            if (bitmap != null) {
                mainImg.setImageBitmap(bitmap);
                return convertView;
            }
        }
        mainImg.setImageResource(R.drawable.img_bg);
        //使用异步加载图片的类加载图片并实现回调
        asyncImageLoader.loadDrawable(position, mediaBean.getPath(),true, new AsyncImageLoader.ImageCallback() {
            @Override
            public void onImageLoad(Integer t, Bitmap bitmap) {
                View view = mGridView.findViewWithTag(t);
                if(view != null){
                    ImageView img_url = view.findViewById(R.id.grid_video);
                    img_url.setImageBitmap(bitmap);
                }
            }
            @Override
            public void onError(Integer t) {
                View view = mGridView.findViewWithTag(t);
                if(view != null){
                }
            }
        });
        return convertView;
    }
}
