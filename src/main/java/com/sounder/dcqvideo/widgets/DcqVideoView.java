package com.sounder.dcqvideo.widgets;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sounder.dcqvideo.FullActivity;
import com.sounder.dcqvideo.MediaPlayerManager;
import com.sounder.dcqvideo.OnStartPlayVideoListener;
import com.sounder.dcqvideo.OnUpdateProgressListener;
import com.sounder.dcqvideo.R;
import com.sounder.dcqvideo.Utils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Createed by Sounder on 2017/3/1
 */
public class DcqVideoView extends FrameLayout implements TextureView.SurfaceTextureListener,
        View.OnClickListener,OnUpdateProgressListener,
        IjkMediaPlayer.OnPreparedListener,IjkMediaPlayer.OnCompletionListener,
        IjkMediaPlayer.OnErrorListener,IjkMediaPlayer.OnBufferingUpdateListener{
    public static final String TAG = "DcqVideoView";
    private ImageButton btnStart;
    private ImageButton btnFullScreen;
    private ProgressBar progressBarLoading;
    /**视频占位图，声明为public,根据各自图片加载方式加载*/
    public ImageView imgThumb;
    private RelativeLayout rlControl;
    private TextView tvTimeNow;
    private TextView tvTimeAll;
    private AppCompatSeekBar seekBar;
    private RelativeLayout rlTitle;
    private ImageButton btnBack;
    private TextView tvTitle;

    private View mControlView;

    private TextureView mTextureView;
    private String mVideoUrl;
    private String mTitle;

    private boolean mControlsShow;

    private Surface mSurface;
    /**仅当在全屏页面才会设置该值为true*/
    private boolean mFullScreenMode = false;
    /**当拖动进度条时是不能设置进度条的值的*/
    private boolean mCanSeekBar = true;

    private OnStartPlayVideoListener mStartPlayVideoListener;

    public DcqVideoView(Context context) {
        super(context);
        init(context);
    }
    public DcqVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public DcqVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 重新设置监听器，
     * 否则在退出全屏返回上个页面时MediaPlayerManager里的各个监听是没有的，当然也不会存在进度条的更新的
     *
     */
    public void resume(){
        initMediaPlayerListeners();
    }
    public void init(Context context){
        makeTextureView();
        makeControlView();
        initControlsView();
        initMediaPlayerListeners();
        initOthers();
        mControlsShow = true;
    }
    public void setUp(String url,String title){
        this.mVideoUrl = url;
        this.mTitle = title;
        tvTitle.setText(title);
    }

    /**
     * 实例化一个TextureView对象，使得IjkMediaplayer有画面可渲染
     */
    private void makeTextureView(){
        TextureView textureView = new TextureView(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        addView(textureView);
        textureView.setLayoutParams(params);
        textureView.setSurfaceTextureListener(this);
    }

    /**
     * 一系列控制的view
     */
    private void makeControlView(){
        mControlView = LayoutInflater.from(getContext()).inflate(R.layout.video_control,this);
        mControlView.setOnClickListener(this);

        FrameLayout.LayoutParams params = (LayoutParams) mControlView.getLayoutParams();
        if(params == null){
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }else{
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        mControlView.setLayoutParams(params);
    }
    private void initControlsView(){
        btnStart = (ImageButton) mControlView.findViewById(R.id.btnPlay);
        btnStart.setOnClickListener(this);
        btnFullScreen = (ImageButton) mControlView.findViewById(R.id.btn_full_screen);
        btnFullScreen.setOnClickListener(this);
        imgThumb = (ImageView) mControlView.findViewById(R.id.imgThubm);
        rlControl = (RelativeLayout) mControlView.findViewById(R.id.rl_control);
        tvTimeAll = (TextView) mControlView.findViewById(R.id.tv_time_all);
        tvTimeNow = (TextView) mControlView.findViewById(R.id.tv_time_now);
        seekBar = (AppCompatSeekBar) findViewById(R.id.seekBar);
        progressBarLoading = (ProgressBar) mControlView.findViewById(R.id.loading);

        rlTitle = (RelativeLayout) mControlView.findViewById(R.id.rl_title);
        btnBack = (ImageButton) mControlView.findViewById(R.id.btn_back);
        tvTitle = (TextView) mControlView.findViewById(R.id.tv_title);
        btnBack.setOnClickListener(this);
        rlTitle.setVisibility(GONE);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mCanSeekBar = false;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(MediaPlayerManager.getInstance().isPrepared()){
                    MediaPlayerManager.getInstance().seekTo(seekBar.getProgress());
                }
                mCanSeekBar = true;
            }
        });
        progressBarLoading.setVisibility(GONE);

    }
    private void initMediaPlayerListeners(){
        MediaPlayerManager.getInstance().setOnBufferimgUpdateListener(this);
        MediaPlayerManager.getInstance().setOnCompletionListener(this);
        MediaPlayerManager.getInstance().setOnErrorListener(this);
        MediaPlayerManager.getInstance().setOnPreparedListener(this);
        MediaPlayerManager.getInstance().setUpdateProgressListener(this);
        if(mSurface != null){
            MediaPlayerManager.getInstance().setSurface(mSurface);
        }
    }

    /**
     * 根据当前播放器的状态设置UI布局
     * 特别是播放状态进入全屏页面时
     */
    private void initOthers(){
        /*
        默认情况下只显示开始按钮和占位图
         */
        if(MediaPlayerManager.getInstance().isPlayed()){
            imgThumb.setVisibility(GONE);
        }else{//还没有点击过播放按钮，就是默认情况
            imgThumb.setVisibility(VISIBLE);
            rlControl.setVisibility(GONE);
            btnStart.setVisibility(VISIBLE);
            progressBarLoading.setVisibility(GONE);
        }
        if(MediaPlayerManager.getInstance().isPrepared()){
            setDuration();
        }
        if(MediaPlayerManager.getInstance().isPlaying()){
            progressBarLoading.setVisibility(GONE);
            btnStart.setImageResource(R.drawable.ic_video_pause);
            showControls(true);
        }
    }
    @Override
    public void onClick(View v) {
        if(v == btnStart){
            onBtnStartClick();
        }else if(v == btnFullScreen){
            onBtnFullScreenClick();
        }else if(v == mControlView){
            onControlViewClick();
        }else if(v == btnBack){
            onBtnFullScreenClick();
        }
    }
    private void onBtnStartClick(){
        imgThumb.setVisibility(GONE);
        if(!MediaPlayerManager.getInstance().isPlayed()){//没有播放过就开始播放
            Log.i(TAG,"not played");
            progressBarLoading.setVisibility(VISIBLE);
            imgThumb.setVisibility(GONE);
            showControls(false);
            MediaPlayerManager.getInstance().play(mVideoUrl);
            if(mStartPlayVideoListener != null){
                mStartPlayVideoListener.onStartPlay();
            }
        }else {
            if (!MediaPlayerManager.getInstance().isPrepared()) {
                Log.i(TAG,"not Prepared");
                progressBarLoading.setVisibility(VISIBLE);
                showControls(false);
                MediaPlayerManager.getInstance().play(mVideoUrl);
            }
            if (MediaPlayerManager.getInstance().isPlaying()) {
                Log.i(TAG," playing");
                MediaPlayerManager.getInstance().pause();
                btnStart.setImageResource(R.drawable.ic_video_play);
            } else {
                Log.i(TAG," pause");
                MediaPlayerManager.getInstance().start();
                btnStart.setImageResource(R.drawable.ic_video_pause);
            }
        }
    }

    /**
     * 设置全屏标记
     */
    public void setFullScreen(){
        mFullScreenMode = true;
        //修改全屏图标
        btnFullScreen.setImageResource(R.drawable.ic_video_screen_normal);
        showControls(true);
    }
    private void onBtnFullScreenClick(){
        if(mFullScreenMode){
            Context context = getContext();
            if(context instanceof  FullActivity){
                ((FullActivity) context).finish();
            }
        }else {
            Intent intent = new Intent(getContext(),FullActivity.class);
            intent.putExtra("_video_url",mVideoUrl);
            intent.putExtra("_video_title",mTitle);
            getContext().startActivity(intent);
        }
    }
    private void onControlViewClick(){
        if(!MediaPlayerManager.getInstance().isPlayed()){
            onBtnStartClick();
        }else{
            showControls(!mControlsShow);
        }
    }
    private void showControls(boolean show){
        if(show){
            btnStart.setVisibility(VISIBLE);
            rlControl.setVisibility(VISIBLE);
            if(mFullScreenMode){
                rlTitle.setVisibility(VISIBLE);
            }
        }else{
            btnStart.setVisibility(GONE);
            rlControl.setVisibility(GONE);
            if(mFullScreenMode){
                rlTitle.setVisibility(GONE);
            }
        }
        mControlsShow = show;
    }

    /**
     * 设置总时长信息
     * 在mediaplayer的onPrepare后才会有信息
     */
    private void setDuration(){
        tvTimeAll.setText(Utils.parseTime(MediaPlayerManager.getInstance().getDuration()));
        seekBar.setMax((int) MediaPlayerManager.getInstance().getDuration());
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        MediaPlayerManager.getInstance().setSurface(mSurface);
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG,"onSurfaceTextureDestroyed");
        return true;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        Log.i(TAG,"onBufferingUpdate-->"+i);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
//        onUpdateProgress(MediaPlayerManager.getInstance().getDuration());
//        tvTimeNow.setText(tvTimeAll.getText());
        btnStart.setImageResource(R.drawable.ic_video_play);
        imgThumb.setVisibility(VISIBLE);
    }
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Log.e(TAG,"Mediaplayer onError ("+i+","+i1+"+)");
        imgThumb.setVisibility(VISIBLE);
        btnStart.setVisibility(VISIBLE);
        progressBarLoading.setVisibility(GONE);
        rlControl.setVisibility(GONE);
        return false;
    }
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        MediaPlayerManager.getInstance().setPrepared(true);
        MediaPlayerManager.getInstance().start();
        progressBarLoading.setVisibility(GONE);
        btnStart.setImageResource(R.drawable.ic_video_pause);
        showControls(true);
        setDuration();
        MediaPlayerManager.getInstance().setSurface(mSurface);
    }
    @Override
    public void onUpdateProgress(long position) {
        tvTimeNow.setText(Utils.parseTime(position));
        if(mCanSeekBar) {
            seekBar.setProgress((int) position);
        }
    }
    public void setStartPlayVideoListener(OnStartPlayVideoListener startPlayVideoListener) {
        mStartPlayVideoListener = startPlayVideoListener;
    }
}
