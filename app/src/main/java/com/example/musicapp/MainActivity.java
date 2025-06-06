package com.example.musicapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.data.MusicDao;
import com.example.musicapp.data.MusicDatabase;
import com.example.musicapp.data.Playlist;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;

    private RecyclerView recyclerView;
    private TextView noMusicTextView;
    private SearchView searchView;
    private MusicListAdapter adapter;
    private ArrayList<AudioModel> songsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── BOOTSTRAP “FAVORITES” PLAYLIST ──
        Executors.newSingleThreadExecutor().execute(() -> {
            MusicDao dao = MusicDatabase
                    .getInstance(getApplicationContext())
                    .musicDao();
            if (dao.getFavoritesPlaylist() == null) {
                dao.insertPlaylist(new Playlist("Favorites"));
            }
        });

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // set up our Toolbar (white, no title)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        searchView      = findViewById(R.id.search_view);
        recyclerView    = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);

        if (!checkPermission()) {
            requestPermission();
            return;
        }

        loadMusic();
        setupRecyclerAndSearch();
    }

    private void loadMusic() {
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection, selection, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title    = cursor.getString(0);
                String path     = cursor.getString(1);
                String duration = cursor.getString(2);
                if (new File(path).exists()) {
                    songsList.add(new AudioModel(path, title, duration));
                }
            }
            cursor.close();
        }
    }

    private void setupRecyclerAndSearch() {
        adapter = new MusicListAdapter(songsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        noMusicTextView.setVisibility(
                songsList.isEmpty() ? View.VISIBLE : View.GONE
        );

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                noMusicTextView.setVisibility(
                        adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
                );
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this,
                    "Permission required to access audio files",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ permission },
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                songsList.clear();
                loadMusic();
                setupRecyclerAndSearch();

            } else {
                Toast.makeText(this,
                        "Permission Denied",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null && adapter != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    // ── inflate our menu so star & playlist icons appear ──
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_playlists) {      // <- only playlist button remains
            startActivity(new Intent(this, PlaylistsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


