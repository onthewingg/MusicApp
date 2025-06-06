package com.example.musicapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.musicapp.data.MusicDao;
import com.example.musicapp.data.MusicDatabase;
import com.example.musicapp.data.Playlist;

import java.util.List;
import java.util.concurrent.Executors;

public class PlaylistsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView noPlaylistsText;
    private PlaylistsAdapter adapter;
    private MusicDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlists);

        // Toolbar with back arrow
        Toolbar toolbar = findViewById(R.id.toolbar_playlists);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // find views
        recyclerView    = findViewById(R.id.recycler_view_playlists);
        noPlaylistsText = findViewById(R.id.no_playlists_text);
        FloatingActionButton fab = findViewById(R.id.fab_add_playlist);

        // get DAO
        dao = MusicDatabase.getInstance(getApplicationContext()).musicDao();

        // set up RecyclerView with click-listener
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaylistsAdapter(playlist -> {
            // when a playlist is tapped, launch detail screen
            Intent i = new Intent(PlaylistsActivity.this, PlaylistDetailActivity.class);
            i.putExtra("playlistId", playlist.getId());
            startActivity(i);
        });
        recyclerView.setAdapter(adapter);

        // observe existing playlists
        dao.getAllPlaylists().observe(this, playlists -> {
            adapter.setPlaylists(playlists);
            noPlaylistsText.setVisibility(
                    playlists.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        // wire up FAB to create new playlist
        fab.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    /** Shows an AlertDialog with an EditText to enter the new playlist’s name */
    private void showCreatePlaylistDialog() {
        final EditText input = new EditText(this);
        input.setHint("My playlist");
        new AlertDialog.Builder(this)
                .setTitle("New Playlist")
                .setMessage("Enter playlist name:")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        Executors.newSingleThreadExecutor().execute(() ->
                                dao.insertPlaylist(new Playlist(name))
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
