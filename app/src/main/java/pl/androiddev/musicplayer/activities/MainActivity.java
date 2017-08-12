package pl.androiddev.musicplayer.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.fragments.AlbumDetailFragment;
import pl.androiddev.musicplayer.fragments.ArtistDetailFragment;
import pl.androiddev.musicplayer.fragments.FoldersFragment;
import pl.androiddev.musicplayer.fragments.MainFragment;
import pl.androiddev.musicplayer.fragments.PlaylistFragment;
import pl.androiddev.musicplayer.fragments.QueueFragment;
import pl.androiddev.musicplayer.permissions.Nammu;
import pl.androiddev.musicplayer.permissions.PermissionCallback;
import pl.androiddev.musicplayer.slidinguppanel.SlidingUpPanelLayout;
import pl.androiddev.musicplayer.utils.Constants;
import pl.androiddev.musicplayer.utils.Helpers;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import shortbread.Shortcut;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer {
    private static MainActivity sMainActivity;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout panelLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    TextView songtitle, songartist;
    ImageView albumart;
    String action;
    Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    Handler navDrawerRunnable = new Handler();
    Runnable runnable;
    Runnable navigateLibrary = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_library).setChecked(true);
            Fragment fragment = new MainFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();

        }
    };
    Runnable navigateNowplaying = () -> {
        navigateLibrary.run();
        startActivity(new Intent(MainActivity.this, NowPlayingActivity.class));
    };
    final PermissionCallback permissionReadstorageCallback = new PermissionCallback() {
        @Override
        public void permissionGranted() {
            loadEverything();
        }

        @Override
        public void permissionRefused() {
            finish();
        }
    };
    Runnable navigatePlaylist = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_playlists).setChecked(true);
            Fragment fragment = new PlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    Runnable navigateFolder = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_folders).setChecked(true);
            Fragment fragment = new FoldersFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    Runnable navigateQueue = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.nav_queue).setChecked(true);
            Fragment fragment = new QueueFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.replace(R.id.fragment_container, fragment).commit();

        }
    };
    Runnable navigateAlbum = () -> {
        long albumID = getIntent().getExtras().getLong(Constants.ALBUM_ID);
        Fragment fragment = AlbumDetailFragment.newInstance(albumID, false, null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    };
    Runnable navigateArtist = () -> {
        long artistID = getIntent().getExtras().getLong(Constants.ARTIST_ID);
        Fragment fragment = ArtistDetailFragment.newInstance(artistID, false, null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    };

    @Shortcut(id = "tracks", icon = R.drawable.library_music , shortLabel = "Tracks", rank = 1)
    public void loadTracks(){
        runnable = navigateLibrary;

        Handler handler = new Handler();
        handler.postDelayed(() -> runnable.run(), 350);
    }

    @Shortcut(id = "nowPlaying", icon = R.drawable.library_music , shortLabel = "Now Playing", rank = 2)
    public void loadNowPlaying(){

    }

    @Shortcut(id = "playlists", icon = R.drawable.library_music , shortLabel = "Playlists", rank = 3)
    public void loadPlaylists(){
        runnable = navigatePlaylist;

        Handler handler = new Handler();
        handler.postDelayed(() -> runnable.run(), 350);
    }

    @Shortcut(id = "settings", icon = R.drawable.library_music , shortLabel = "Settings", rank = 4)
    public void loadSettings(){
        NavigationUtils.navigateToSettings(MainActivity.this);
    }

    public static MainActivity getInstance() {
        return sMainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        sMainActivity = this;
        action = getIntent().getAction();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        navigationMap.put(Constants.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(Constants.NAVIGATE_NOWPLAYING, navigateNowplaying);
        navigationMap.put(Constants.NAVIGATE_PLAYLIST, navigatePlaylist);
        navigationMap.put(Constants.NAVIGATE_QUEUE, navigateQueue);
        navigationMap.put(Constants.NAVIGATE_ALBUM, navigateAlbum);
        navigationMap.put(Constants.NAVIGATE_ARTIST, navigateArtist);

        View header = navigationView.inflateHeaderView(R.layout.nav_header);

        albumart = (ImageView) header.findViewById(R.id.album_art);
        songtitle = (TextView) header.findViewById(R.id.song_title);
        songartist = (TextView) header.findViewById(R.id.song_artist);

        setPanelSlideListeners(panelLayout);

        navDrawerRunnable.postDelayed(() -> {
            setupDrawerContent(navigationView);
            setupNavigationIcons(navigationView);
        }, 700);


        if (PlayerUtils.isMarshmallow()) {
            checkPermissionAndThenLoad();
        } else {
            loadEverything();
        }

        addBackstackListener();

        if(Intent.ACTION_VIEW.equals(action)) {
            if (getIntent().getData() != null) {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    MusicPlayer.clearQueue();
                    MusicPlayer.openFile(getIntent().getData().getPath());
                    MusicPlayer.playOrPause();
                    navigateNowplaying.run();
                }, 350);
            }
        }

    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
            NavigationUtils.navigateToNowplaying(MainActivity.this, false);
        }

        new initQuickControls().execute("");
    }

    private void checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            loadEverything();
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout, "Application will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> Nammu.askForPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback)).show();
            } else {
                Nammu.askForPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, permissionReadstorageCallback);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isNavigatingMain()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else super.onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (panelLayout.isPanelExpanded()) {
            panelLayout.collapsePanel();
        } else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    updatePosition(menuItem);
                    return true;

                });
    }

    private void setupNavigationIcons(NavigationView navigationView) {
        navigationView.getMenu().findItem(R.id.nav_library).setIcon(R.drawable.library_music_white);
        navigationView.getMenu().findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play_white);
        navigationView.getMenu().findItem(R.id.nav_queue).setIcon(R.drawable.music_note_white);
        navigationView.getMenu().findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_white_24dp);
        navigationView.getMenu().findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music_white);
        navigationView.getMenu().findItem(R.id.nav_settings).setIcon(R.drawable.settings_white);
        navigationView.getMenu().findItem(R.id.nav_about).setIcon(R.drawable.information_white);
    }

    private void updatePosition(final MenuItem menuItem) {
        runnable = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_library:
                runnable = navigateLibrary;
                break;
            case R.id.nav_playlists:
                runnable = navigatePlaylist;
                break;
            case R.id.nav_folders:
                runnable = navigateFolder;
                break;
            case R.id.nav_nowplaying:
                NavigationUtils.navigateToNowplaying(MainActivity.this, false);
                break;
            case R.id.nav_queue:
                runnable = navigateQueue;
                break;
            case R.id.nav_settings:
                NavigationUtils.navigateToSettings(MainActivity.this);
                break;
            case R.id.nav_about:
                mDrawerLayout.closeDrawers();
                Handler handler = new Handler();
                handler.postDelayed(() -> Helpers.showAbout(MainActivity.this), 350);
                break;
        }

        if (runnable != null) {
            menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();
            Handler handler = new Handler();
            handler.postDelayed(() -> runnable.run(), 350);
        }
    }

    public void setDetailsToHeader() {
        String name = MusicPlayer.getTrackName();
        String artist = MusicPlayer.getArtistName();

        if (name != null && artist != null) {
            songtitle.setText(name);
            songartist.setText(artist);
        }
        ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                new DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build());
    }

    @Override
    public void onMetaChanged() {
        super.onMetaChanged();
        setDetailsToHeader();
    }

    @Override
    public void onResume() {
        super.onResume();
        sMainActivity = this;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isNavigatingMain() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        return (currentFragment instanceof MainFragment || currentFragment instanceof QueueFragment
                || currentFragment instanceof PlaylistFragment || currentFragment instanceof FoldersFragment);
    }

    private void addBackstackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> getSupportFragmentManager().findFragmentById(R.id.fragment_container).onResume());
    }


    @Override
    public int getActivityTheme() {
        return R.style.AppThemeNormalDark;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
    }
}


