package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.terramaster.R;
import com.terramaster.widget.MyTextView;

public class SuccessToast extends Toast {

    @SuppressLint("InflateParams")
    public SuccessToast(Context context, String message, String path) {
        super(context);
        // TODO Auto-generated constructor stub
        View view = LayoutInflater.from(context).inflate(R.layout.toast_success, null, false);
        setView(view);

        ((MyTextView) view.findViewById(R.id.tvMessage)).setText(message);
        ((MyTextView) view.findViewById(R.id.tvPathShow)).setText(path);
        setDuration(Toast.LENGTH_LONG);
        setGravity(Gravity.CENTER, 0, 0);

    }

    @SuppressLint("InflateParams")
    public SuccessToast(Context context, String message) {
        super(context);
        // TODO Auto-generated constructor stub
        View view = LayoutInflater.from(context).inflate(R.layout.toast_success, null, false);
        setView(view);

        ((MyTextView) view.findViewById(R.id.tvMessage)).setText(message);
        view.findViewById(R.id.tvPathShow).setVisibility(View.GONE);
        setDuration(Toast.LENGTH_LONG);
        setGravity(Gravity.CENTER, 0, 0);

    }

}
