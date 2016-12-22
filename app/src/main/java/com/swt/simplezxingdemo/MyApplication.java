package com.swt.simplezxingdemo;

import android.app.Application;

/**
 * Created by swt on 2016/12/22.
 */
public class MyApplication extends Application {
    private static MyApplication INSTANCE = null;

    public static int widthPixels = 720;// 屏幕宽度
    public static int heightPixels = 1280;// 屏幕高度


    public static MyApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

    }

}
