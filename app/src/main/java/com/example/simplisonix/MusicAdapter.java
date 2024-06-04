package com.example.simplisonix;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyVieHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles)
    {
        this.mFiles=mFiles;
        this.mContext=mContext;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items,parent,false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        try {
            byte[] image= getAlbumArt(mFiles.get(position).getPath());
            if(image!=null)
            {
                Glide.with(mContext).asBitmap()
                        .load(image)
                        .into(holder.album_art);
            }
            else
            {
                Glide.with(mContext)
                        .load(R.drawable.defaultart)
                        .into(holder.album_art);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent= new Intent(mContext, PlayerActivity.class);
                    intent.putExtra("position", position);
                    mContext.startActivity(intent);
                }
            });
            holder.menuMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    PopupMenu popupMenu=new PopupMenu(mContext,view);
                    popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.delete)
                            {
                                Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                                deleteFile(position, view);
                            }
                            return true;
                        }
                    });
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteFile(int position, View view)
    {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getId()));
        File file=new File(mFiles.get(position).getPath());
        boolean deleted = file.delete(); //delete your file
        if (deleted)
        {
            mContext.getContentResolver().delete(contentUri,null,null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File Deleted ", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            // may be in sd card
            Snackbar.make(view,"Can't be deleted",Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder
    {
        TextView file_name;
        ImageView album_art, menuMore;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name= itemView.findViewById(R.id.music_file_name);
            album_art= itemView.findViewById(R.id.music_img);
            menuMore= itemView.findViewById(R.id.menuMore);
        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
