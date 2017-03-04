package com.sounder.dcqvideo;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 维持一个IjkMediaPlayer的单例
 * Createed by Sounder on 2017/3/1
 */
public class MediaPlayerManager {
    private static MediaPlayerManager sInstance;

    public static MediaPlayerManager getInstance() {
        if (sInstance == null) {
            sInstance = new MediaPlayerManager();
        }
        return sInstance;
    }

    private MediaPlayerManager() {
        mPlayer = new IjkMediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setLooping(true);
        //开启线程
        new Thread(mProgressRunable).start();
    }

    private IjkMediaPlayer mPlayer;
    private String mVideoUrl;
    private OnUpdateProgressListener mUpdateProgressListener;
    private boolean mPrepared;
    private Surface mSurface;
    /**
     * 是否播放过
     */
    private boolean mPlayed = false;

    /***/
    private Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 2) {
                if (mUpdateProgressListener != null && mPlayer.isPlaying()) {
                    mUpdateProgressListener.onUpdateProgress(getCurrentPosition());
                }
            }
        }
    };
    private boolean mRunning = true;
    /**
     * 更新进度的线程
     */
    private Runnable mProgressRunable = new Runnable() {
        @Override
        public void run() {
            while (mRunning) {
                if (mProgressHandler != null) {
                    mProgressHandler.sendEmptyMessage(2);
                }
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i("MediaPlayerManager","update thread is stopped");
        }
    };

    public void play(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            throw new NullPointerException("video uri can not be NULL");
        }
        mVideoUrl = videoUrl;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mVideoUrl);
            mPlayer.prepareAsync();
            mPlayed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public long getCurrentPosition() {
        try {
            return mPlayer.getCurrentPosition();
        } catch (Exception e) {
            return 0L;
        }
    }

    public void start() {
        mPlayer.start();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void setSurface(Surface surface) {
        this.mSurface = surface;
        mPlayer.setSurface(surface);
    }

    public void setOnCompletionListener(IjkMediaPlayer.OnCompletionListener listener) {
        mPlayer.setOnCompletionListener(listener);
    }

    public void setOnPreparedListener(IjkMediaPlayer.OnPreparedListener listener) {
        mPlayer.setOnPreparedListener(listener);
    }

    public void setOnErrorListener(IjkMediaPlayer.OnErrorListener listener) {
        mPlayer.setOnErrorListener(listener);
    }

    public void setOnBufferimgUpdateListener(IjkMediaPlayer.OnBufferingUpdateListener listener) {
        mPlayer.setOnBufferingUpdateListener(listener);
    }

    public void _release() {
        mRunning = false;
        mPlayed = false;
        mUpdateProgressListener = null;
        mProgressHandler = null;
        mPlayer.reset();
        mPlayer.release();
    }

    /**
     * 当不需要播放时释放资源
     * 如果不释放，更新线程仍会继续执行
     */
    public static void release() {
        Log.i("MediaPlayerManager","release All Media");
        sInstance._release();
        sInstance = null;
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void setPrepared(boolean prepared) {
        mPrepared = prepared;
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    public boolean isPlayed() {
        return mPlayed;
    }

    public void setPlayed(boolean played) {
        mPlayed = played;
    }
    public void setUpdateProgressListener(OnUpdateProgressListener listener){
        this.mUpdateProgressListener = listener;
    }

    public long getDuration() {
        return mPlayer.getDuration();
    }

    public void seekTo(int progress) {
        mPlayer.seekTo(progress);
    }
}
