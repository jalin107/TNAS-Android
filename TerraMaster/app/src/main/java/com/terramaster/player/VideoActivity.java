package com.terramaster.player;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import com.terramaster.R;

import java.util.HashMap;
import java.util.Map;


public class VideoActivity extends Activity {
    VideoView videoView;
    ControllerView playerController;
///	public String auth;
	@Override
	protected void onResume()
	{
		if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		super.onResume();


	}

///	public VideoActivity(String User,String pwd)
///	{
///
///	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view);
        // andsontan
		/*
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		TextView tv = (TextView)this.findViewById(R.id.tv);
		float width=dm.widthPixels*dm.density;
		float height=dm.heightPixels*dm.density;
		tv.setText("First method:"+dm.toString()+"\n"+"Second method:"+"Y="+screenWidth+";X="+screenHeight);
		*/
		//


        videoView = (VideoView) findViewById(R.id.video_view);
        playerController = (ControllerView) findViewById(R.id.play_control);
      
        playerController.setVideoView(videoView);

       String type =  getIntent().getStringExtra("type");
       if(type != null && type.equals("audio")){
    	    playerController.hide();
       }
       
        String url = getIntent().getStringExtra("playURI");
		String auth = getIntent().getStringExtra("playAUTH");
        if(url != null){
			// Map(String,string )hearder = ("Authorization"," Basic YWRtaW46MTIzNDU2")
			Map<String,String>xhead = new HashMap<String,String>();
			xhead.put("Authorization",auth);//" Basic YWRtaW46MTIzNDU2"
        	videoView.setVideoURI(Uri.parse(url),xhead);// heaeder
        	videoView.start();
        }else{
        	finish();
        }
    }
    protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Bundle extras = intent.getExtras();
		if(extras != null){
			String action = extras.getString("action");
			if(action == null){
				return;
			}
			if(action.equals("seek")){
				int seek = extras.getInt("seek");
				videoView.seekTo(seek);
			}else if(action.equals("stop")){
				videoView.stopPlayback();
			}else if(action.equals("pause")){
				videoView.pause();
			}else if(action.equals("start")){
				videoView.start();
			}
		}
	}
}
