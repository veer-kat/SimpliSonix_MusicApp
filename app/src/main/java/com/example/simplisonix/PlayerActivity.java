package com.example.simplisonix;

import static com.example.simplisonix.ApplicationClass.ACTION_NEXT;
import static com.example.simplisonix.ApplicationClass.ACTION_PLAY;
import static com.example.simplisonix.ApplicationClass.ACTION_PREVIOUS;
import static com.example.simplisonix.ApplicationClass.CHANNEL_ID_2;
import static com.example.simplisonix.AudioPlayer.musicFiles;
import static com.example.simplisonix.AudioPlayer.repeatBoolean;
import static com.example.simplisonix.AudioPlayer.shuffleBoolean;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection
{

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position= -1;
    static ArrayList<MusicFiles> listSongs= new ArrayList<>();
    static Uri uri;
    //static MediaPlayer mediaPlayer;
    private Handler handler= new Handler();
    public Thread playThread, prevThread, nextThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mediaSessionCompat=new MediaSessionCompat(getBaseContext(), "My Audio");
        initViews();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                boolean fromUser;
                if(musicService!=null && b)
                {
                    musicService.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService!=null)
                {
                    int mCurrentPosition=musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shuffleBoolean)
                {
                    shuffleBoolean=false;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_off);
                }
                else
                {
                    shuffleBoolean=true;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatBoolean)
                {
                    repeatBoolean=false;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_off);
                }
                else
                {
                    repeatBoolean=true;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_on);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Intent resintent=new Intent(this, MusicService.class);
        bindService(resintent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn()
    {
        prevThread=new Thread()
        {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked()
    {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean)
            {
                position=getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position=((position-1) < 0 ? (listSongs.size()-1) : (position-1));
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.baseline_pause);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean)
            {
                position=getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position=((position-1) < 0 ? (listSongs.size()-1) : (position-1));
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.baseline_play_arrow);
            playPauseBtn.setImageResource(R.drawable.baseline_play_arrow);
        }
    }

    private void nextThreadBtn()
    {
        nextThread=new Thread()
        {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked()
    {
        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean)
            {
                position=getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position=((position+1)%listSongs.size());
            }
            //else position will be position...
            uri=Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.baseline_pause);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean)
            {
                position=getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position=((position+1)%listSongs.size());
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metadata(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            musicService.OnCompleted();
            showNotification(R.drawable.baseline_play_arrow);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_play_arrow);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    private void playThreadBtn()
    {
        playThread=new Thread()
        {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked()
    {
        if(musicService.isPlaying())
        {
            showNotification(R.drawable.baseline_play_arrow);
            playPauseBtn.setImageResource(R.drawable.baseline_play_arrow);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else
        {
            showNotification(R.drawable.baseline_pause);
            playPauseBtn.setImageResource(R.drawable.baseline_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService!=null)
                    {
                        int mCurrentPosition=musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }


    private String formattedTime(int mCurrentPosition) {
        String totalout="";
        String totalnew="";
        String seconds=String.valueOf(mCurrentPosition%60);
        String minutes=String.valueOf(mCurrentPosition/60);
        totalout= minutes+":"+seconds;
        totalnew=minutes+":"+"0"+seconds;
        if(seconds.length()==1)
        {
            return totalnew;
        }
        else
        {
            return totalout;
        }
    }

    private void getIntentMethod()
    {
        position=getIntent().getIntExtra("position",-1);
        listSongs= musicFiles;
        if(listSongs!= null)
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause);
            uri=Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.baseline_pause);
        Intent inintent= new Intent(this, MusicService.class);
        inintent.putExtra("servicePosition",position);
        startService(inintent);
    }

    private void initViews()
    {
        song_name=findViewById(R.id.song_name);
        artist_name= findViewById(R.id.song_artist);
        duration_played=findViewById(R.id.durationPlayed);
        duration_total=findViewById(R.id.durationTotal);
        cover_art=findViewById(R.id.cover_art);
        nextBtn= findViewById(R.id.id_next);
        prevBtn=findViewById(R.id.id_prev);
        backBtn=findViewById(R.id.back_btn);
        shuffleBtn= findViewById(R.id.id_shuffle);
        repeatBtn=findViewById(R.id.id_repeat);
        playPauseBtn=findViewById(R.id.play_pause);
        seekBar= findViewById(R.id.seekBar);
    }

    private void metadata(Uri uri)
    {
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal= Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art= retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art!=null)
        {

            bitmap = BitmapFactory.decodeByteArray(art, 0,art.length);
            ImageAnimation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener()
            {
                @Override
                public void onGenerated(@Nullable Palette palette)
                {
                    Palette.Swatch swatch= palette.getDominantSwatch();
                    if(swatch!=null)
                    {
                        ImageView gredient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.simplisonix_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(),0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }
                    else
                    {
                        ImageView gredient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gredient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.simplisonix_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000,0x00000000});
                        gredient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else
        {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.defaultart)
                    .into(cover_art);
            ImageView gredient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gredient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.simplisonix_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }

    }

    public void ImageAnimation(Context context,ImageView imageView, Bitmap bitmap)
    {
        Animation animOut= AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn= AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder= (MusicService.MyBinder) iBinder;
        musicService=myBinder.getService();
        Toast.makeText(this,"Connected " +musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metadata(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.OnCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService=null;
    }

    void showNotification(int playPauseBtn)
    {
        Intent notiintent=new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,notiintent, PendingIntent.FLAG_MUTABLE);

        Intent prevIntent=new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent=new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent=new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent.getBroadcast(this,0,nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        try {
            picture = getAlbumArt(musicFiles.get(position).getPath());
            Bitmap thumb=null;
            if(picture !=null)
            {
                thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
            }
            else
            {
                thumb= BitmapFactory.decodeResource(getResources(),R.drawable.defaultart);
            }
            Notification notification= new NotificationCompat.Builder(this, CHANNEL_ID_2)
                    .setSmallIcon(playPauseBtn)
                    .setLargeIcon(thumb)
                    .setContentTitle(musicFiles.get(position).getTitle())
                    .setContentText(musicFiles.get(position).getArtist())
                    .addAction(R.drawable.baseline_skip_previous, "Previous", prevPending)
                    .addAction(playPauseBtn, "Pause", pausePending)
                    .addAction(R.drawable.baseline_skip_next, "Next", nextPending)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
                    .build();
            NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0,notification);
        } catch (IOException e) {
            throw new RuntimeException(e);
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