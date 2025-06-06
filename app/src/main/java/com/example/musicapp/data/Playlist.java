package com.example.musicapp.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;

    /** No-arg constructor for Room **/
    public Playlist() {}

    /** Convenience constructor—Room will ignore this **/
    @Ignore
    public Playlist(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
