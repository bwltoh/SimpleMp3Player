package com.example.simplemp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static android.os.Build.VERSION.SDK_INT;

public class PlayerService extends Service {


    final String CHANNEL_ID = "244";
    IBinder               binder       = new sBinder();
    simplePlayer          simplePlayer;
    ArrayList<AudioFile>  audioFiles;
    boolean               isForeground = false;
    boolean               isPlay       = false;
    Handler               handler      = new Handler(Looper.getMainLooper());
    NotificationManager   nm;
    int                   i;
    OnPlayerEventListener onPlayerEventListener;

    int progressDuration;
    int totalDuration;

    public PlayerService() {

        simplePlayer = new simplePlayer(this);

    }


    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel;
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, "SMP", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                progressDuration = simplePlayer.getSongProgressDuration();

                totalDuration = simplePlayer.getSongTotalDuration();

                handler.postDelayed(this, 100);
            }
        };


        Bundle bundle = intent.getExtras();
        if (bundle != null)
            if (!bundle.isEmpty()) {
                audioFiles = new ArrayList<>();
                audioFiles = bundle.getParcelableArrayList("songs");
                i = bundle.getInt("running_song");
            }

        if ("com.example.simplemp3player.init".equals(intent.getAction())) {
            simplePlayer.init();
        } else if ("com.example.simplemp3player.play".equals(intent.getAction())) {

            simplePlayer.setCurrentPosition(i);
            simplePlayer.play();
            updateNotificationAndPlayingState(true, true, runnable);

            if (!isForeground) {

                startForeground(132, getNotification(this, isPlay));
                isForeground = true;
            }
        } else if ("com.example.simplemp3player.replay".equals(intent.getAction())) {
            simplePlayer.resume();

            updateNotificationAndPlayingState(false, true, runnable);
            if (!isForeground) {

                startForeground(132, getNotification(this, isPlay));
                isForeground = true;
            }

        } else if ("com.example.simplemp3player.pause".equals(intent.getAction())) {
            simplePlayer.pause();

            updateNotificationAndPlayingState(false, false, runnable);

        } else if ("com.example.simplemp3player.next".equals(intent.getAction())) {

            simplePlayer.nextSong();

            updateNotificationAndPlayingState(true, true, runnable);


        } else if ("com.example.simplemp3player.prev".equals(intent.getAction())) {

            simplePlayer.previousSong();

            updateNotificationAndPlayingState(true, true, runnable);

        } else if ("com.example.simplemp3player.seek_to".equals(intent.getAction())) {

            if (bundle != null) {
                simplePlayer.setSeekPosition(bundle.getInt("seek_to"));
            }
        }


        return START_NOT_STICKY;
    }

    private void updateNotificationAndPlayingState(boolean songChanged, boolean playState, Runnable runnable) {
        isPlay = playState;
        nm.notify(132, getNotification(this, isPlay));
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 100);
        if (songChanged) {
            onPlayerEventListener.onPlayerPlay(isPlay);
            onPlayerEventListener.onSongChanged(simplePlayer.getSongTilte(), getCurrent_position());

        } else
            onPlayerEventListener.onPlayerPlay(isPlay);

    }

    private Notification getNotification(Context context, boolean isPlay) {

        NotificationCompat.Builder builder;


        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction("com.example.simplemp3player.next");
        PendingIntent nextPendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);//PendingIntent.FLAG_UPDATE_CURRENT

        Intent intent1 = new Intent(context, PlayerService.class);
        intent1.setAction("com.example.simplemp3player.prev");
        PendingIntent prevPendingIntent = PendingIntent.getService(context, 2, intent1, PendingIntent.FLAG_CANCEL_CURRENT);


        Intent intent3 = new Intent(context, PlayerService.class);
        intent3.setAction("com.example.simplemp3player.pause");
        PendingIntent pausePendingIntent = PendingIntent.getService(context, 4, intent3, PendingIntent.FLAG_CANCEL_CURRENT);


        Intent intent2 = new Intent(context, PlayerService.class);
        intent2.setAction("com.example.simplemp3player.replay");
        PendingIntent playPendingIntent = PendingIntent.getService(context, 3, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews remoteView = new RemoteViews(context.getPackageName(),
                R.layout.media_controll);


        String tilte = getTitle();
        remoteView.setTextViewText(R.id.title, tilte);
        remoteView.setOnClickPendingIntent(R.id.next, nextPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.prev, prevPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.pause, isPlay ? pausePendingIntent : playPendingIntent);

        remoteView.setImageViewResource(R.id.pause, isPlay ? R.drawable.ic_pause_black : R.drawable.ic_play_black);


        if (SDK_INT >= Build.VERSION_CODES.O) {

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        } else {
            builder = new NotificationCompat.Builder(context);

        }


        return builder
                .setSmallIcon(R.drawable.ic_stat_new_message)
                .setCustomContentView(remoteView)
                .setChannelId(CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false).build();

    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {

        stopForeground(true);
        simplePlayer.stop();
        super.onDestroy();
    }

    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
        this.onPlayerEventListener = onPlayerEventListener;
    }

    public void sendCurrentSongInfo() {
        onPlayerEventListener.onSongChanged(getTitle(), getCurrent_position());
    }

    public boolean isPlay() {
        return isPlay;
    }

    public int getCurrent_position() {
        return simplePlayer.getCurrentPosition();
    }

    public int getAudioProgressDuration() {
        return progressDuration;
    }

    public int getAudioTotalDuration() {
        return totalDuration;
    }

    public String getTitle() {
        return simplePlayer.getSongTilte();
    }

    public void setPlayList(ArrayList<AudioFile> audioFiles) {
        simplePlayer.setSongs(audioFiles);
    }

    public class sBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }

    }


}
