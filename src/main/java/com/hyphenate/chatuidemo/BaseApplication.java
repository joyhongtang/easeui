package com.hyphenate.chatuidemo;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

public class BaseApplication extends MultiDexApplication {
    public boolean isForeground(){
        return false;
    }
    public static  Application application;
}
