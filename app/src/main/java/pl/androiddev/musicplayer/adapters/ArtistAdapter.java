package pl.androiddev.musicplayer.adapters;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pl.androiddev.musicplayer.models.Artist;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PreferencesUtility;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import pl.androiddev.musicplayer.widgets.BubbleTextGetter;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ItemHolder> implements BubbleTextGetter {

    private List<Artist> arraylist;
    private Activity mContext;
    private boolean isGrid;

    public ArtistAdapter(Activity context, List<Artist> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid();
    }

    public static int getOpaqueColor(@ColorInt int paramInt) {
        return 0xFF000000 | paramInt;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isGrid) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(pl.androiddev.musicplayer.R.layout.item_artist_grid, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(pl.androiddev.musicplayer.R.layout.item_artist, null);
            ItemHolder ml = new ItemHolder(v);
            return ml;
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        final Artist localItem = arraylist.get(i);

        itemHolder.name.setText(localItem.name);
        String albumNmber = PlayerUtils.makeLabel(mContext, pl.androiddev.musicplayer.R.plurals.Nalbums, localItem.albumCount);
        String songCount = PlayerUtils.makeLabel(mContext, pl.androiddev.musicplayer.R.plurals.Nsongs, localItem.songCount);
        itemHolder.albums.setText(PlayerUtils.makeCombinedString(mContext, albumNmber, songCount));

        if (PlayerUtils.isLollipop())
            itemHolder.artistImage.setTransitionName("transition_artist_art" + i);

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    @Override
    public String getTextToShowInBubble(final int pos) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        return Character.toString(arraylist.get(pos).name.charAt(0));
    }

    public void updateDataSet(List<Artist> arrayList) {
        this.arraylist = arrayList;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView name, albums;
        protected ImageView artistImage;
        protected View footer;

        public ItemHolder(View view) {
            super(view);
            this.name = (TextView) view.findViewById(pl.androiddev.musicplayer.R.id.artist_name);
            this.albums = (TextView) view.findViewById(pl.androiddev.musicplayer.R.id.album_song_count);
            this.artistImage = (ImageView) view.findViewById(pl.androiddev.musicplayer.R.id.artistImage);
            this.footer = view.findViewById(pl.androiddev.musicplayer.R.id.footer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtils.navigateToArtist(mContext, arraylist.get(getAdapterPosition()).id,
                    new Pair<View, String>(artistImage, "transition_artist_art" + getAdapterPosition()));
        }

    }
}




