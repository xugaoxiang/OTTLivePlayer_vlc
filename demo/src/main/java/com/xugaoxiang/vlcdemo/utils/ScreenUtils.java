package com.xugaoxiang.vlcdemo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ScreenUtils {

    private static ServiceConnection mScreenshotConnection;
    private static ServiceConnection conn;
    private static Handler mHandler = new Handler();
    private static Message msg;
    private static Messenger messenger;

    /**
     * @param mContext 建议使用getApplicationContext()，防止内存泄露
     */
    public static void takeScreenshot(final Context mContext) {
        synchronized (ScreenUtils.class) {
            if (mScreenshotConnection != null) {
                return;
            }
            //实际上是通过启动systemui的TakeScreenshotServiceservice来实现
            ComponentName cn = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);

            if (conn == null) {
                conn = new ServiceConnection() {
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        synchronized (this) {
                            if (mScreenshotConnection != this) {
                                return;
                            }
                            messenger = new Messenger(service);
                            msg = Message.obtain(null, 1);
                            final ServiceConnection myConn = this;
                            Handler h = new Handler(mHandler.getLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    synchronized (this) {
                                        if (mScreenshotConnection == myConn) {
                                            mContext.unbindService(mScreenshotConnection);
                                            mScreenshotConnection = null;
//                                            mHandler.removeCallbacks(mScreenshotTimeout);
                                            mHandler.removeCallbacksAndMessages(null);
                                        }
                                    }
                                }
                            };
                            msg.replyTo = new Messenger(h);
                            msg.arg1 = msg.arg2 = 0;
                            try {
                                messenger.send(msg);
                            } catch (RemoteException e) {
                            }
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
            }

            if (mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        synchronized (this) {
                            if (mScreenshotConnection != null) {
                                mContext.unbindService(mScreenshotConnection);
                                mScreenshotConnection = null;
                            }
                        }
                    }
                }, 1500);
            }

        }
    }

}

