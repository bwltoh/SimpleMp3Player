package com.example.simplemp3player;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnPlayerEventListener,FilesAdapter.PlayOnClick {

    public static final int PERMISSIN_ID = 32;
    PlayerService     playerService;
    ServiceConnection serviceConnection;
    // --Commented out by Inspection (10/14/2021 3:00 AM):boolean           isBound;
    ListView          listView;
    FilesAdapter      filesAdapter;
    Runnable runnable;
    boolean      isPlay = false;
    LinearLayout bottomSheetheader;
    SeekBar      seekBar;
    ImageButton  play, next, prev,play_all;
    TextView title,currentSongTime,totalSongTime;
    Handler handler;
    Transition transition;
    private BottomSheetBehavior<ConstraintLayout> sheetBehavior;
    ArrayList<AudioFile> audioFiles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setUPViews();


        runnable = new Runnable() {
            @Override
            public void run() {
                if (playerService != null) {
                    int current = playerService.getAudioProgressDuration();
                    int total = playerService.getAudioTotalDuration();

                    if (total != 0) {
                        seekBar.setProgress((int) (((float) current / total) * 100));
                        formateSongTime(currentSongTime,current);
                        formateSongTime(totalSongTime,total);

                    }


                }


                handler.postDelayed(this, 100);
            }
        };//runnable

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                handler.removeCallbacks(runnable);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = (seekBar.getProgress() * playerService.getAudioTotalDuration()) / 100;
                Intent intent = new Intent(MainActivity.this, PlayerService.class);
                intent.setAction("com.example.simplemp3player.seek_to");
                intent.putExtra("seek_to", progress);
                startService(intent);
                handler.postDelayed(runnable,200);

            }
        });//OnSeekBarChangeListener





        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                PlayerService.sBinder mBinder = (PlayerService.sBinder) service;
                playerService =  mBinder.getService();
                //isBound = true;
                playerService.setOnPlayerEventListener(MainActivity.this);
                playerService.setPlayList(audioFiles);
                playerService.sendCurrentSongInfo();
                handler.post(runnable);
                isPlay=playerService.isPlay();
                updatePlayButton(isPlay);


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //isBound = false;
                playerService = null;

                handler.removeCallbacks(runnable);
            }
        };//serviceConnection

         ConstraintLayout constraintLayout=findViewById(R.id.behavior);
          sheetBehavior=BottomSheetBehavior.from(constraintLayout);
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    setButtonVisibilityAndAnimation(View.VISIBLE);
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    setButtonVisibilityAndAnimation(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
      
            }
        });//BottomSheetCallback

      Intent intent=new Intent(this,PlayerService.class);
      intent.setAction("com.example.simplemp3player.init");
      startService(intent);
        if (checkPermission())
            getdata();
        else requestPermissions();

    }//onCreate

    private void formateSongTime(TextView textView, int time) {
        int hrs=(time/3600000);
        int mns=(time/60000)%60000;
        int scs=(time%60000)/1000;
        String son=String.format(Locale.ENGLISH,"%02d:%02d:%02d",hrs,mns,scs);
        textView.setText(son);
    }

    private void setUPViews() {
        listView = findViewById(R.id.list);
        seekBar = findViewById(R.id.progress);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        bottomSheetheader = findViewById(R.id.header);
        play_all = findViewById(R.id.play_all);
        title = findViewById(R.id.title);
        title.setSelected(true);
        currentSongTime= findViewById(R.id.current_time);
        totalSongTime= findViewById(R.id.total_time);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        play_all.setOnClickListener(this);
        bottomSheetheader.setOnClickListener(this);
        audioFiles = new ArrayList<>();
        transition=new Fade();
        transition.setDuration(200);
        transition.addTarget(play_all);

        handler = new Handler(getMainLooper());
    }

    private void setButtonVisibilityAndAnimation(int visibility) {
        TransitionManager.beginDelayedTransition((ViewGroup) play_all.getParent(),transition);
        play_all.setVisibility(visibility);
    }

    @Override
    protected void onStart() {
        super.onStart();


        Intent intent = new Intent(MainActivity.this, PlayerService.class);

        this.bindService(intent,
                serviceConnection, BIND_AUTO_CREATE);

    }//onStart

    @Override
    protected void onStop() {
        this.unbindService(serviceConnection);
        super.onStop();
    }//onStop


    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIN_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIN_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getdata();
            } else {
                finish();
            }
        }
    }//onRequestPermissionsResult

    void getdata() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.ArtistColumns.ARTIST,MediaStore.Audio.AudioColumns.TITLE
       };
       
        Cursor cursor = this.getContentResolver().query(uri, projection, MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")}, null);


        
        if (cursor != null) {
            while (cursor.moveToNext()) {

                AudioFile audioFile = new AudioFile();
                String data = cursor.getString(0);
                String artist = cursor.getString(1);
                String song_title = cursor.getString(2);
                audioFile.setData(data);
                audioFile.setArtist(artist);
                audioFile.setSongTitle(song_title);
                audioFiles.add(audioFile);

            }
            cursor.close();
        }
        filesAdapter = new FilesAdapter(this, audioFiles,this);
        listView.setAdapter(filesAdapter);
        title.setText(audioFiles.get(0).getSongTitle());
        listView.setItemChecked(0,true);


    }//getData

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play) {
            onPlay();
        } else if (id == R.id.next) {
            onNext();
        } else if (id == R.id.prev) {
            onPrev();
        } else if (id == R.id.play_all) {
            onPlay();
        } else if (id == R.id.header) {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            else if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }//onClick

    void updatePlayButton(boolean isplay1) {
        if (isplay1) {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));
            play_all.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_black, null));
        } else {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
            play_all.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_black, null));
        }
    }//updatePlayButton

    private void onNext() {
        startServiceWithAction("com.example.simplemp3player.next");
        updateHandler(isPlay);
        //handler.post(runnable);
    }//onNext

    private void onPrev() {
        startServiceWithAction("com.example.simplemp3player.prev");
        updateHandler(isPlay);
       // handler.post(runnable);
    }//onPrev

    private void onPlay() {

        if (isPlay) {
            startServiceWithAction("com.example.simplemp3player.pause");
          //  handler.removeCallbacks(runnable);



        } else {
            startServiceWithAction("com.example.simplemp3player.replay");
           // handler.post(runnable);


        }
        updateHandler(isPlay);

    }//onPlay


    private void startServiceWithAction(String action) {
        Intent intent = new Intent(MainActivity.this, PlayerService.class);
        intent.setAction(action);
        startService(intent);
    }//startServiceWithAction


    @Override
    public void onPlayerPlay(boolean isPlaying) {

        isPlay = isPlaying;
        updatePlayButton(isPlay);

        updateHandler(isPlay);

    }//onPlayerPlay

    private void updateHandler(boolean isPlay){
        if (!isPlay){
            handler.removeCallbacks(runnable);
        }else{
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }

    @Override
    public void onSongChanged(String title, int position) {

        this.title.setText(title);
        listView.setItemChecked(position,true);
    }

    @Override
    public void sendToPlay(int position) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction("com.example.simplemp3player.play");
        intent.putExtra("running_song", position);
        startService(intent);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_play",isPlay);
    }

    @Override
    public void onBackPressed() {

        if (sheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }
}//MainActivity


