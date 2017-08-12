package pl.androiddev.musicplayer.adapters;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import pl.androiddev.musicplayer.MusicPlayer;
import pl.androiddev.musicplayer.dialogs.AddPlaylistDialog;
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.utils.Helpers;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import pl.androiddev.musicplayer.widgets.MusicVisualizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class BaseQueueAdapter extends RecyclerView.Adapter<BaseQueueAdapter.ItemHolder> {

    public static int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private String ateKey;

    public BaseQueueAdapter(AppCompatActivity context, List<Song> arraylist) {
        this.arraylist = arraylist;
        this.mContext = context;
        currentlyPlayingPosition = MusicPlayer.getQueuePosition();
        this.ateKey = Helpers.getATEKey(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(pl.androiddev.musicplayer.R.layout.item_song_nowplaying, null);
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
        ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.albumArt, new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(pl.androiddev.musicplayer.R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build());
        setOnPopupMenuListener(itemHolder, i);
    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.popupMenu.setOnClickListener(v -> {

            final PopupMenu menu = new PopupMenu(mContext, v);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case pl.androiddev.musicplayer.R.id.popup_song_play:
                        MusicPlayer.playAll(mContext, getSongIds(), position, -1, PlayerUtils.IdType.NA, false);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_play_next:
                        long[] ids = new long[1];
                        ids[0] = arraylist.get(position).id;
                        MusicPlayer.playNext(mContext, ids, -1, PlayerUtils.IdType.NA);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_goto_album:
                        NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumId);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_goto_artist:
                        NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_addto_queue:
                        long[] id = new long[1];
                        id[0] = arraylist.get(position).id;
                        MusicPlayer.addToQueue(mContext, id, -1, PlayerUtils.IdType.NA);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_addto_playlist:
                        AddPlaylistDialog.newInstance(arraylist.get(position)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_share:
                        PlayerUtils.shareTrack(mContext, arraylist.get(position).id);
                        break;
                    case pl.androiddev.musicplayer.R.id.popup_song_delete:
                        long[] deleteIds = {arraylist.get(position).id};
                        PlayerUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, BaseQueueAdapter.this, position);
                        break;
                }
                return false;
            });
            menu.inflate(pl.androiddev.musicplayer.R.menu.popup_song);
            menu.show();
        });
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private MusicVisualizer visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(pl.androiddev.musicplayer.R.id.song_title);
            this.artist = (TextView) view.findViewById(pl.androiddev.musicplayer.R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(pl.androiddev.musicplayer.R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(pl.androiddev.musicplayer.R.id.popup_menu);
            visualizer = (MusicVisualizer) view.findViewById(pl.androiddev.musicplayer.R.id.visualizer);
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



