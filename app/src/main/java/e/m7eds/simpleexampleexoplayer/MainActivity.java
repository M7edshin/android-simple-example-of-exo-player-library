package e.m7eds.simpleexampleexoplayer;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class MainActivity extends AppCompatActivity implements ExoPlayer.EventListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    SimpleExoPlayer exoPlayer;
    SimpleExoPlayerView exoPlayerView;
    PlaybackStateCompat.Builder stateBuilder;
    MediaSessionCompat mediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exoPlayerView = findViewById(R.id.exo_player_view);

        initializeMediaSession();
        playMedia();

    }

    private void playMedia(){
        exoPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                (getResources(), R.drawable.image));

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(null);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        exoPlayerView.requestFocus();
        exoPlayerView.setUseController(true);
        exoPlayerView.setPlayer(exoPlayer);

        String userAgent = Util.getUserAgent(this, "ExampleExoPlayer");
        DataSource.Factory dataSource = new DefaultDataSourceFactory(this, userAgent);
        //You can either provide path of audio or video as example:
        /*
        Uri file = Uri.parse("asset:///audio.mp3");
        Uri file = Uri.parse("asset:///video.mp4");
        */
        Uri file = Uri.parse("asset:///video.mp4");

        ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource(file, dataSource,
                new DefaultExtractorsFactory() ,null, null);
        exoPlayer.prepare(extractorMediaSource);
        exoPlayer.setPlayWhenReady(true);

    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mediaSession = new MediaSessionCompat(this, TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mediaSession.setPlaybackState(stateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mediaSession.setActive(true);

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    /**
     * Method that is called when the ExoPlayer state changes. Used to update the MediaSession and
     * PlayBackState to keep in sync.
     * @param playWhenReady true if ExoPlayer is playing, false if it's paused.
     * @param playbackState int describing the state of ExoPlayer. Can be STATE_READY, STATE_IDLE,
     *                      STATE_BUFFERING, or STATE_ENDED.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == ExoPlayer.STATE_READY) && playWhenReady){
            stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    exoPlayer.getCurrentPosition(), 1f);
        } else if((playbackState == ExoPlayer.STATE_READY)){
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.getCurrentPosition(), 1f);
        }
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            exoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            exoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            exoPlayer.seekTo(0);
        }
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }

    /**
     * Release the player when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        mediaSession.setActive(false);
    }
}
