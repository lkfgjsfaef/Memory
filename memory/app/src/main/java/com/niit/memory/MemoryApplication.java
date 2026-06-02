package com.niit.memory;

import android.app.Application;

public class MemoryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(getPackageName());
    }
}
