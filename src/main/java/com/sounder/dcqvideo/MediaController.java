package com.sounder.dcqvideo;

/**
 * Created by Sounder on 2017/3/1.
 */

public interface MediaController {
    void start();
    void pause();
    int getCurrentPosition();
    boolean isPlaying();
    int getDuration();
    void setPosition(int position);
    void setDuration(int duration);
}
