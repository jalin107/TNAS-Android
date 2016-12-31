package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.terramaster.R;
import com.terramaster.widget.MyTextView;

public class ErrorToast extends Toast {

    @SuppressLint("InflateParams")
    public ErrorToast(Context context, String message) {
        super(context);
        // TODO Auto-generated constructor stub
        View view = LayoutInflater.from(context).inflate(R.layout.toast_success, null, false);
        setView(view);

        ((ImageView) view.findViewById(R.id.ivIc)).setImageResource(R.drawable.ic_error);
        ((MyTextView) view.findViewById(R.id.tvMessage)).setText(message);

        setDuration(Toast.LENGTH_SHORT);
        setGravity(Gravity.CENTER, 0, 0);

    }

}
