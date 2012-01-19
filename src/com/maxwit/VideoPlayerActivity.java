package com.maxwit;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaRecorder.VideoSource;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity {
    /** Called when the activity is first created. */
	VideoView videoView;
    MediaController mediaController;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setActivated(true);
        videoView.setKeepScreenOn(true);
        videoView.setBackgroundColor(Color.BLUE);
        
        String path = "/mnt/sdcard/Movies/Backkom-60.mp4";
        videoView.setVideoPath(path);
        mediaController = new MediaController(this);
        
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        
        videoView.requestFocus();
        videoView.setVisibility(VideoView.VISIBLE);
        videoView.start();       
    }
}
