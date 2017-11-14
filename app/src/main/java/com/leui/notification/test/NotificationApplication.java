package com.leui.notification.test;

import android.app.Application;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by xiaoxin on 17-9-13.
 */

public class NotificationApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        //使用LeakCanary检测内存泄露
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/
        // Normal app init code...
    }
}
