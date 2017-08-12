package pl.androiddev.musicplayer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.dataloaders.PlaylistLoader;
import pl.androiddev.musicplayer.dialogs.CreatePlaylistDialog;
import pl.androiddev.musicplayer.models.Playlist;
import pl.androiddev.musicplayer.subfragments.PlaylistPagerFragment;
import pl.androiddev.musicplayer.utils.Constants;
import pl.androiddev.musicplayer.utils.PreferencesUtility;
import pl.androiddev.musicplayer.widgets.MultiViewPager;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    int playlistcount;
    FragmentStatePagerAdapter adapter;
    MultiViewPager pager;
    RecyclerView recyclerView;

    private PreferencesUtility mPreferences;
    private boolean showAuto;

    private List<Playlist> playlists = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
        showAuto = mPreferences.showAutoPlaylist();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_playlist, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        pager = (MultiViewPager) rootView.findViewById(R.id.playlistpager);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);


        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.playlists);
        }

        playlists = PlaylistLoader.getPlaylists(getActivity(), showAuto);
        playlistcount = playlists.size();

        initPager();

        return rootView;

    }

    private void initPager() {
        pager.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(null);
        adapter = new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public int getCount() {
                return playlistcount;
            }

            @Override
            public Fragment getItem(int position) {
                return PlaylistPagerFragment.newInstance(position);
            }

        };
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ATE.apply(this, "dark_theme");
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (showAuto) {
            menu.findItem(R.id.action_view_auto_playlists).setTitle("Hide auto playlists");
        } else menu.findItem(R.id.action_view_auto_playlists).setTitle("Show auto playlists");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_playlist:
                CreatePlaylistDialog.newInstance().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_view_auto_playlists:
                if (showAuto) {
                    showAuto = false;
                    mPreferences.setToggleShowAutoPlaylist(false);
                } else {
                    showAuto = true;
                    mPreferences.setToggleShowAutoPlaylist(true);
                }
                reloadPlaylists();
                getActivity().invalidateOptionsMenu();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void updatePlaylists(final long id) {
        playlists = PlaylistLoader.getPlaylists(getActivity(), showAuto);
        playlistcount = playlists.size();

        adapter.notifyDataSetChanged();
        if (id != -1) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                for (int i = 0; i < playlists.size(); i++) {
                    long playlistid = playlists.get(i).id;
                    if (playlistid == id) {
                        pager.setCurrentItem(i);
                        break;
                    }
                }
            }, 200);
        }
    }

    public void reloadPlaylists() {
        playlists = PlaylistLoader.getPlaylists(getActivity(), showAuto);
        playlistcount = playlists.size();

        initPager();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.ACTION_DELETE_PLAYLIST) {
            if (resultCode == Activity.RESULT_OK) {
                reloadPlaylists();
            }

        }
    }
}

