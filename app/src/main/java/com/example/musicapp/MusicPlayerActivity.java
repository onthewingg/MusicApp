package com.example.musicapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;
    ImageView shuffleBtn, repeatBtn;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();

    private boolean isShuffle = false;
    private boolean isRepeatOn = false;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                int pos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(pos);
                currentTimeTv.setText(convertToMMSS(pos));
                pausePlay.setImageResource(
                        mediaPlayer.isPlaying()
                                ? R.drawable.baseline_pause_circle_outline_24
                                : R.drawable.baseline_play_circle_outline_24
                );
            }
            uiHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv       = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv   = findViewById(R.id.total_time);
        seekBar       = findViewById(R.id.seek_bar);
        pausePlay     = findViewById(R.id.pause_play);
        nextBtn       = findViewById(R.id.next);
        previousBtn   = findViewById(R.id.previous);
        musicIcon     = findViewById(R.id.music_icon_big);
        shuffleBtn    = findViewById(R.id.shuffle_btn);
        repeatBtn     = findViewById(R.id.repeat_btn);

        titleTv.setSelected(true);
        songsList = (ArrayList<AudioModel>) getIntent()
                .getSerializableExtra("LIST");

        shuffleBtn.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            shuffleBtn.setImageResource(
                    isShuffle
                            ? R.drawable.baseline_shuffle_on_24
                            : R.drawable.baseline_shuffle_24
            );
        });

        repeatBtn.setOnClickListener(v -> {
            isRepeatOn = !isRepeatOn;
            repeatBtn.setImageResource(
                    isRepeatOn
                            ? R.drawable.baseline_repeat_on_24
                            : R.drawable.baseline_repeat_24
            );
        });

        setResourcesWithMusic();
        uiHandler.post(updateRunnable);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacks(updateRunnable);
    }

    private void setResourcesWithMusic() {
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        titleTv.setText(currentSong.getTitle());
        totalTimeTv.setText(convertToMMSS(
                Long.parseLong(currentSong.getDuration())
        ));

        pausePlay.setOnClickListener(v -> pausePlay());
        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeatOn) {
                // repeat current song
                mp.seekTo(0);
                mp.start();

            } else if (isShuffle) {
                // shuffle next
                playShuffle();

            } else {
                // normal next
                if (MyMediaPlayer.currentIndex < songsList.size() - 1) {
                    MyMediaPlayer.currentIndex++;
                    mp.reset();
                    setResourcesWithMusic();
                }
                // else end playback
            }
        });

        playMusic();
    }

    private void playMusic() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (isShuffle) {
            playShuffle();
        } else {
            if (MyMediaPlayer.currentIndex < songsList.size() - 1) {
                MyMediaPlayer.currentIndex++;
                mediaPlayer.reset();
                setResourcesWithMusic();
            } else if (isRepeatOn) {
                // if user presses next at end and repeat is on, restart same
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        }
    }

    private void playPreviousSong() {
        if (isShuffle) {
            playShuffle();
        } else {
            if (MyMediaPlayer.currentIndex > 0) {
                MyMediaPlayer.currentIndex--;
                mediaPlayer.reset();
                setResourcesWithMusic();
            } else if (isRepeatOn) {
                // at start and repeat on, restart same
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        }
    }

    private void playShuffle() {
        if (songsList.size() < 2) return;
        Random rnd = new Random();
        int idx;
        do {
            idx = rnd.nextInt(songsList.size());
        } while (idx == MyMediaPlayer.currentIndex);
        MyMediaPlayer.currentIndex = idx;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        else mediaPlayer.start();
    }

    public static String convertToMMSS(long millis) {
        long m = TimeUnit.MILLISECONDS.toMinutes(millis);
        long s = TimeUnit.MILLISECONDS.toSeconds(millis)
                % TimeUnit.MINUTES.toSeconds(1);
        return String.format("%02d:%02d", m, s);
    }
}
