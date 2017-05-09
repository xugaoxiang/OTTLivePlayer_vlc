package com.xugaoxiang.ott.live.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by user on 2016/10/13.
 */
public class NetWorkUtils {

    public static boolean getNetState(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo isNetWorkInfo = manager.getActiveNetworkInfo();
        boolean isNetWork = false;
        if (isNetWorkInfo != null) {
            isNetWork = isNetWorkInfo.isAvailable();
        }
        if (!isNetWork) {
            return false;
        }else {
            return true;
        }
    }
}
