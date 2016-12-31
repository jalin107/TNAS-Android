package com.terramaster.player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.terramaster.R;

import java.util.ArrayList;

public class MyImageView extends Activity {
    private ArrayList<String> URLS;
    private ArrayList<String> NAMES;
    private String MYFILE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        URLS = intent.getStringArrayListExtra("URLS");
        NAMES = intent.getStringArrayListExtra("NAMES");
        MYFILE = intent.getStringExtra("MYFILE");
        this.getWindow().setBackgroundDrawableResource(R.color.uvv_black);
        setContentView(new ViewFlipperView(this));
    }

    public String getFile() {
        return MYFILE;
    }

    public ArrayList<String> getURLS(){
        return URLS;
    }

    public ArrayList<String> getNAMES(){
        return NAMES;
    }

    public TextView getBackView()
    {
        TextView tvback = new TextView(this);
        tvback.setText(R.string.back);
        tvback.setPadding(6, 6, 6, 6);
        tvback.setTextColor(Color.WHITE);
        tvback.setTextAppearance(this,R.style.PatternView_Holo_Light);
        return tvback;
    }

    public TextView getTextView(String title){
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setPadding(6, 6, 6, 6);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        tv.setLayoutParams(new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        return tv;
    }
}