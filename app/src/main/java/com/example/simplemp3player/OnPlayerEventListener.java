package com.example.simplemp3player;

public interface OnPlayerEventListener {

       void onPlayerPlay (boolean isPlay);
       void onSongChanged(String title,int position);
}
