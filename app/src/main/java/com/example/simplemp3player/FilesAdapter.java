package com.example.simplemp3player;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FilesAdapter extends BaseAdapter {

    private Context              context;
    private ArrayList<AudioFile> arrayList ;

    public FilesAdapter(Context context, ArrayList<AudioFile> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

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
        View view ;
        if (convertView == null)
            view = LayoutInflater.from(context).inflate(R.layout.row_file, parent, false);
        else
            view = convertView;


        TextView file = view.findViewById(R.id.file);
        TextView artist = view.findViewById(R.id.artist);

        String path = arrayList.get(position).getData();
        String file_name = path.substring(path.lastIndexOf("/") + 1, path.length() - 4);
        file.setText(file_name);
        artist.setText(arrayList.get(position).getArtist());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PlayerService.class);
                intent.setAction("com.example.simplemp3player.play");
                intent.putParcelableArrayListExtra("songs", arrayList);
                intent.putExtra("running_song", position);
                context.startService(intent);
            }
        });
        return view;
    }


}





