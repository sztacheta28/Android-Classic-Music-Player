package pl.androiddev.musicplayer.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.dataloaders.ArtistLoader;
import pl.androiddev.musicplayer.models.Artist;
import pl.androiddev.musicplayer.utils.ATEUtils;
import pl.androiddev.musicplayer.utils.Constants;
import pl.androiddev.musicplayer.utils.Helpers;
import pl.androiddev.musicplayer.utils.ImageUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ArtistDetailFragment extends Fragment {

    long artistID = -1;

    ImageView artistArt;

    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    boolean largeImageLoaded = false;
    int primaryColor = -1;

    public static ArtistDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong(Constants.ARTIST_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_artist_detail, container, false);

        artistArt = (ImageView) rootView.findViewById(R.id.artist_art);

        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar);

        if (getArguments().getBoolean("transition")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                artistArt.setTransitionName(getArguments().getString("transition_name"));
            }
        }

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        setupToolbar();
        setUpArtistDetails();

        getChildFragmentManager().beginTransaction().replace(R.id.container, ArtistMusicFragment.newInstance(artistID)).commit();


        return rootView;
    }

    private void setupToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpArtistDetails() {
        final Artist artist = ArtistLoader.getArtist(getActivity(), artistID);

        collapsingToolbarLayout.setTitle(artist.name);
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (primaryColor != -1 && getActivity() != null) {
            collapsingToolbarLayout.setContentScrimColor(primaryColor);
            String ateKey = Helpers.getATEKey(getActivity());
            ATEUtils.setStatusBarColor(getActivity(), ateKey, primaryColor);
        }
    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null && !largeImageLoaded) {
                artistArt.setImageDrawable(result);
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

}