package com.blood.bloodplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadata;
import android.media.MediaSession2;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerActivity extends Activity implements StyledPlayerControlView.VisibilityListener {
    String  POSITION = "",
            NAME = "";
    Uri videoUri;
    public StyledPlayerView playerView;
    boolean homePressed = false,
            changeEpisode = false,
            timeSkip = false,
            shouldShowControls = false;
    private static final long DOUBLE_TAP_TIME_WINDOW = 1000;
    private long lastTapTime = 0;
    private View decorView;
    private int doubleTap = 0, x, y;
    CardView nextButton,
            skipIntroBtn;
    TextView nextButtonText,
            skipBtntext;
    LinearLayout nextButton2,
            prevButton;
    protected ExoPlayer player;
    Handler handler;
    private Runnable updateRunnable;
    TextView videoTitle;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        changeEpisode = false;
        playerView = findViewById(R.id.playerView2);
        videoTitle = findViewById (R.id.videoTitle);
        nextButton2 = findViewById(R.id.nextBtn2);
        prevButton = findViewById(R.id.prevBtn);
        nextButtonText = (TextView) findViewById(R.id.nextBtnText);
        skipBtntext = findViewById(R.id.skipBtnText);
        skipIntroBtn = findViewById(R.id.skipIntroBtn);
        nextButton = findViewById(R.id.nextBtn);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setVisibility(View.INVISIBLE);
                stopPlayer();
                saveData();
                finish();
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setVisibility(View.INVISIBLE);
                stopPlayer();
                saveData();
                finish();
            }
        });
        nextButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayer();
                saveData();
                finish();
            }
        });
        skipIntroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipIntroBtn.setVisibility(View.INVISIBLE);
                player.seekTo(122500);
            }
        });
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(i -> {
            if (i == 0) {
                decorView.setSystemUiVisibility(hideSystemBars());
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private int hideSystemBars() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        homePressed = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        videoUri = intent.getData();

        if (videoUri != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("Blood", MODE_PRIVATE);
            NAME = getVideoFileName(videoUri);
            POSITION = sharedPreferences.getString(NAME, "");
            if(!POSITION.equals("")){
                timeSkip = true;
            } else {
                timeSkip = false;
            }
            initializePlayer(videoUri);
        }
//        videoTitle.setText("Episode " + episode);
        //initialize Player then send url and position of episode to player


    }

    @Override
    public void onStop() {
        stopPlayer();
        saveData();
        super.onStop();
        // Add your onStop logic here
    }

    public void onDestroy() {
        stopPlayer();
        saveData();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        stopPlayer();
        saveData();
        super.onPause();
    }


    public void initializePlayer(Uri video) {
        homePressed = false;
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        AtomicReference<MediaItem> mediaItem = new AtomicReference<>(new MediaItem.Builder()
                .setUri(video)
//                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build());
        player.setMediaItem(mediaItem.get());
        player.prepare();
        player.setPlayWhenReady(true);


        if(timeSkip){
            player.seekTo(Integer.parseInt(POSITION));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                x = (int)event.getX();
//                y = (int)event.getY();
//                playerView.setUseController(false);
                return false;
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;


                double t1 = width*0.4;
                double t2 = width*0.4;
                t1 = width-t2;

                if(x>t1){
//                    playerView.setUseController(false);
                    doubleTap = doubleTap + 1;
                    if(doubleTap == 2) {
//                        playerView.setUseController(false);
                        doubleTap = 0;
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastTapTime <= DOUBLE_TAP_TIME_WINDOW) {
                            player.seekForward();
                            Log.d("Tracking: ", "ffwd");
//                            playerView.setUseController(true);
                        } else {
                            playerView.setUseController(true);
                            playerView.showController();
                        }
                        lastTapTime = currentTime;
                    } else {
                        Log.d("Tracking: ", "not ffwd");
//                        playerView.setUseController(true);
//                        playerView.showController();
                    }

                } else if(x<t2){
//                    playerView.setUseController(false);
                    doubleTap = doubleTap + 1;
                    if(doubleTap == 2) {
//                        playerView.setUseController(false);
                        doubleTap = 0;
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastTapTime <= DOUBLE_TAP_TIME_WINDOW) {
                            Log.d("Tracking: ", "prev");
                            player.seekBack();
//                            playerView.setUseController(true);
                        } else {
                            playerView.setUseController(true);
                            playerView.showController();
                        }
                        lastTapTime = currentTime;
//                        playerView.setUseController(true);
                    } else {
//                        playerView.setUseController(true);
//                        playerView.showController();
                    }
                }
            }
        });

        player.addListener(
                new Player.Listener() {
                    @Override
                    public void onIsPlayingChanged(boolean isPlaying) {
                        if (isPlaying) {
                            // Active playback.
//                            playerView.setUseController(false);
                        }
                    }
                });

        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {

                long position = player.getCurrentPosition();
                POSITION = String.valueOf(position);
                if ((player.getContentDuration() - player.getCurrentPosition()) < 123000 && player.getCurrentPosition() != 0) {
//                    nextButtonText.setText("Next Episode");
//                    nextButton.setVisibility(View.VISIBLE);
                } else if (position > 0 && position < 122000) {
//                    skipBtntext.setText("Skip Intro");
//                    skipIntroBtn.setVisibility(View.VISIBLE);
                }
                else {
                    nextButton.setVisibility(View.INVISIBLE);
                    skipIntroBtn.setVisibility(View.INVISIBLE);

                }
                handler.postDelayed(this, 2000); // 2000ms frequency of updates.
            }
        };
        handler.postDelayed(updateRunnable, 2000);

        nextButton.setVisibility(View.INVISIBLE);
        skipIntroBtn.setVisibility(View.INVISIBLE);
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlayer();
                saveData(); //saves position and duration in SharedPreferences("info")
//                Intent intent = new Intent(PlayerActivity.this, Activity_Main.class); 22/08/2023
//                startActivity(intent); 22/08/2023
                finish();
            }
        });
    }

    private void stopPlayer() {
        player.stop();
        player.release();
        handler.removeCallbacks(updateRunnable);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        stopPlayer();
        saveData();
        finish();
        super.onBackPressed();
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("Blood", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(NAME, POSITION);
        edit.apply();
    }

    private String getVideoFileName(Uri videoUri) {
        String fileName = null;

        if (videoUri != null) {
            List<String> pathSegments = videoUri.getPathSegments();
            if (pathSegments != null && pathSegments.size() > 0) {
                fileName = pathSegments.get(pathSegments.size() - 1);
            }
        }

        return fileName;
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }
}

