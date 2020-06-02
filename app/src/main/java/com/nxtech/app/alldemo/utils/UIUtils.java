package com.nxtech.app.alldemo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.nxtech.app.alldemo.MyApplication;

public class UIUtils {

    public static Context getContext() {
        return MyApplication.sApplication;
    }

    public static Resources getResources() {
        return getContext().getResources();
    }

    public static int getScreenWidth() {
        DisplayMetrics dm = UIUtils.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int dp2Px(int dip) {
        //        dp<-->px
        //1. px/dp = density
        //2. px / (ppi/160) = dp;
        float density = UIUtils.getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + .5f);
        return px;
    }

}
