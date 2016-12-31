package com.terramaster.player;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.terramaster.R;

import java.util.Formatter;
import java.util.Locale;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;


/**
 * Created by Administrator on 2016/7/27.
 */
public class ControllerView extends FrameLayout implements View.OnKeyListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener{
    private VideoView videoView;
    private int duration;
    private int current;
    private int seekSize;
    private boolean flag = false;

    private Context mContext;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private View mCenterPlayButton;
    private View mControlLayout;
    private ViewGroup loadingLayout;
    private ViewGroup errorLayout;

    private static final int HIDE_BAR = 0x10001;
    private static final int SHOW_VIDEO_VIEW_STATUS = 0x10002;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case HIDE_BAR:
                    mControlLayout.setVisibility(View.GONE);
                    mHandler.removeMessages(HIDE_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_BAR, 3000);
                    break;
                case SHOW_VIDEO_VIEW_STATUS:
                    if(videoView != null){
                        mProgress.setProgress(videoView.getCurrentPosition());
                        mCurrentTime.setText(stringForTime(videoView.getCurrentPosition()));
                    }
                    mHandler.removeMessages(SHOW_VIDEO_VIEW_STATUS);
                    mHandler.sendEmptyMessageDelayed(SHOW_VIDEO_VIEW_STATUS, 300);
                    break;
            }
        }
    };

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
        if (videoView != null){
            videoView.setOnKeyListener(this);
            videoView.setOnCompletionListener(this);
            videoView.setOnErrorListener(this);
            videoView.setOnPreparedListener(this);
        } else {
            throw new RuntimeException("videoView is null ............",null);
        }
    }

    public ControllerView(Context context) {
        super(context);
        initPlay(context);
    }

    public ControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPlay(context);
    }

    public ControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPlay(context);
    }

    public void initPlay(Context context){
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRoot = inflater.inflate(R.layout.uvv_player_controller, this);
        initControllerView(viewRoot);
    }

    private void initControllerView(View v) {
        mControlLayout = v.findViewById(R.id.control_layout);
        mCenterPlayButton = v.findViewById(R.id.center_play_btn);
        loadingLayout = (ViewGroup) v.findViewById(R.id.loading_layout);
        errorLayout = (ViewGroup) v.findViewById(R.id.error_layout);
        View bar = v.findViewById(R.id.seekbar);
        mProgress = (ProgressBar) bar;
        mEndTime = (TextView) v.findViewById(R.id.duration);
        mCurrentTime = (TextView) v.findViewById(R.id.has_played);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        show();
        mHandler.sendEmptyMessage(SHOW_VIDEO_VIEW_STATUS);
    }
    
    public void show(){
    	  mHandler.sendEmptyMessageDelayed(HIDE_BAR, 3000);
    }
    
    public void hide(){
    	 mHandler.removeMessages(HIDE_BAR);
    }
    
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        // TODO Auto-generated method stub
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            if(!flag){
                current = videoView.getCurrentPosition();
                flag = true;
            }
            switch(keyCode){
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if(videoView.isPlaying()){
                        videoView.pause();
                        mCenterPlayButton.setVisibility(View.VISIBLE);
                        show();
                        mHandler.removeMessages(HIDE_BAR);
                        mHandler.removeMessages(SHOW_VIDEO_VIEW_STATUS);
                    }else{
                        videoView.start();
                        mCenterPlayButton.setVisibility(View.GONE);
                        mHandler.sendEmptyMessage(HIDE_BAR);
                        mHandler.sendEmptyMessage(SHOW_VIDEO_VIEW_STATUS);
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if(!videoView.isPlaying()){
                        videoView.start();
                    }
                    mHandler.removeMessages(SHOW_VIDEO_VIEW_STATUS);
                    mCenterPlayButton.setVisibility(View.GONE);
                    mControlLayout.setVisibility(View.VISIBLE);

                    mHandler.removeMessages(HIDE_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_BAR, 3000);

                    current = current + seekSize;
                    if(current > duration){
                        current = duration;
                    }
                    mProgress.setProgress(current);


                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if(!videoView.isPlaying()){
                        videoView.start();
                    }
                    mHandler.removeMessages(SHOW_VIDEO_VIEW_STATUS);
                    mCenterPlayButton.setVisibility(View.GONE);
                    mControlLayout.setVisibility(View.VISIBLE);

                    mHandler.removeMessages(HIDE_BAR);
                    mHandler.sendEmptyMessageDelayed(HIDE_BAR, 3000);

                    current = current - seekSize;
                    if(current < 0){
                        current = 0;
                    }
                    mProgress.setProgress(current);
                    break;
            }
        }else if(event.getAction() == KeyEvent.ACTION_UP){
            flag = false;
            switch(keyCode){
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    videoView.seekTo(current);
                    mHandler.sendEmptyMessage(SHOW_VIDEO_VIEW_STATUS);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    videoView.seekTo(current);
                    mHandler.sendEmptyMessage(SHOW_VIDEO_VIEW_STATUS);
                    break;
            }
        }
      /*  if(videoView.isPlaying()){
            videoView.pause();
            mCenterPlayButton.setVisibility(View.VISIBLE);
            show();
            mHandler.removeMessages(HIDE_BAR);
            mHandler.removeMessages(SHOW_VIDEO_VIEW_STATUS);
        }else{
            videoView.start();
            mCenterPlayButton.setVisibility(View.GONE);
            mHandler.sendEmptyMessage(HIDE_BAR);
            mHandler.sendEmptyMessage(SHOW_VIDEO_VIEW_STATUS);
        }*/
        return false;
    }
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d(TAG, "dispatchTouchEvent"+ event.getAction());
        return super.dispatchTouchEvent(event);
    }
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            default:
                break;
        }

        invalidate();
        return false;
    }
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        errorLayout.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if(videoView != null){
            current = videoView.getCurrentPosition();
            duration = videoView.getDuration();
            mProgress.setMax(duration);
            seekSize = duration/200;
            mCurrentTime.setText(stringForTime(current));
            mEndTime.setText(stringForTime(duration));
        }

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }
}
