package bp.com.slamdunktop10;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by daz on 12/12/14.
 */
public class ActivityPlayVideo  extends Activity {
    private VideoView videoView;
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private String streamingAddress;

    private Intent intent;

    @Override
    public void onCreate (Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_play_video);
        intent = getIntent();

        initViews();
        setViews();
    }

    private void initViews () {
        getActionBar().hide();

        mediaController = new MediaController(this);
        videoView = (VideoView) findViewById(R.id.videoview);
        streamingAddress = intent.getExtras().getString(Constant.VIDEO_STREAMING_URL);
    }

    private void setViews () {
        Uri videoUri = Uri.parse(streamingAddress);
        videoView.setVideoURI(videoUri);
        videoView.start();

        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
