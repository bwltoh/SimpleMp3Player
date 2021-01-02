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
    public        int                  currentPosition = 0;
    public        int                  currentDuration = 0;
    Context context;


    public simplePlayer(Context context) {
        this.context = context;

    }

    public void init(ArrayList<AudioFile> _songs) {
        songs = _songs;
        currentPosition = 0;
        if (player == null) {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    player.stop();
                    player.reset();
                    Intent intent = new Intent(context, PlayerService.class);
                    intent.setAction("com.example.simplemp3player.next");
                    context.startService(intent);

                }
            });


        }
    }

    public void stop() {
        if (player != null) {
            if (player.isPlaying())
                player.stop();
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

                            Uri u = Uri.fromFile(new File(songs.get(currentPosition).getData()));

                            player.setDataSource(context, u);
                            player.prepare();

                            currentDuration = player.getDuration();

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
            if (player.isPlaying())
                player.stop();
            player.reset();
            if ((currentPosition + 1) == songs.size())
                currentPosition = 0;
            else currentPosition = currentPosition + 1;
            play();
        }
    }

    public void previousSong() {
        if (player != null) {
            if (isPaused) isPaused = false;
            if (player.isPlaying()) player.stop();
            player.reset();
            if (currentPosition - 1 < 0)
                currentPosition = songs.size();
            else currentPosition = currentPosition - 1;
            play();
        }
    }


    public void setSeekPosition(int msec) {
        if (player != null) player.seekTo(msec);
    }


    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getSongTilte() {

        return songs.get(currentPosition).getData();
    }


    public int getSongCurrentDuration() {
        return player.getCurrentPosition();
    }

    public int getSongDuration() {
        return player.getDuration();
    }
}