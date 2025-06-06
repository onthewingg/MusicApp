package com.example.musicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/** shows songs in a single playlist, allows removal */
public class PlaylistSongsAdapter
        extends RecyclerView.Adapter<PlaylistSongsAdapter.VH> {

    public interface OnRemoveListener {
        void onRemove(String songPath);
    }

    private final ArrayList<AudioModel> songs = new ArrayList<>();
    private final OnRemoveListener removeListener;

    public PlaylistSongsAdapter(OnRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setSongs(java.util.List<AudioModel> list) {
        songs.clear();
        songs.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        // you might want a separate layout with a remove button, but for simplicity:
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AudioModel m = songs.get(position);
        holder.title.setText(m.getTitle());
        holder.removeBtn.setOnClickListener(v ->
                removeListener.onRemove(m.getPath())
        );
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        ImageButton removeBtn;
        VH(@NonNull View itemView) {
            super(itemView);
            title     = itemView.findViewById(R.id.music_title_text);
            // suppose you've added an ImageButton with id remove_btn to your item layout:
            removeBtn = itemView.findViewById(R.id.remove_btn);
        }
    }
}
