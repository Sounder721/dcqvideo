package com.sounder.dcqvideo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.sounder.dcqvideo.widgets.DcqVideoView;

public class FullActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full);

        Intent data = getIntent();
        String videoUrl = data.getStringExtra("_video_url");
        String title = data.getStringExtra("_video_title");
        DcqVideoView video = (DcqVideoView) findViewById(R.id.video);
        video.setUp(videoUrl,title);
        video.setFullScreen();
    }
}
