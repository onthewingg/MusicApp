package com.example.musicapp;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.data.MusicDao;
import com.example.musicapp.data.MusicDatabase;
import com.example.musicapp.data.PlaylistSongCrossRef;
import com.example.musicapp.data.PlaylistWithSongs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * Screen that lets the user pick songs to add to a given playlist.
 *
 * Behaviour:
 *  • Lists every audio track found on the device, using the same scan logic as {@link MainActivity}.
 *  • Songs already inside the target playlist appear greyed-out.
 *  • Tapping a song that is not yet in the playlist adds it via {@link MusicDao#addToPlaylist}.
 */
public class AddToPlaylistActivity extends AppCompatActivity {

    private long           playlistId;
    private MusicDao       dao;
    private RecyclerView   rv;
    private SongsAdapter   adapter;

    // master list of all songs on device
    private final List<AudioModel> allSongs = new ArrayList<>();
    // paths that are ALREADY part of the playlist – updated via Room observer
    private final Set<String>      songsInPlaylist = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_to_playlist);

        // ═════════════ Toolbar ═════════════
        Toolbar toolbar = findViewById(R.id.toolbar_add_songs);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ═════════════ DAO / playlistId ═════════════
        playlistId = getIntent().getLongExtra("playlistId", -1);
        dao        = MusicDatabase.getInstance(getApplicationContext()).musicDao();

        // ═════════════ RecyclerView ═════════════
        rv = findViewById(R.id.recycler_all_songs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongsAdapter();
        rv.setAdapter(adapter);

        loadSongsFromStorage();
        observeExistingPlaylistEntries();
    }

    // ────────────────────────────────────────────────────────────────
    // Room observer – whenever playlist content changes we update UI
    // ────────────────────────────────────────────────────────────────
    private void observeExistingPlaylistEntries() {
        dao.getPlaylistWithSongs(playlistId).observe(this, (Observer<PlaylistWithSongs>) data -> {
            songsInPlaylist.clear();
            if (data != null) {
                for (AudioModel m : data.songs) songsInPlaylist.add(m.getPath());
            }
            adapter.notifyDataSetChanged();
        });
    }

    // ────────────────────────────────────────────────────────────────
    // MediaStore scan – identical to MainActivity.loadMusic()
    // ────────────────────────────────────────────────────────────────
    private void loadSongsFromStorage() {
        ContentResolver resolver = getContentResolver();

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        )) {
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                String title    = cursor.getString(0);
                String path     = cursor.getString(1);
                String duration = cursor.getString(2);
                if (new File(path).exists()) {
                    allSongs.add(new AudioModel(path, title, duration));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ────────────────────────────────────────────────────────────────
    // RecyclerView adapter
    // ────────────────────────────────────────────────────────────────
    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AudioModel song = allSongs.get(position);
            holder.title.setText(song.getTitle());

            boolean alreadyAdded = songsInPlaylist.contains(song.getPath());
            holder.itemView.setAlpha(alreadyAdded ? 0.3f : 1f);
            holder.itemView.setOnClickListener(v -> {
                if (alreadyAdded) {
                    Toast.makeText(AddToPlaylistActivity.this,
                            "Song already in playlist", Toast.LENGTH_SHORT).show();
                } else {
                    addSongToPlaylist(song);
                }
            });
        }

        @Override
        public int getItemCount() { return allSongs.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.music_title_text);
            }
        }
    }

    // ────────────────────────────────────────────────────────────────
    // DB insert helper
    // ────────────────────────────────────────────────────────────────
    private void addSongToPlaylist(@NonNull AudioModel song) {
        Executors.newSingleThreadExecutor().execute(() -> {

            dao.insertSong(song);                                           // NEW
            dao.addToPlaylist(new PlaylistSongCrossRef(playlistId,          // NEW
                    song.getPath()));    // NEW

            runOnUiThread(() -> {
                songsInPlaylist.add(song.getPath());
                adapter.notifyDataSetChanged();
                Toast.makeText(AddToPlaylistActivity.this,
                        "Added to playlist", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
