package com.example.simplemp3player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FilesAdapter extends BaseAdapter {

    private final Context              context;
    private final ArrayList<AudioFile> arrayList;
    final         PlayOnClick          playOnClick;

    public FilesAdapter(Context context, ArrayList<AudioFile> arrayList, PlayOnClick playOnClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.playOnClick = playOnClick;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null)
            view = LayoutInflater.from(context).inflate(R.layout.row_file, parent, false);
        else
            view = convertView;


        TextView file = view.findViewById(R.id.file);
        TextView artist = view.findViewById(R.id.artist);

        file.setText(arrayList.get(position).getSongTitle());
        artist.setText(arrayList.get(position).getArtist());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playOnClick.sendToPlay(position);
            }
        });
        return view;
    }

    interface PlayOnClick {
        void sendToPlay(int position);
    }
}





