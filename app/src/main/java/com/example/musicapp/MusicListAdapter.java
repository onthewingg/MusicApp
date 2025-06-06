package com.example.musicapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MusicListAdapter
        extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>
        implements Filterable {

    private final ArrayList<AudioModel> fullList;    // all songs
    private ArrayList<AudioModel> filteredList;      // current display
    private final Context context;

    public MusicListAdapter(ArrayList<AudioModel> songsList, Context context) {
        this.fullList     = new ArrayList<>(songsList);
        this.filteredList = new ArrayList<>(songsList);
        this.context      = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel songData = filteredList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        if (MyMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.RED);
        } else {
            holder.titleTextView.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            MyMediaPlayer.getInstance().reset();
            MyMediaPlayer.currentIndex = position;
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.putExtra("LIST", filteredList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String q = constraint.toString().toLowerCase().trim();
                ArrayList<AudioModel> result = new ArrayList<>();
                if (q.isEmpty()) {
                    result.addAll(fullList);
                } else {
                    for (AudioModel s : fullList) {
                        if (s.getTitle().toLowerCase().contains(q)) {
                            result.add(s);
                        }
                    }
                }
                FilterResults fr = new FilterResults();
                fr.values = result;
                return fr;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<AudioModel>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  titleTextView;
        ImageView iconImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
        }
    }
}
