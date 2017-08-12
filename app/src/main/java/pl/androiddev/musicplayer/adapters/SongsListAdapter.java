package pl.androiddev.musicplayer.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import pl.androiddev.musicplayer.utils.PreferencesUtility;
import pl.androiddev.musicplayer.utils.PlayerUtils;
import pl.androiddev.musicplayer.widgets.BubbleTextGetter;
import pl.androiddev.musicplayer.widgets.MusicVisualizer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ItemHolder> implements BubbleTextGetter {

    public int currentlyPlayingPosition;
    private List<Song> arraylist;
    private AppCompatActivity mContext;
    private long[] songIDs;
    private boolean isPlaylist;
    private boolean animate;
    private int lastPosition = -1;
    private String ateKey;
    private long playlistId;

    public SongsListAdapter(AppCompatActivity context, List<Song> arraylist, boolean isPlaylistSong, boolean animate) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.isPlaylist = isPlaylistSong;
        this.songIDs = getSongIds();
        this.ateKey = Helpers.getATEKey(context);
        this.animate = animate;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (isPlaylist) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_playlist, null);
            return new ItemHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song, null);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {
        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.artist.setText(localItem.artistName);

        ImageLoader.getInstance().displayImage(PlayerUtils.getAlbumArtUri(localItem.albumId).toString(), itemHolder.albumArt, new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build());
        if (MusicPlayer.getCurrentAudioId() == localItem.id) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey));
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey));
                itemHolder.visualizer.setVisibility(View.VISIBLE);
            }
        } else {
            if (isPlaylist)
                itemHolder.title.setTextColor(Color.WHITE);
            else
                itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey));
            itemHolder.visualizer.setVisibility(View.GONE);
        }


        if (animate && isPlaylist && PreferencesUtility.getInstance(mContext).getAnimations()) {
            if (PlayerUtils.isLollipop())
                setAnimation(itemHolder.itemView, i);
            else {
                if (i > 10)
                    setAnimation(itemHolder.itemView, i);
            }
        }


        setOnPopupMenuListener(itemHolder, i);

    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
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
                    case R.id.popup_song_remove_playlist:
                        PlayerUtils.removeFromPlaylist(mContext, arraylist.get(position).id, playlistId);
                        removeSongAt(position);
                        notifyItemRemoved(position);
                        break;
                    case R.id.popup_song_play:
                        MusicPlayer.playAll(mContext, songIDs, position, -1, PlayerUtils.IdType.NA, false);
                        break;
                    case R.id.popup_song_play_next:
                        long[] ids = new long[1];
                        ids[0] = arraylist.get(position).id;
                        MusicPlayer.playNext(mContext, ids, -1, PlayerUtils.IdType.NA);
                        break;
                    case R.id.popup_song_goto_album:
                        NavigationUtils.goToAlbum(mContext, arraylist.get(position).albumId);
                        break;
                    case R.id.popup_song_goto_artist:
                        NavigationUtils.goToArtist(mContext, arraylist.get(position).artistId);
                        break;
                    case R.id.popup_song_addto_queue:
                        long[] id = new long[1];
                        id[0] = arraylist.get(position).id;
                        MusicPlayer.addToQueue(mContext, id, -1, PlayerUtils.IdType.NA);
                        break;
                    case R.id.popup_song_addto_playlist:
                        AddPlaylistDialog.newInstance(arraylist.get(position)).show(mContext.getSupportFragmentManager(), "ADD_PLAYLIST");
                        break;
                    case R.id.popup_song_share:
                       PlayerUtils.shareTrack(mContext, arraylist.get(position).id);
                        break;
                    case R.id.popup_song_delete:
                        long[] deleteIds = {arraylist.get(position).id};
                        PlayerUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, SongsListAdapter.this, position);
                        break;
                }
                return false;
            });
            menu.inflate(R.menu.popup_song);
            menu.show();
            if (isPlaylist)
                menu.getMenu().findItem(R.id.popup_song_remove_playlist).setVisible(true);
        });
    }

    public long[] getSongIds() {
        long[] ret = new long[getItemCount()];
        for (int i = 0; i < getItemCount(); i++) {
            ret[i] = arraylist.get(i).id;
        }

        return ret;
    }

    @Override
    public String getTextToShowInBubble(final int pos) {
        if (arraylist == null || arraylist.size() == 0)
            return "";
        Character ch = arraylist.get(pos).title.charAt(0);
        if (Character.isDigit(ch)) {
            return "#";
        } else
            return Character.toString(ch);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_in_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist;
        protected ImageView albumArt, popupMenu;
        private MusicVisualizer visualizer;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.albumArt = (ImageView) view.findViewById(R.id.albumArt);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            visualizer = (MusicVisualizer) view.findViewById(R.id.visualizer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayer.playAll(mContext, songIDs, getAdapterPosition(), -1, PlayerUtils.IdType.NA, false);
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    notifyItemChanged(currentlyPlayingPosition);
                    notifyItemChanged(getAdapterPosition());
                }, 50);
            }, 100);


        }

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
}


