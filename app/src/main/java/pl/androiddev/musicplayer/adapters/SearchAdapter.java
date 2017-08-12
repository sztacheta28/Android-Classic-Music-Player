package pl.androiddev.musicplayer.adapters;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.dialogs.AddPlaylistDialog;
import pl.androiddev.musicplayer.models.Album;
import pl.androiddev.musicplayer.models.Artist;
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.Collections;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ItemHolder> {

    private Activity mContext;
    private List searchResults = Collections.emptyList();

    public SearchAdapter(Activity context) {
        this.mContext = context;

    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                return new ItemHolder(v0);
            case 1:
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_search, null);
                return new ItemHolder(v1);
            case 2:
                View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_artist, null);
                return new ItemHolder(v2);
            case 10:
                View v10 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.search_section_header, null);
                return new ItemHolder(v10);
            default:
                View v3 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
                return new ItemHolder(v3);
        }
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                Song song = (Song) searchResults.get(i);
                itemHolder.title.setText(song.title);
                itemHolder.songartist.setText(song.albumName);
                ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(song.albumId).toString(), itemHolder.albumArt,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(new FadeInBitmapDisplayer(400))
                                .build());
                setOnPopupMenuListener(itemHolder, i);
                break;
            case 1:
                Album album = (Album) searchResults.get(i);
                itemHolder.albumtitle.setText(album.title);
                itemHolder.albumartist.setText(album.artistName);
                ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(album.id).toString(), itemHolder.albumArt,
                        new DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(new FadeInBitmapDisplayer(400))
                                .build());
                break;
            case 2:
                Artist artist = (Artist) searchResults.get(i);
                itemHolder.artisttitle.setText(artist.name);
                String albumNmber = PlayerUtils.makeLabel(mContext, R.plurals.Nalbums, artist.albumCount);
                String songCount = PlayerUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songCount);
                itemHolder.albumsongcount.setText(PlayerUtils.makeCombinedString(mContext, albumNmber, songCount));
                break;
            case 10:
                itemHolder.sectionHeader.setText((String) searchResults.get(i));
            case 3:
                break;
        }
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener((View v)-> {

            final PopupMenu menu = new PopupMenu(mContext, v);
            menu.setOnMenuItemClickListener((MenuItem item)-> {
                long[] song = new long[1];
                song[0] = ((Song) searchResults.get(position)).id;
                switch (item.getItemId()) {
                    case R.id.popup_song_play:
                        MusicPlayer.playAll(mContext, song, 0, -1, PlayerUtils.IdType.NA, false);
                        break;
                    case R.id.popup_song_play_next:
                        MusicPlayer.playNext(mContext, song, -1, PlayerUtils.IdType.NA);
                        break;
                    case R.id.popup_song_goto_album:
                        NavigationUtils.navigateToAlbum(mContext, ((Song) searchResults.get(position)).albumId, null);
                        break;
                    case R.id.popup_song_goto_artist:
                        NavigationUtils.navigateToArtist(mContext, ((Song) searchResults.get(position)).artistId, null);
                        break;
                    case R.id.popup_song_addto_queue:
                        MusicPlayer.addToQueue(mContext, song, -1, PlayerUtils.IdType.NA);
                        break;
                    case R.id.popup_song_addto_playlist:
                        AddPlaylistDialog.newInstance(((Song) searchResults.get(position))).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                        break;
                }
                return false;

            });
            menu.inflate(R.menu.popup_song);
            menu.show();

        });
    }

    @Override
    public int getItemViewType(int position) {
        if (searchResults.get(position) instanceof Song)
            return 0;
        if (searchResults.get(position) instanceof Album)
            return 1;
        if (searchResults.get(position) instanceof Artist)
            return 2;
        if (searchResults.get(position) instanceof String)
            return 10;
        return 3;
    }

    public void updateSearchResults(List searchResults) {
        this.searchResults = searchResults;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, songartist, albumtitle, artisttitle, albumartist, albumsongcount, sectionHeader;
        protected ImageView albumArt, artistImage, menu;

        public ItemHolder(View view) {
            super(view);

            this.title = (TextView) view.findViewById(R.id.song_title);
            this.songartist = (TextView) view.findViewById(R.id.song_artist);
            this.albumsongcount = (TextView) view.findViewById(R.id.album_song_count);
            this.artisttitle = (TextView) view.findViewById(R.id.artist_name);
            this.albumtitle = (TextView) view.findViewById(R.id.album_title);
            this.albumartist = (TextView) view.findViewById(R.id.album_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.artistImage = (ImageView) view.findViewById(R.id.artistImage);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);

            this.sectionHeader = (TextView) view.findViewById(R.id.section_header);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (getItemViewType()) {
                case 0:
                    final Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        long[] ret = new long[1];
                        ret[0] = ((Song) searchResults.get(getAdapterPosition())).id;
                        MusicPlayer.playAll(mContext, ret, 0, -1, PlayerUtils.IdType.NA, false);
                    }, 100);

                    break;
                case 1:
                    NavigationUtils.goToAlbum(mContext, ((Album) searchResults.get(getAdapterPosition())).id);
                    break;
                case 2:
                    NavigationUtils.goToArtist(mContext, ((Artist) searchResults.get(getAdapterPosition())).id);
                    break;
                case 3:
                    break;
                case 10:
                    break;
            }
        }

    }
}





