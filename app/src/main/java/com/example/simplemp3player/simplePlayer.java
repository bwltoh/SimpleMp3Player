package com.example.simplemp3player;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

public class simplePlayer {

    public static MediaPlayer          player;
    public        ArrayList<AudioFile> songs           = null;
    public        boolean              isPaused        = false;
    public        int                  currentRunningSongPosition = 0;
   // public int     currentDuration = 0;
    final  Context context;


    public simplePlayer(Context context) {
        this.context = context;

    }

    public void init() {

        if (player == null) {

            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return true;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    mp.stop();
                    mp.reset();
                    Intent intent = new Intent(context, PlayerService.class);
                    intent.setAction("com.example.simplemp3player.next");
                    context.startService(intent);

                }
            });
            currentRunningSongPosition = 0;

        }
    }

    public void stop() {
        if (player != null) {
            if (player.isPlaying())
                player.stop();
            player.release();
        }
    }

    public void pause() {
        if (!isPaused && player != null) {

            player.pause();

            isPaused = true;
        }
    }

    public void play() {
        if (player != null) {
            if (!isPaused) {
                if (songs != null) {
                    if (songs.size() > 0) {

                        if (player.isPlaying()) {
                            player.stop();
                            player.reset();
                        }
                        try {

                            Uri u = Uri.fromFile(new File(songs.get(currentRunningSongPosition).getData()));

                            player.setDataSource(context, u);
                            player.prepare();

                            player.start();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else {
                player.start();
                isPaused = false;
            }
        }
    }

    public void resume() {
        if (player != null) {
            play();
            if (isPaused) isPaused = false;
        }
    }


    public void nextSong() {
        if (player != null) {
            if (isPaused) isPaused = false;

            if ((currentRunningSongPosition + 1) == songs.size())
                currentRunningSongPosition = 0;
            else currentRunningSongPosition = currentRunningSongPosition + 1;
            play();
        }
    }

    public void previousSong() {
        if (player != null) {
            if (isPaused) isPaused = false;

            if (currentRunningSongPosition - 1 < 0)
                currentRunningSongPosition = songs.size()-1;
            else currentRunningSongPosition = currentRunningSongPosition - 1;
            play();
        }
    }


    public void setSongs(ArrayList<AudioFile> _songs){
        songs=_songs;
    }

    public void setSeekPosition(int msec) {
        if (player != null) player.seekTo(msec);
    }


    public void setCurrentPosition(int currentPosition) {
        this.currentRunningSongPosition = currentPosition;
    }
    public int getCurrentPosition(){
        return currentRunningSongPosition;
    }
    public String getSongTilte() {

        if (songs.size()>0)
        return songs.get(currentRunningSongPosition).getSongTitle();
        else return "";
    }


    public int getSongProgressDuration() {
        return player.getCurrentPosition();
    }

    public int getSongTotalDuration() {
        return player.getDuration();
    }
}