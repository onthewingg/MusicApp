package com.example.musicapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.data.MusicDao;
import com.example.musicapp.data.MusicDatabase;
import com.example.musicapp.data.PlaylistWithSongs;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;

/** Displays a single user playlist; swipe to remove songs, tap to play. */
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

        // ─── Toolbar ──────────────────────────────────────────────────────────────
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ─── Playlist id & DAO ────────────────────────────────────────────────────
        playlistId = getIntent().getLongExtra("playlistId", -1);
        dao = MusicDatabase.getInstance(getApplicationContext()).musicDao();

        // ─── Views ────────────────────────────────────────────────────────────────
        rv        = findViewById(R.id.recycler_songs);
        emptyText = findViewById(R.id.empty_songs);
        FloatingActionButton fab = findViewById(R.id.fab_add_songs);

        // ─── RecyclerView setup ───────────────────────────────────────────────────
        rv.setLayoutManager(new LinearLayoutManager(this));

        songAdapter = new PlaylistSongsAdapter((list, position) -> {
            MyMediaPlayer.getInstance().reset();
            MyMediaPlayer.currentIndex = position;

            Intent i = new Intent(this, MusicPlayerActivity.class);
            i.putExtra("LIST", list);          // AudioModel list is Serializable
            startActivity(i);
        });
        rv.setAdapter(songAdapter);

        /* ── Swipe-to-delete helper ─────────────────────────────────────────────── */
        ItemTouchHelper.SimpleCallback swipe =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override public boolean onMove(RecyclerView rv,
                                                    RecyclerView.ViewHolder a,
                                                    RecyclerView.ViewHolder b) { return false; }

                    @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                        int pos  = vh.getAdapterPosition();
                        String path = songAdapter.getSongAt(pos).getPath();

                        Executors.newSingleThreadExecutor().execute(() ->
                                dao.removeFromPlaylist(playlistId, path));
                    }
                };
        new ItemTouchHelper(swipe).attachToRecyclerView(rv);
        /* ───────────────────────────────────────────────────────────────────────── */

        // ─── Observe playlist songs ───────────────────────────────────────────────
        dao.getPlaylistWithSongs(playlistId)
                .observe(this, new Observer<PlaylistWithSongs>() {
                    @Override
                    public void onChanged(PlaylistWithSongs data) {
                        songAdapter.setSongs(data.songs);
                        emptyText.setVisibility(data.songs.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });

        // ─── FAB: add songs to playlist ───────────────────────────────────────────
        fab.setOnClickListener(v -> {
            Intent i = new Intent(this, AddToPlaylistActivity.class);
            i.putExtra("playlistId", playlistId);
            startActivity(i);
        });
    }
}
