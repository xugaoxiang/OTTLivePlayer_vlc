package com.xugaoxiang.ott.live;

import android.app.Application;
import android.os.Environment;

import java.io.PrintWriter;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // SDKInitializer.initialize(getApplicationContext());
//        Thread.setDefaultUncaughtExceptionHandler(new MyHandler());
    }

    class MyHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            ex.printStackTrace();
            try {
                PrintWriter err = new PrintWriter(Environment.getExternalStorageDirectory() + "MyApp/Log/live.log");
                ex.printStackTrace(err);
                err.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

}
