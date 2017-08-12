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

import com.afollestad.appthemeengine.Config;
import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.R;
import pl.androiddev.musicplayer.dialogs.AddPlaylistDialog;
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.utils.Helpers;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import pl.androiddev.musicplayer.widgets.MusicVisualizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class PlayingQueueAdapter extends RecyclerView.Adapter<PlayingQueueAdapter.ItemHolder> {

    public int currentlyPlayingPosition;
    private List<Song> arraylist;
    private Activity mContext;
    private String ateKey;

    public PlayingQueueAdapter(Activity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.currentlyPlayingPosition = MusicPlayer.getQueuePosition();
        this.ateKey = Helpers.getATEKey(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playing_queue, null);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        if (MusicPlayer.getCurrentAudioId() == localItem.id) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            }
        } else {
            itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
            itemHolder.visualizer.setVisibility(View.GONE);
        }
        ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.albumArt, new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build());
        setOnPopupMenuListener(itemHolder, i);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(v -> {

            final PopupMenu menu = new PopupMenu(mContext, v);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.popup_song_play:
                        MusicPlayer.playAll(mContext, getSongIds(), position, -1, PlayerUtils.IdType.NA, false);
                        break;
                    case R.id.popup_song_goto_album:
                        NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumId);
                        break;
                    case R.id.popup_song_goto_artist:
                        NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                        break;
                    case R.id.popup_song_addto_playlist:
                        AddPlaylistDialog.newInstance(arraylist.get(position)).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                        break;
                    case R.id.popup_song_delete:
                        long[] deleteIds = {arraylist.get(position).id};
                        PlayerUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, PlayingQueueAdapter.this,position);
                        break;
                }
                return false;
            });
            menu.inflate(R.menu.popup_playing_queue);
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    public Song getSongAt(int i) {
        return arraylist.get(i);
    }

    public void addSongTo(int i, Song song) {
        arraylist.add(i, song);
    }

    public void removeSongAt(int i) {
        arraylist.remove(i);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, reorder, menu;
        private MusicVisualizer visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            this.reorder = (ImageView) view.findViewById(R.id.reorder);
            visualizer = (MusicVisualizer) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayer.setQueuePosition(getAdapterPosition());
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    notifyItemChanged(currentlyPlayingPosition);
                    notifyItemChanged(getAdapterPosition());
                }, 50);
            }, 100);

        }

    }

}




