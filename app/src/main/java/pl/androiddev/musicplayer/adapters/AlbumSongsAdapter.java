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
import pl.androiddev.musicplayer.models.Song;
import pl.androiddev.musicplayer.utils.NavigationUtils;
import pl.androiddev.musicplayer.utils.PlayerUtils;

import java.util.List;

public class AlbumSongsAdapter extends RecyclerView.Adapter<AlbumSongsAdapter.ItemHolder> {

    private List<Song> arraylist;
    private Activity mContext;
    private long albumID;
    private long[] songIDs;

    public AlbumSongsAdapter(Activity context, List<Song> arraylist, long albumID) {
        this.arraylist = arraylist;
        this.mContext = context;
        this.songIDs = getSongIds();
        this.albumID = albumID;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album_song, null);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder itemHolder, int i) {

        Song localItem = arraylist.get(i);

        itemHolder.title.setText(localItem.title);
        itemHolder.duration.setText(PlayerUtils.makeShortTimeString(mContext, (localItem.duration) / 1000));
        int tracknumber = localItem.trackNumber;
        if (tracknumber == 0) {
            itemHolder.trackNumber.setText("-");
        } else itemHolder.trackNumber.setText(String.valueOf(tracknumber));

        setOnPopupMenuListener(itemHolder, i);
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(v -> {

            final PopupMenu menu = new PopupMenu(mContext, v);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
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
                        AddPlaylistDialog.newInstance(arraylist.get(position)).show(((AppCompatActivity) mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                        break;
                    case R.id.popup_song_share:
                        PlayerUtils.shareTrack(mContext, arraylist.get(position).id);
                        break;
                    case R.id.popup_song_delete:
                        long[] deleteIds = {arraylist.get(position).id};
                        PlayerUtils.showDeleteDialog(mContext,arraylist.get(position).title, deleteIds, AlbumSongsAdapter.this, position);
                        break;
                }
                return false;
            });
            menu.inflate(R.menu.popup_song);
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

    public void updateDataSet(List<Song> arraylist) {
        this.arraylist = arraylist;
        this.songIDs = getSongIds();
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, duration, trackNumber;
        protected ImageView menu;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.duration = (TextView) view.findViewById(R.id.song_duration);
            this.trackNumber = (TextView) view.findViewById(R.id.trackNumber);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayer.playAll(mContext, songIDs, getAdapterPosition(), albumID, PlayerUtils.IdType.Album, false);
                NavigationUtils.navigateToNowplaying(mContext, true);
            }, 100);

        }

    }

}



