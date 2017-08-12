package pl.androiddev.musicplayer.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.adapters.SongsListAdapter;
import pl.androiddev.musicplayer.dataloaders.LastAddedLoader;
import pl.androiddev.musicplayer.dataloaders.PlaylistLoader;
import pl.androiddev.musicplayer.dataloaders.PlaylistSongLoader;
import pl.androiddev.musicplayer.dataloaders.SongLoader;
import pl.androiddev.musicplayer.dataloaders.TopTracksLoader;
import pl.androiddev.musicplayer.listeners.SimplelTransitionListener;
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.utils.Constants;
import pl.androiddev.musicplayer.utils.PreferencesUtility;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import pl.androiddev.musicplayer.widgets.DividerItemDecoration;
import pl.androiddev.musicplayer.widgets.DragSortRecycler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.*;

public class PlaylistDetailActivity extends BaseActivity implements ATEActivityThemeCustomizer, ATEToolbarCustomizer {
    String action;
    long playlistID;
    HashMap<String, Runnable> playlistsMap = new HashMap<>();
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.blurFrame) ImageView blurFrame;
    @BindView(R.id.name) TextView playlistname;
    @BindView(R.id.foreground) View foreground;
    private AppCompatActivity mContext = PlaylistDetailActivity.this;
    private SongsListAdapter mAdapter;

    private boolean animate;

    Runnable playlistLastAdded = new Runnable() {
        public void run() {
            Observable<String> source = Observable.fromCallable(() -> {
                List<Song> lastadded = LastAddedLoader.getLastAddedSongs(mContext);
                mAdapter = new SongsListAdapter(mContext, lastadded, true, animate);
                mAdapter.setPlaylistId(playlistID);
                return "Executed";
            });

            Observable<String> sourceSubOn = source.subscribeOn(Schedulers.io());
            Observable<String> sourceObserveOn = sourceSubOn.observeOn(AndroidSchedulers.mainThread());
            Disposable sourceSubscribe = sourceObserveOn.subscribe((result) -> {
                setRecyclerViewAapter();
            });
        }
    };
    Runnable playlistRecents = new Runnable() {
        @Override
        public void run() {
            Observable<String> source = Observable.fromCallable(() -> {
                TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.RecentSongs);
                List<Song> recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
                mAdapter = new SongsListAdapter(mContext, recentsongs, true, animate);
                mAdapter.setPlaylistId(playlistID);
                return "Executed";
            });

            Observable<String> sourceSubOn = source.subscribeOn(Schedulers.io());
            Observable<String> sourceObserveOn = sourceSubOn.observeOn(AndroidSchedulers.mainThread());
            Disposable sourceSubscribe = sourceObserveOn.subscribe((result) -> {
                setRecyclerViewAapter();
            });
        }
    };
    Runnable playlistToptracks = new Runnable() {
        @Override
        public void run() {
            Observable<String> source = Observable.fromCallable(() -> {
                TopTracksLoader loader = new TopTracksLoader(mContext, TopTracksLoader.QueryType.TopTracks);
                List<Song> toptracks = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
                mAdapter = new SongsListAdapter(mContext, toptracks, true, animate);
                mAdapter.setPlaylistId(playlistID);
                return "Executed";
            });

            Observable<String> sourceSubOn = source.subscribeOn(Schedulers.io());
            Observable<String> sourceObserveOn = sourceSubOn.observeOn(AndroidSchedulers.mainThread());
            Disposable sourceSubscribe = sourceObserveOn.subscribe((result) -> {
                setRecyclerViewAapter();
            });
        }
    };
    Runnable playlistUsercreated = new Runnable() {
        @Override
        public void run() {
            Observable<String> source = Observable.fromCallable(() -> {
                playlistID = getIntent().getExtras().getLong(Constants.PLAYLIST_ID);
                List<Song> playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, playlistID);
                mAdapter = new SongsListAdapter(mContext, playlistsongs, true, animate);
                mAdapter.setPlaylistId(playlistID);
                return "Executed";
            });

            Observable<String> sourceSubOn = source.subscribeOn(Schedulers.io());
            Observable<String> sourceObserveOn = sourceSubOn.observeOn(AndroidSchedulers.mainThread());
            Disposable sourceSubscribe = sourceObserveOn.subscribe((result) -> {
                setRecyclerViewAapter();
            });
        }
    };

    @TargetApi(21)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        ButterKnife.bind(this);

        action = getIntent().getAction();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_LASTADDED, playlistLastAdded);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_RECENT, playlistRecents);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_TOPTRACKS, playlistToptracks);
        playlistsMap.put(Constants.NAVIGATE_PLAYLIST_USERCREATED, playlistUsercreated);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setAlbumart();

        animate = getIntent().getBooleanExtra(Constants.ACTIVITY_TRANSITION, false);
        if (animate && PlayerUtils.isLollipop() && PreferencesUtility.getInstance(this).getAnimations()) {
            getWindow().getEnterTransition().addListener(new EnterTransitionListener());
        } else {
            setUpSongs();
        }
    }

    private void setAlbumart() {
        playlistname.setText(getIntent().getExtras().getString(Constants.PLAYLIST_NAME));
        foreground.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), getIntent().getExtras().getInt(Constants.PLAYLIST_FOREGROUND_COLOR)));
        loadBitmap(PlayerUtils.getAlbumArtUri(getIntent().getExtras().getLong(Constants.ALBUM_ID)).toString());
    }

    private void setUpSongs() {
        Runnable navigation = playlistsMap.get(action);
        if (navigation != null) {
            navigation.run();

            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.reorder);

            dragSortRecycler.setOnItemMovedListener((from, to) -> {
                Log.d("playlist", "onItemMoved " + from + " to " + to);
                Song song = mAdapter.getSongAt(from);
                mAdapter.removeSongAt(from);
                mAdapter.addSongTo(to, song);
                mAdapter.notifyDataSetChanged();
                MediaStore.Audio.Playlists.Members.moveItem(getContentResolver(),
                        playlistID, from, to);
            });

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());

        } else {
            Log.d("PlaylistDetail", "mo action specified");
        }
    }

    private void loadBitmap(String uri) {
        ImageLoader.getInstance().displayImage(uri, blurFrame,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    private void setRecyclerViewAapter() {
        recyclerView.setAdapter(mAdapter);
        if (animate && PlayerUtils.isLollipop() && PreferencesUtility.getInstance(mContext).getAnimations()) {
            Handler handler = new Handler();
            handler.postDelayed(() -> recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white)), 250);
        } else
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white));
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return R.style.AppTheme_FullScreen_Dark;

    }

    private class EnterTransitionListener extends SimplelTransitionListener {

        @TargetApi(21)
        public void onTransitionEnd(Transition paramTransition) {
            setUpSongs();
        }

        public void onTransitionStart(Transition paramTransition) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (action.equals(Constants.NAVIGATE_PLAYLIST_USERCREATED)) {
            menu.findItem(R.id.action_delete_playlist).setVisible(true);
            menu.findItem(R.id.action_clear_auto_playlist).setVisible(false);
        } else {
            menu.findItem(R.id.action_delete_playlist).setVisible(false);
            menu.findItem(R.id.action_clear_auto_playlist).setTitle("Clear " + playlistname.getText().toString());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_delete_playlist:
                showDeletePlaylistDialog();
                break;
            case R.id.action_clear_auto_playlist:
                clearAutoPlaylists();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletePlaylistDialog() {
        new MaterialDialog.Builder(this)
                .title("Delete playlist?")
                .content("Are you sure you want to delete playlist " + playlistname.getText().toString() + " ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive((dialog, which) -> {
                    PlaylistLoader.deletePlaylists(PlaylistDetailActivity.this, playlistID);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearAutoPlaylists() {
        switch (action) {
            case Constants.NAVIGATE_PLAYLIST_LASTADDED:
                PlayerUtils.clearLastAdded(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_RECENT:
                PlayerUtils.clearRecent(this);
                break;
            case Constants.NAVIGATE_PLAYLIST_TOPTRACKS:
                PlayerUtils.clearTopTracks(this);
                break;
        }
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public int getToolbarColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getLightToolbarMode() {
        return Config.LIGHT_TOOLBAR_AUTO;
    }
}
