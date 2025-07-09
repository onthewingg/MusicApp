package com.example.musicapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.data.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter
        extends RecyclerView.Adapter<PlaylistsAdapter.VH> {

    /** Callback for when a playlist is clicked */
    public interface PlaylistClickListener {
        void onPlaylistClicked(Playlist playlist);
    }

    private final List<Playlist> items = new ArrayList<>();
    private final PlaylistClickListener listener;

    /** You must pass in a listener when you construct this adapter */
    public PlaylistsAdapter(@NonNull PlaylistClickListener listener) {
        this.listener = listener;
    }

    /** Replace the current list and refresh */
    public void setPlaylists(@NonNull List<Playlist> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    /** ItemTouchHelper needs this */
    public Playlist getPlaylistAt(int pos) { return items.get(pos); }   // ← NEW

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Playlist p = items.get(position);
        holder.title.setText(p.getName());

        // wire up the click
        holder.itemView.setOnClickListener(v -> {
            listener.onPlaylistClicked(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
        }
    }
}
