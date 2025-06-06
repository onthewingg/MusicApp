package com.example.musicapp.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = { "playlistId", "songPath" })
public class PlaylistSongCrossRef {
    private long playlistId;

    @NonNull
    private String songPath;

    public PlaylistSongCrossRef(long playlistId, @NonNull String songPath) {
        this.playlistId = playlistId;
        this.songPath   = songPath;
    }

    public long getPlaylistId() {
        return playlistId;
    }
    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    @NonNull
    public String getSongPath() {
        return songPath;
    }
    public void setSongPath(@NonNull String songPath) {
        this.songPath = songPath;
    }
}
