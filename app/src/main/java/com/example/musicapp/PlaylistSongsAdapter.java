package com.example.musicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Shows songs inside one playlist – row-tap starts playback, swipe removes. */
public class PlaylistSongsAdapter
        extends RecyclerView.Adapter<PlaylistSongsAdapter.VH> {

    /** Fired when the user taps a row to start playback. */
    public interface OnSongClick {
        void onClick(ArrayList<AudioModel> currentList, int position);
    }

    private final ArrayList<AudioModel> songs = new ArrayList<>();
    private final OnSongClick songClick;

    public PlaylistSongsAdapter(OnSongClick songClick) {
        this.songClick = songClick;
    }

    public void setSongs(List<AudioModel> list) {
        songs.clear();
        songs.addAll(list);
        notifyDataSetChanged();
    }

    /** Used by ItemTouchHelper to identify the swiped item. */
    public AudioModel getSongAt(int pos) { return songs.get(pos); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_playlist_item_song, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AudioModel m = songs.get(pos);
        h.title.setText(m.getTitle());

        h.itemView.setOnClickListener(v ->
                songClick.onClick(new ArrayList<>(songs), pos)
        );
    }

    @Override public int getItemCount() { return songs.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.music_title_text);
        }
    }
}
