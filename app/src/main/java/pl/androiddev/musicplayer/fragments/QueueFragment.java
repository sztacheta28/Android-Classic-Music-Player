package pl.androiddev.musicplayer.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.activities.BaseActivity;
import pl.androiddev.musicplayer.adapters.PlayingQueueAdapter;
import pl.androiddev.musicplayer.dataloaders.QueueLoader;
import pl.androiddev.musicplayer.listeners.MusicStateListener;
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.widgets.DragSortRecycler;

public class QueueFragment extends Fragment implements MusicStateListener {

    private PlayingQueueAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_queue, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.playing_queue);
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(null);

        new loadQueueSongs().execute("");
        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);		
        ATE.apply(this, "dark_theme");
    }

    public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private class loadQueueSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mAdapter = new PlayingQueueAdapter(getActivity(), QueueLoader.getQueueSongs(getActivity()));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.reorder);

            dragSortRecycler.setOnItemMovedListener((from, to) -> {
                Log.d("queue", "onItemMoved " + from + " to " + to);
                Song song = mAdapter.getSongAt(from);
                mAdapter.removeSongAt(from);
                mAdapter.addSongTo(to, song);
                mAdapter.notifyDataSetChanged();
                MusicPlayer.moveQueueItem(from, to);
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

            recyclerView.getLayoutManager().scrollToPosition(mAdapter.currentlyPlayingPosition);

        }

        @Override
        protected void onPreExecute() {
        }
    }

}

