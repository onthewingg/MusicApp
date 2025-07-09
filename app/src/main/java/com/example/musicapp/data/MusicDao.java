package com.example.musicapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.musicapp.AudioModel;            // ← NEW import

import java.util.List;

@Dao
public interface MusicDao {

    /* ── PLAYLIST CRUD ───────────────────────────── */
    @Insert
    long insertPlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

    @Query("SELECT * FROM Playlist ORDER BY name")
    LiveData<List<Playlist>> getAllPlaylists();

    /* ── FAVORITES helper ────────────────────────── */
    @Query("SELECT * FROM Playlist WHERE name = 'Favorites' LIMIT 1")
    Playlist getFavoritesPlaylist();

    /* ── SONG ⇄ PLAYLIST cross-ref ──────────────── */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addToPlaylist(PlaylistSongCrossRef crossRef);

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId=:plId AND songPath=:path")
    void removeFromPlaylist(long plId, String path);

    /* ── INSERT a song row so the join works ────── */   // NEW
    @Insert(onConflict = OnConflictStrategy.IGNORE)       // NEW
    void insertSong(AudioModel song);                     // NEW

    /* ── GET songs for one playlist ──────────────── */
    @Transaction
    @Query("SELECT * FROM Playlist WHERE id = :plId")
    LiveData<PlaylistWithSongs> getPlaylistWithSongs(long plId);

    @Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :plId")
    void removeAllSongsFromPlaylist(long plId);          // NEW
}
