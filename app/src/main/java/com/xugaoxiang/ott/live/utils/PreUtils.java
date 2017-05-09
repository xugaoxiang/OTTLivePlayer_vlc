package com.xugaoxiang.ott.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 2016/10/11.
 */
public class PreUtils {
    private final static String LIVE_INFO = "live";

    public static void setInt(Context context , String key , int value){
        SharedPreferences sp = context.getSharedPreferences(LIVE_INFO , Context.MODE_PRIVATE);
        sp.edit().putInt(key , value).commit();
    }

    public static int getInt(Context context , String key , int defValue){
        SharedPreferences sp = context.getSharedPreferences(LIVE_INFO, Context.MODE_PRIVATE);
        return sp.getInt(key , defValue);
    }
}
