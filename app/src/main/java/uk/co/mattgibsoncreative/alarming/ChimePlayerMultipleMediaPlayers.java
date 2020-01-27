package uk.co.mattgibsoncreative.alarming;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;

import timber.log.Timber;

public class ChimePlayerMultipleMediaPlayers extends AbstractChimePlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    // We use a MediaPlayer for each sample so we can keep them
    // loaded and reduce latency. SoundPool would be better for
    // this if it didn't have an undocumented restriction on
    // maximum sound duration. Sigh. There's only three chimes
    // so we shouldn't have a resource problem here.
    private SparseArray<MediaPlayer> mMediaPlayers = new SparseArray<>();

    public ChimePlayerMultipleMediaPlayers(Context c) {
        super(c);
        AudioManager mAudioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null) {
            Timber.d("Couldn't get audio manager");
            throw new RuntimeException("Illegal argument loading sound files to MediaPlayer");
        }
    }

    public void playChime(int chimeResourceId, float volume) {
        MediaPlayer player = mMediaPlayers.get(chimeResourceId);

        // Our minimum interval between any two chimes is one minute, and all our samples
        // are significantly shorter than that. We should always, therefore, either be
        // already Prepared or at PlaybackCompleted.
        if (player != null) {
            // Still. Better safe than sorry.
            if (!player.isPlaying()) {
                player.setVolume(volume, volume);
                player.start();
                // Once this finishes it'll end up in PlaybackCompleted, at which
                // point it's fine to call start() again.
            }
        }
    }
    public void playChime(int chimeResourceId) {
        playChime(chimeResourceId, 1f);
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        // Lose the audio focus we acquired when we began playing a chime.
        //mAudioManager.abandonAudioFocus(this);
    }
    public void close() {
        for (int i=0; i<mMediaPlayers.size(); i++) {
            mMediaPlayers.valueAt(i).release();
            Timber.d("Released player for resource %d", mMediaPlayers.keyAt(i));
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub
        Timber.d("MediaPlayer prepared.");
    }

    @Override
    public void prepareChime(int chimeResourceId) {
        MediaPlayer player = mMediaPlayers.get(chimeResourceId);
        if (player == null) {
            player = MediaPlayer.create(mContext, chimeResourceId);
            if (player != null) {
                mMediaPlayers.append(chimeResourceId, player);
                player.setOnCompletionListener(this);
                Timber.d("Loaded and prepared player for resource %d", chimeResourceId);
            } else {
                Timber.d("Couldn't create a new MediaPlayer for resource %d", chimeResourceId);
            }
        }
    }
}
