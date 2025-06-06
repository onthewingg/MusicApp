package com.example.musicapp.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.musicapp.AudioModel;

import java.util.List;

public class PlaylistWithSongs {
    @Embedded
    public Playlist playlist;

    @Relation(
            parentColumn = "id",
            entityColumn = "path",            // <-- matches AudioModel’s @PrimaryKey field
            associateBy = @Junction(
                    value        = PlaylistSongCrossRef.class,
                    parentColumn = "playlistId",
                    entityColumn = "songPath"
            )
    )
    public List<AudioModel> songs;
}
