package com.example.musicapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class AudioModel implements Serializable {
    @PrimaryKey
    @NonNull
    private String path;

    private String title;
    private String duration;

    // Room requires either public fields or getters/setters:
    public AudioModel(@NonNull String path, String title, String duration) {
        this.path     = path;
        this.title    = title;
        this.duration = duration;
    }

    /** No-arg constructor for Room **/
    public AudioModel() {}

    @NonNull
    public String getPath() {
        return path;
    }
    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
}
