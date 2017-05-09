package com.xugaoxiang.ott.live.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xugaoxiang.ott.live.R;
import com.xugaoxiang.ott.live.adapter.ProgramAdapter;
import com.xugaoxiang.ott.live.bean.LiveBean;
import com.xugaoxiang.ott.live.utils.NetWorkUtils;
import com.xugaoxiang.ott.live.utils.PreUtils;
import com.xugaoxiang.ott.live.view.RotaProgressBar;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;


public class VideoPlayerActivity extends Activity implements IVLCVout.OnNewVideoLayoutListener {
    private static final String TAG = VideoPlayerActivity.class.getName();

    public static LiveBean liveBean;
    @Bind(R.id.lv_program)
    ListView lvProgram;
    @Bind(R.id.tv_program_number)
    TextView tvProgramNumber;
    @Bind(R.id.tv_program_name)
    TextView tvProgramName;
    @Bind(R.id.tv_system_time)
    TextView tvSystemTime;
    @Bind(R.id.rl_display)
    RelativeLayout rlDisplay;
    @Bind(R.id.video_surface_frame)
    FrameLayout mVideoSurfaceFrame;
    @Bind(R.id.surfaceview)
    SurfaceView surfaceview;
    @Bind(R.id.pb_loading)
    RotaProgressBar pbLoading;
    @Bind(R.id.ll_program_list)
    LinearLayout llProgramList;
    @Bind(R.id.tv_cache)
    TextView tvCache;

    private final static int CODE_SHOWLOADING = 1;

    private final static int CODE_STOP_SHOWLOADING = 2;

    private final static int CODE_GONE_PROGRAMINFO = 3;

    private final static int CODE_NET_STATE = 4;

    private final static int CODE_HIDE_BLACK = 5;

    private final static String PROGRAM_KEY = "lastProIndex";
    @Bind(R.id.tv_net_state)
    TextView tvNetState;
    @Bind(R.id.tv_black)
    TextView tvBlack;
    private SurfaceHolder surfaceHolder;
    private LibVLC libvlc = null;
    private MediaPlayer mediaPlayer = null;
    private IVLCVout ivlcVout;
    private Media media;

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_16_9;

    private int programIndex = 0;
    private int currentListItemID = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_SHOWLOADING:
                    showLoading();
                    handler.sendEmptyMessageDelayed(CODE_SHOWLOADING, 1000);
                    break;

                case CODE_STOP_SHOWLOADING:
                    hideLoading();
                    handler.removeMessages(CODE_SHOWLOADING);
                    break;

                case CODE_GONE_PROGRAMINFO:
                    rlDisplay.setVisibility(View.INVISIBLE);
                    break;

                case CODE_NET_STATE:
                    tvNetState.setVisibility(View.INVISIBLE);
                    break;

                case CODE_HIDE_BLACK:
                    tvBlack.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    private TranslateAnimation animIn;
    private TranslateAnimation exitAnim;
    private NetworkReceiver networkReceiver;
    private int volume;
//    private HomeWatcherReceiver mHomeKeyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        ButterKnife.bind(this);

        initData();
        initPlayer();

        showProgramInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerNetReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mediaPlayer.stop();
        mediaPlayer.getVLCVout().detachViews();
    }

    private void registerNetReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetworkReceiver();
        registerReceiver(networkReceiver, filter);
    }

//    private void registerHomeKeyReceiver() {
//        mHomeKeyReceiver = new HomeWatcherReceiver();
//        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//        registerReceiver(mHomeKeyReceiver, homeFilter);
//    }

    private void initData() {
        setAdapter();

        programIndex = PreUtils.getInt(VideoPlayerActivity.this, PROGRAM_KEY, 0);
        if ((programIndex >= liveBean.getData().size()) || (programIndex < 0)) {
            programIndex = 0;
        }
    }

    private void initPlayer() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");
        options.add("-vvv");
        libvlc = new LibVLC(VideoPlayerActivity.this, options);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        mediaPlayer = new MediaPlayer(libvlc);
        mediaPlayer.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Buffering:

                        if (event.getBuffering() >= 100.0f) {
                            hideLoading();
                            Log.i(TAG, "onEvent: buffer success...");
                            mediaPlayer.play();
                            handler.sendEmptyMessageDelayed(CODE_HIDE_BLACK, 500);
                            handler.sendEmptyMessageDelayed(CODE_GONE_PROGRAMINFO, 5000);
                        } else {
                            showLoading();
                            tvCache.setText("缓冲: " + Math.floor(event.getBuffering()) + "%");
                        }

                        break;

                    case MediaPlayer.Event.Playing:
                        Log.i(TAG, "onEvent: playing...");
                        break;

                    case MediaPlayer.Event.EncounteredError:
                        Log.i(TAG, "onEvent: error...");
                        break;
                }
            }
        });

        media = new Media(libvlc, Uri.parse(liveBean.getData().get(programIndex).getUrl()));
        mediaPlayer.setMedia(media);

        ivlcVout = mediaPlayer.getVLCVout();
        ivlcVout.setVideoView(surfaceview);
        ivlcVout.attachViews(this);
//        ivlcVout.attachViews(new IVLCVout.OnNewVideoLayoutListener() {
//            @Override
//            public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
//                Log.i(TAG, "onNewVideoLayout: ...");
//                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//                Display display = windowManager.getDefaultDisplay();
//                Point point = new Point();
//                display.getSize(point);
//
//                int videoWidth = width;
//                int videoHight = height;
//
//                ViewGroup.LayoutParams layoutParams = surfaceview.getLayoutParams();
//                layoutParams.width = point.x;
//                layoutParams.height = (int) Math.ceil((float) videoHight * (float) point.x / (float) videoWidth);
//                surfaceview.setLayoutParams(layoutParams);
//            }
//        });
        ivlcVout.addCallback(new IVLCVout.Callback() {
            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {

            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {

            }
        });

        mediaPlayer.play();

    }

    private void setAdapter() {
        if (liveBean != null) {
            ProgramAdapter programAdapter = new ProgramAdapter(VideoPlayerActivity.this);
            lvProgram.setAdapter(programAdapter);
        } else {
            Toast.makeText(this, "打开播放列表失败，请检查网络！", Toast.LENGTH_SHORT).show();
        }

        lvProgram.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (programIndex != position) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }

                    programIndex = position;
                    ivlcVout.detachViews();
                    play(position);
                }

            }
        });

        lvProgram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentListItemID = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static void openLive(Context context, LiveBean liveBean) {
        VideoPlayerActivity.liveBean = liveBean;
        context.startActivity(new Intent(context, VideoPlayerActivity.class));
    }

    private void play(int position) {
        Uri parse = Uri.parse(liveBean.getData().get(position).getUrl());
        media = new Media(libvlc, parse);
        mediaPlayer.setMedia(media);
        ivlcVout.setVideoView(surfaceview);
        ivlcVout.attachViews();
        mediaPlayer.play();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (event.getRepeatCount() != 0) {
                    Log.i(TAG, "onKeyDown: repeat count != 0 ...");
                    break;
                }
                togglePlaylist();
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (currentListItemID == 0) {
                    lvProgram.setSelection(liveBean.getData().size() - 1);
                }

                if (llProgramList.getVisibility() == View.VISIBLE) {
                    return false;
                }
                previous();
                showProgramInfo();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (currentListItemID == liveBean.getData().size() - 1) {
                    lvProgram.setSelection(0);
                }

                if (llProgramList.getVisibility() == View.VISIBLE) {
                    return false;
                }
                next();
                showProgramInfo();
                break;

            case KeyEvent.KEYCODE_MENU:
                showProgramInfo();
                handler.sendEmptyMessageDelayed(CODE_GONE_PROGRAMINFO, 4000);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyUp: ...");

        switch (keyCode) {

            case KeyEvent.KEYCODE_DPAD_UP:
                if (llProgramList.getVisibility() == View.VISIBLE) {
                    return false;
                }
                cutProgram();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.i(TAG, "onKeyUp: down --> up...");
                if (llProgramList.getVisibility() == View.VISIBLE) {
                    return false;
                }
                cutProgram();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void showProgramInfo() {
        rlDisplay.setVisibility(View.VISIBLE);
        tvProgramName.setText(liveBean.getData().get(programIndex).getName());
        tvProgramNumber.setText(liveBean.getData().get(programIndex).getNum());
        tvSystemTime.setText(getDtate());
    }

    @Override
    public void onBackPressed() {
        if (llProgramList.getVisibility() == View.VISIBLE) {
            exitProgramList();
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            finish();
        }
    }

    private void exitProgramList() {
        if (exitAnim == null) {
            exitAnim = new TranslateAnimation(0f, -llProgramList.getWidth(), 0f, 0f);
            exitAnim.setDuration(500);
        }
        exitAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llProgramList.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llProgramList.startAnimation(exitAnim);
    }

    public void togglePlaylist() {
        if (llProgramList.getVisibility() == View.VISIBLE) {
            if (currentListItemID == programIndex) {
                return;
            }
        }

        if (animIn == null) {
            animIn = new TranslateAnimation(-llProgramList.getWidth(), 0f, 0f, 0f);
            animIn.setDuration(500);
        }
        animIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                llProgramList.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        llProgramList.startAnimation(animIn);
        lvProgram.setSelection(programIndex);
    }

    public String getDtate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd    HH:mm");
        return sDateFormat.format(new Date());
    }

    private void showLoading() {
        if (pbLoading.getVisibility() == View.INVISIBLE) {
            pbLoading.setVisibility(View.VISIBLE);
            tvCache.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (pbLoading.getVisibility() == View.VISIBLE) {
            pbLoading.setVisibility(View.INVISIBLE);
            tvCache.setVisibility(View.GONE);
        }
    }

    private void cutProgram() {
        ivlcVout.detachViews();
        play(programIndex);
    }

    private void next() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

        tvBlack.setVisibility(View.VISIBLE);
        programIndex++;
        if (programIndex >= liveBean.getData().size()) {
            programIndex = 0;
        }

        handler.removeMessages(CODE_GONE_PROGRAMINFO);
    }

    private void previous() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

        tvBlack.setVisibility(View.VISIBLE);
        programIndex--;
        if (programIndex < 0) {
            programIndex = liveBean.getData().size() - 1;
        }

        handler.removeMessages(CODE_GONE_PROGRAMINFO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        volume = PreUtils.getInt(VideoPlayerActivity.this, "Volume", 100);
        if (volume < 0) {
            volume = 0;
        } else if (volume > 100) {
            volume = 100;
        }

        mediaPlayer.setVolume(volume);
        try {
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: ...");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ....");
        super.onDestroy();

        PreUtils.setInt(VideoPlayerActivity.this, PROGRAM_KEY, programIndex);

        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            ivlcVout.detachViews();
            libvlc.release();
        }

        finish();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mediaPlayer.setScale(scale);
                    mediaPlayer.setAspectRatio(null);
                } else {
                    mediaPlayer.setScale(0);
                    mediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mediaPlayer.setAspectRatio("16:9");
                mediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mediaPlayer.setAspectRatio("4:3");
                mediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mediaPlayer.setAspectRatio(null);
                mediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceview.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceview.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mediaPlayer.setAspectRatio(null);
            mediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceview.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        surfaceview.invalidate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    public void showNetworkInfo(String text) {
        if (tvNetState.getVisibility() == View.INVISIBLE) {
            tvNetState.setVisibility(View.VISIBLE);
        }

        tvNetState.setText(text);
    }

    public void hideNetworkInfo() {
        if (tvNetState.getVisibility() == View.VISIBLE) {
            tvNetState.setVisibility(View.INVISIBLE);
        }

        tvNetState.setText("");
    }

    class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!NetWorkUtils.getNetState(context)) {
                showNetworkInfo("网络已断开!");
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            } else {
                hideNetworkInfo();
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.play();
                }
            }
        }
    }

//    class HomeWatcherReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            PreUtils.setInt(context, "Volume", mediaPlayer.getVolume());
//            mediaPlayer.setVolume(0);
//        }
//    }
}
