package com.example.musicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.data.MusicDao;
import com.example.musicapp.data.MusicDatabase;
import com.example.musicapp.data.PlaylistWithSongs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

public class PlaylistDetailActivity extends AppCompatActivity {
    private long playlistId;
    private MusicDao dao;
    private RecyclerView rv;
    private TextView emptyText;
    private PlaylistSongsAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist_detail);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // grab playlistId
        playlistId = getIntent().getLongExtra("playlistId", -1);

        dao = MusicDatabase.getInstance(getApplicationContext()).musicDao();

        // find views
        rv        = findViewById(R.id.recycler_songs);
        emptyText = findViewById(R.id.empty_songs);
        FloatingActionButton fab = findViewById(R.id.fab_add_songs);

        // setup recycler
        rv.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new PlaylistSongsAdapter(path -> {
            // remove song from this playlist
            Executors.newSingleThreadExecutor().execute(() ->
                    dao.removeFromPlaylist(playlistId, path)
            );
        });
        rv.setAdapter(songAdapter);

        // observe songs in playlist
        dao.getPlaylistWithSongs(playlistId)
                .observe(this, new Observer<PlaylistWithSongs>() {
                    @Override
                    public void onChanged(PlaylistWithSongs data) {
                        songAdapter.setSongs(data.songs);
                        emptyText.setVisibility(data.songs.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });

        // add songs FAB
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, AddToPlaylistActivity.class);
            i.putExtra("playlistId", playlistId);
            startActivity(i);
        });
    }
}
