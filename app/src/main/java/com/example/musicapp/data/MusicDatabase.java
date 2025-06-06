package com.example.musicapp.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.musicapp.AudioModel;

@Database(
        entities = {
                Playlist.class,
                PlaylistSongCrossRef.class,
                AudioModel.class
        },
        version = 1
)
public abstract class MusicDatabase extends RoomDatabase {
    public abstract MusicDao musicDao();

    private static volatile MusicDatabase INSTANCE;
    public static MusicDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (MusicDatabase.class) {
                INSTANCE = Room.databaseBuilder(
                        ctx.getApplicationContext(),
                        MusicDatabase.class,
                        "music_db"
                ).build();
            }
        }
        return INSTANCE;
    }
}
