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
import androidx.appcompat.widget.SearchView;          // ← NEW
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
 * • Lists every audio track found on the device.
 * • Songs already in the playlist appear dimmed.
 * • Tap to add; live SearchView filters the list.
 */
public class AddToPlaylistActivity extends AppCompatActivity {

    private long         playlistId;
    private MusicDao     dao;
    private RecyclerView rv;
    private SongsAdapter adapter;
    private SearchView   searchView;               // NEW

    /** master list of ALL songs on device            */ private final List<AudioModel> allSongs        = new ArrayList<>();
    /** subset shown after filtering (adapter uses)   */ private final List<AudioModel> filteredSongs  = new ArrayList<>();
    /** paths that are already part of the playlist   */ private final Set<String>      songsInPlaylist = new HashSet<>();

    // ═══════════════════════════════════════════════════ onCreate
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_to_playlist);

        // toolbar
        Toolbar tb = findViewById(R.id.toolbar_add_songs);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());

        playlistId = getIntent().getLongExtra("playlistId", -1);
        dao        = MusicDatabase.getInstance(getApplicationContext()).musicDao();

        // views
        searchView = findViewById(R.id.search_view_add);      // NEW
        rv         = findViewById(R.id.recycler_all_songs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SongsAdapter();
        rv.setAdapter(adapter);

        hookSearchView();           // NEW
        loadSongsFromStorage();
        observeExistingPlaylistEntries();
    }

    // ═══════════════════════════════════════════════════ live search
    private void hookSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) {
                filterSongs(q);
                return true;
            }
            @Override public boolean onQueryTextChange(String q) {
                filterSongs(q);
                return true;
            }
        });
    }
    private void filterSongs(@NonNull String query) {
        filteredSongs.clear();
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) {
            filteredSongs.addAll(allSongs);
        } else {
            for (AudioModel s : allSongs) {
                if (s.getTitle().toLowerCase().contains(q)) filteredSongs.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // ═══════════════════════════════════════════════════ Room observer
    private void observeExistingPlaylistEntries() {
        dao.getPlaylistWithSongs(playlistId).observe(this,
                (Observer<PlaylistWithSongs>) data -> {
                    songsInPlaylist.clear();
                    if (data != null)
                        for (AudioModel m : data.songs) songsInPlaylist.add(m.getPath());
                    adapter.notifyDataSetChanged();
                });
    }

    // ═══════════════════════════════════════════════════ MediaStore scan
    private void loadSongsFromStorage() {
        ContentResolver resolver = getContentResolver();
        String[] proj = { MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION };
        String sel = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        try (Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                proj, sel, null, null)) {
            if (c == null) return;
            while (c.moveToNext()) {
                String title = c.getString(0);
                String path  = c.getString(1);
                String dur   = c.getString(2);
                if (new File(path).exists())
                    allSongs.add(new AudioModel(path, title, dur));
            }
        }
        filteredSongs.clear();
        filteredSongs.addAll(allSongs);    // initial state = full list
        adapter.notifyDataSetChanged();
    }

    // ═══════════════════════════════════════════════════ RecyclerView adapter
    private class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.VH> {

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_item, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            AudioModel song = filteredSongs.get(pos);
            h.title.setText(song.getTitle());

            boolean alreadyAdded = songsInPlaylist.contains(song.getPath());
            h.itemView.setAlpha(alreadyAdded ? 0.3f : 1f);
            h.itemView.setOnClickListener(v -> {
                if (alreadyAdded) {
                    Toast.makeText(AddToPlaylistActivity.this,
                            "Song already in playlist", Toast.LENGTH_SHORT).show();
                } else addSongToPlaylist(song);
            });
        }

        @Override public int getItemCount() { return filteredSongs.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.music_title_text);
            }
        }
    }

    // ═══════════════════════════════════════════════════ insert helper
    private void addSongToPlaylist(@NonNull AudioModel song) {
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.insertSong(song);
            dao.addToPlaylist(new PlaylistSongCrossRef(playlistId, song.getPath()));

            runOnUiThread(() -> {
                songsInPlaylist.add(song.getPath());
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
