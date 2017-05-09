package com.xugaoxiang.ott.live.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.xugaoxiang.ott.live.R;
import com.xugaoxiang.ott.live.bean.LiveBean;
import com.xugaoxiang.ott.live.utils.NetWorkUtils;
import com.xugaoxiang.ott.live.utils.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceProgramActivity extends Activity{
    private static String BASE_URI = "http://202.158.177.67:8080";

    private static final String LIVE_URI = "/api/live";
    private static final String LIVE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyApp/LiveInfo";
    private static final String LOCAL_URL = Environment.getExternalStorageDirectory() + File.separator + "MyApp" + File.separator + "AppServer";
    private final static int CODE_NETWORK_ERROR = 0;

    private final static int CODE_NETWORK_SUCCESS = 1;

    private static int what = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CODE_NETWORK_SUCCESS:
                    setLiveData();
                    break;
                case CODE_NETWORK_ERROR:
                    Toast.makeText(ServiceProgramActivity.this , "网络连接异常，请检查网络!" , Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };

    private Gson gson;
    private LiveBean liveBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);
        if (!TextUtils.isEmpty(getLocalFileURL())){
            BASE_URI = getLocalFileURL();
        }
        initData();
    }

    private String getLocalFileURL() {
        File file = new File(LOCAL_URL);
        String str = "";
        if (file.exists()){
            try {
                FileInputStream stream = new FileInputStream(file);
                str = StreamUtils.stream2String(stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    private void setLiveData() {
        if (liveBean != null){
            VideoPlayerActivity.openLive(this , liveBean);
            finish();
            }
    }
    private void initData() {
        gson = new Gson();
        liveBean = new LiveBean();
        String str = getFileLiveInfo();
        if (!TextUtils.isEmpty(str)){
            liveBean = gson.fromJson(str , LiveBean.class);
        }
        if (!NetWorkUtils.getNetState(this)){
            handler.sendEmptyMessage(CODE_NETWORK_ERROR);
            return;
        }
        getServiceLiveList();
    }

    private String getFileLiveInfo() {
        File file = new File(LIVE_DIR , "liveList.xml");
        String str = "";
        if (file.exists()){
            try {
                FileInputStream stream = new FileInputStream(file);
                str = StreamUtils.stream2String(stream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    private void getServiceLiveList() {
        new Thread(){
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URI+LIVE_URI).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200){
                        InputStream is = conn.getInputStream();
                        String s = StreamUtils.stream2String(is);
                        if (!TextUtils.isEmpty(s)){
                            liveBean = gson.fromJson(s , LiveBean.class);
                            File dirFile = new File(LIVE_DIR);
                            if (!dirFile.exists()){
                                dirFile.mkdirs();
                            }
                            File file = new File(LIVE_DIR , "liveList.xml");
                            FileOutputStream stream = new FileOutputStream(file);
                            stream.write(s.getBytes());
                            what = CODE_NETWORK_SUCCESS;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    what = CODE_NETWORK_ERROR;
                    if (liveBean != null){
                        what = CODE_NETWORK_SUCCESS;
                    }
                }finally {
                    handler.sendEmptyMessage(what);
                }
            }
        }.start();
    }


}
