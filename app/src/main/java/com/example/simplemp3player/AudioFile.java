package com.example.simplemp3player;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioFile implements Parcelable {

    public static final Creator<AudioFile> CREATOR = new Creator<AudioFile>() {
        @Override
        public AudioFile createFromParcel(Parcel in) {
            return new AudioFile(in);
        }

        @Override
        public AudioFile[] newArray(int size) {
            return new AudioFile[size];
        }
    };
    String data;
    String artist;
    String songTitle;
    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }



    public AudioFile() {
    }

    public AudioFile(String data, String artist,String songTitle) {
        this.data = data;
        this.artist = artist;
        this.songTitle = songTitle;
    }

    protected AudioFile(Parcel in) {
        data = in.readString();
        artist = in.readString();
        songTitle = in.readString();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
        dest.writeString(artist);
        dest.writeString(songTitle);
    }
}
