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
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

import static android.os.Build.VERSION.SDK_INT;

public class PlayerService extends Service {


    IBinder binder=new sBinder();
    simplePlayer simplePlayer;
    ArrayList<AudioFile> audioFiles;
    boolean isForeground=false;
    boolean isPlay;
    Handler handler=new Handler();
    String CHANNEL_ID="244";
    NotificationManager nm;
    int i;
    OnPlayerEventListener onPlayerEventListener;

    int currentdur;
    int dur;
    public PlayerService() {

        simplePlayer=new simplePlayer(this);

    }

    public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener){
        this.onPlayerEventListener=onPlayerEventListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel=null;
        nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (SDK_INT >= Build.VERSION_CODES.O) {
            channel= new NotificationChannel(CHANNEL_ID,"SMP",NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                 currentdur=simplePlayer.getSongCurrentDuration();

                 dur=simplePlayer.getSongDuration();

                 handler.postDelayed(this,1000);
            }
        };



        Bundle bundle= intent.getExtras();
        if (bundle!=null)
            if (!bundle.isEmpty()) {
                audioFiles = new ArrayList<>();
                audioFiles = bundle.getParcelableArrayList("songs");
                  i = bundle.getInt("running_song");
            }

        if ("com.example.simplemp3player.play".equals(intent.getAction())){
            simplePlayer.init(audioFiles);
            simplePlayer.setCurrentPosition(i);
            simplePlayer.play();
            isPlay=true;

            handler.postDelayed(runnable,1000);
            if (!isForeground){

                startForeground(132, getNotification(this,isPlay));
                isForeground=true;
            }
            nm.notify(132,getNotification(this,isPlay));

        }else if("com.example.simplemp3player.replay".equals(intent.getAction())) {
            simplePlayer.resume();
            isPlay=true;
            nm.notify(132,getNotification(this,isPlay));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,1000);
            onPlayerEventListener.onPlayerPlay(isPlay);

        }else if ("com.example.simplemp3player.pause".equals(intent.getAction())){
            simplePlayer.pause();
            isPlay=false;
            nm.notify(132,getNotification(this,isPlay));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,1000);
            onPlayerEventListener.onPlayerPlay(isPlay);
        }else if ("com.example.simplemp3player.next".equals(intent.getAction())){

            simplePlayer.nextSong();
            isPlay=true;
            nm.notify(132,getNotification(this,isPlay));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,1000);

        }else if ("com.example.simplemp3player.prev".equals(intent.getAction())){

            simplePlayer.previousSong();
            isPlay=true;
            nm.notify(132,getNotification(this,isPlay));
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable,1000);

        }else if ("com.example.simplemp3player.seek_to".equals(intent.getAction())){

            simplePlayer.setSeekPosition( bundle.getInt("seek_to"));
        }




        return START_NOT_STICKY;
    }



    private Notification getNotification(Context context,boolean isPlay){

        NotificationCompat.Builder builder;



        Intent intent=new Intent(context,PlayerService.class);
        intent.setAction("com.example.simplemp3player.next");
        PendingIntent nextPendingIntent=PendingIntent.getService(context,1,intent,PendingIntent.FLAG_CANCEL_CURRENT);//PendingIntent.FLAG_UPDATE_CURRENT

        Intent intent1=new Intent(context,PlayerService.class);
        intent1.setAction("com.example.simplemp3player.prev");
        PendingIntent prevPendingIntent=PendingIntent.getService(context,2,intent1,PendingIntent.FLAG_CANCEL_CURRENT);


        Intent intent3=new Intent(context,PlayerService.class);
        intent3.setAction("com.example.simplemp3player.pause");
        PendingIntent pausePendingIntent=PendingIntent.getService(context,4,intent3,PendingIntent.FLAG_CANCEL_CURRENT);


        Intent intent2=new Intent(context,PlayerService.class);
        intent2.setAction("com.example.simplemp3player.replay");
        PendingIntent playPendingIntent=PendingIntent.getService(context,3,intent2,PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews remoteView=new RemoteViews(context.getPackageName(),
                R.layout.media_controll);


       String tilte = simplePlayer.getSongTilte();
       remoteView.setTextViewText(R.id.title,tilte);
        remoteView.setOnClickPendingIntent(R.id.next,nextPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.prev,prevPendingIntent);
        remoteView.setOnClickPendingIntent(R.id.pause, isPlay?pausePendingIntent:playPendingIntent);

        remoteView.setImageViewResource(R.id.pause,isPlay?R.drawable.ic_pause:R.drawable.ic_play);


        if (SDK_INT>= Build.VERSION_CODES.O){

            builder=new NotificationCompat.Builder(context,CHANNEL_ID);

        }else
        {
            builder = new NotificationCompat.Builder(context);

        }


        return builder.setDefaults(Notification.DEFAULT_ALL)
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

    public int getCurrentdur(){
        return currentdur;
    }

    public int getDur(){
        return  dur;
    }

    public class sBinder extends Binder{



        public PlayerService getService(){
            return   PlayerService.this;
        }

    }


}
