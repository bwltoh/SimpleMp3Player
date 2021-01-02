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
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnPlayerEventListener {


    public static final int PERMISSIN_ID = 32;
    PlayerService     playerService;
    ServiceConnection serviceConnection;
    boolean           isBound;
    ListView          listView;
    FilesAdapter      filesAdapter;
    Runnable runnable;
    boolean      isPlay = true;
    LinearLayout controll_layout;
    SeekBar      seekBar;
    ImageButton  play, next, prev;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.list);
        seekBar = findViewById(R.id.progress);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        controll_layout = findViewById(R.id.media_controll);

        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (playerService != null) {
                    int current = playerService.getCurrentdur();
                    int total = playerService.getDur();

                    if (total != 0) {
                        seekBar.setProgress((int) (((float) current / total) * 100));

                        controll_layout.setVisibility(View.VISIBLE);

                    }


                }


                handler.postDelayed(this, 500);
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
                int progress = (seekBar.getProgress() * playerService.getDur()) / 100;
                Intent intent = new Intent(MainActivity.this, PlayerService.class);
                intent.setAction("com.example.simplemp3player.seek_to");
                intent.putExtra("seek_to", progress);
                startService(intent);
                handler.post(runnable);

            }
        });


        if (checkPermission())
            getdata();


        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                PlayerService.sBinder mBinder = (PlayerService.sBinder) service;
                playerService = (PlayerService) mBinder.getService();
                isBound = true;

                playerService.setOnPlayerEventListener(MainActivity.this);
                handler.post(runnable);


            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                playerService = null;

                handler.removeCallbacks(runnable);
            }
        };//serviceConnection


    }//onCreate

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermission())
            requestPermissions();

        Intent intent = new Intent(MainActivity.this, PlayerService.class);

        this.bindService(intent,
                serviceConnection, BIND_AUTO_CREATE);
    }//onStart

    @Override
    protected void onStop() {
        this.unbindService(serviceConnection);
        super.onStop();
    }//onStop

    @Override
    protected void onDestroy() {

        stopService(new Intent(MainActivity.this, PlayerService.class));
        super.onDestroy();
    }//onDestroy


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
                MediaStore.Audio.ArtistColumns.ARTIST};
       
        Cursor cursor = this.getContentResolver().query(uri, projection, MediaStore.Files.FileColumns.MIME_TYPE + "=?",
                new String[]{MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")}, null);

        ArrayList<AudioFile> audioFiles = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {

                AudioFile audioFile = new AudioFile();
                String data = cursor.getString(0);
                String artist = cursor.getString(1);

                audioFile.setData(data);
                audioFile.setArtist(artist);

                audioFiles.add(audioFile);

            }
            cursor.close();
        }
        filesAdapter = new FilesAdapter(this, audioFiles);
        listView.setAdapter(filesAdapter);
    }//getData

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.play:
                onPlay();
                break;
            case R.id.next:
                onNext();
                break;
            case R.id.prev:
                onPrev();
                break;
        }
    }//onClick

    void updatePlayButton(boolean isplay1) {
        if (isplay1) {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause, null));
        } else {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play, null));
        }
    }//updatePlayButton

    private void onNext() {
        startServiceWithAction("com.example.simplemp3player.next");


        isPlay = true;
        updatePlayButton(isPlay);
        handler.post(runnable);
    }//onNext

    private void onPrev() {
        startServiceWithAction("com.example.simplemp3player.prev");


        isPlay = true;
        updatePlayButton(isPlay);
        handler.post(runnable);
    }//onPrev

    private void onPlay() {

        if (isPlay) {
            startServiceWithAction("com.example.simplemp3player.pause");
            handler.removeCallbacks(runnable);


            isPlay = false;
            updatePlayButton(isPlay);

        } else {
            startServiceWithAction("com.example.simplemp3player.replay");
            handler.post(runnable);


            isPlay = true;
            updatePlayButton(isPlay);

        }


    }//onPlay


    @Override
    protected void onResume() {
        super.onResume();

    }//onResume

    private void startServiceWithAction(String action) {
        Intent intent = new Intent(MainActivity.this, PlayerService.class);
        intent.setAction(action);
        startService(intent);
    }//startServiceWithAction


    @Override
    public void onPlayerPlay(boolean isPlaying) {

        isPlay = isPlaying;
        updatePlayButton(isPlay);

    }//onPlayerPlay

}//MainActivity


