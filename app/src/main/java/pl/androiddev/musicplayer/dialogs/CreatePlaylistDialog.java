package pl.androiddev.musicplayer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.fragments.PlaylistFragment;
import pl.androiddev.musicplayer.models.Song;

public class CreatePlaylistDialog extends DialogFragment {

    public static CreatePlaylistDialog newInstance() {
        return newInstance((Song) null);
    }

    public static CreatePlaylistDialog newInstance(Song song) {
        long[] songs;
        if (song == null) {
            songs = new long[0];
        } else {
            songs = new long[1];
            songs[0] = song.id;
        }
        return newInstance(songs);
    }

    public static CreatePlaylistDialog newInstance(long[] songList) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity()).positiveText("Create").negativeText("Cancel").input("Enter playlist name", "", false, (dialog, input) -> {

            long[] songs = getArguments().getLongArray("songs");
            long playistId = MusicPlayer.createPlaylist(getActivity(), input.toString());

            if (playistId != -1) {
                if (songs != null && songs.length != 0)
                    MusicPlayer.addToPlaylist(getActivity(), songs, playistId);
                else
                    Toast.makeText(getActivity(), "Created playlist", Toast.LENGTH_SHORT).show();
                if (getParentFragment() instanceof PlaylistFragment) {
                    ((PlaylistFragment) getParentFragment()).updatePlaylists(playistId);
                }
            } else {
                Toast.makeText(getActivity(), "Unable to create playlist", Toast.LENGTH_SHORT).show();
            }

        }).build();
    }
}
