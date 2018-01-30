package com.xugaoxiang.vlcdemo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xugaoxiang.vlcdemo.utils.ScreenUtils;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private LibVLC libvlc = null;
    private MediaPlayer mediaPlayer = null;
    private IVLCVout ivlcVout;
    private Media media;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        initPlayer();
    }

    private void initPlayer() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch");

        options.add("--audio-resampler");
        options.add("soxr");

        options.add("--avcodec-skiploopfilter");
        options.add("1");

        options.add("--avcodec-skip-frame");
        options.add("0");

        options.add("--avcodec-skip-idct");
        options.add("0");

        options.add("--udp-timeout");
        options.add("5");

        // deinterlace and deinterlace-mode, see https://wiki.videolan.org/Deinterlacing/#VLC_deinterlace_modes
        options.add("--deinterlace");
        options.add("1");

        options.add("--deinterlace-mode");
        options.add("bob");

        options.add("-vv");
        libvlc = new LibVLC(MainActivity.this, options);
        surfaceHolder = surfaceView.getHolder();
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

        media = new Media(libvlc, Uri.parse("http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8"));
        mediaPlayer.setMedia(media);

        ivlcVout = mediaPlayer.getVLCVout();
        ivlcVout.setVideoView(surfaceView);
        ivlcVout.attachViews();
        ivlcVout.addCallback(new IVLCVout.Callback() {
            @Override
            public void onSurfacesCreated(IVLCVout vlcVout) {
                int sw = getWindow().getDecorView().getWidth();
                int sh = getWindow().getDecorView().getHeight();

                if (sw * sh == 0) {
                    Log.e(TAG, "Invalid surface size");
                    return;
                }

                mediaPlayer.getVLCVout().setWindowSize(sw, sh);
                mediaPlayer.setAspectRatio("16:9");
                mediaPlayer.setScale(0);
            }

            @Override
            public void onSurfacesDestroyed(IVLCVout vlcVout) {

            }
        });

        mediaPlayer.play();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_STAR:
                try {
                    ScreenUtils.takeScreenshot(getApplicationContext());
                } catch (Exception e) {
                    new Exception("TakeScreenshot error.").printStackTrace();
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}
