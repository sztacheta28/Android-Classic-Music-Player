package pl.androiddev.musicplayer.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.ATEActivity;
import pl.androiddev.musicplayer.utils.Helpers;

public class BaseThemedActivity extends ATEActivity {

    @Nullable
    @Override
    public String getATEKey() {
        return Helpers.getATEKey(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //make volume keys change multimedia volume even if music is not playing now
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
}
