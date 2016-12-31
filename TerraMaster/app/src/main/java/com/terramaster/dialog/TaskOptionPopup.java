package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.terramaster.R;
import com.terramaster.dbhelper.DBKeys;

public class TaskOptionPopup {

    private PopupWindow pwindow;
    private View popupLayout;
    private LayoutInflater inflater;

    @SuppressWarnings("deprecation")
    @SuppressLint("InflateParams")
    public TaskOptionPopup(Context context, int taskStatus, final OnClickListener onClickListener, OnDismissListener onDismissListener) {
        // TODO Auto-generated constructor stub

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupLayout = inflater.inflate(R.layout.popup_task_option, null);

        // Creating the PopupWindow
        pwindow = new PopupWindow(context);
        pwindow.setContentView(popupLayout);
        // pwindow.showAsDropDown(v);
        // pwindow.setWidth(popupwindowWidth);
        pwindow.setBackgroundDrawable(new BitmapDrawable());
        pwindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pwindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        pwindow.setFocusable(true);

        pwindow.setOnDismissListener(onDismissListener);

        boolean isIncomplete = (taskStatus == DBKeys.STATUS_ERROR || taskStatus == DBKeys.STATUS_RUNNING || taskStatus == DBKeys.STATUS_PAUSED);
        popupLayout.findViewById(R.id.llCompleteOptions).setVisibility(isIncomplete ? View.GONE : View.VISIBLE);
        popupLayout.findViewById(R.id.llInCompleteOptions).setVisibility(isIncomplete ? View.VISIBLE : View.GONE);

        TextView tvPlayPause = (TextView) popupLayout.findViewById(R.id.tvPlayPause);
        tvPlayPause.setText(context.getString(taskStatus == DBKeys.STATUS_RUNNING ? R.string.pause : R.string.recover));
        tvPlayPause.setCompoundDrawablesWithIntrinsicBounds(0, taskStatus == DBKeys.STATUS_RUNNING ? R.drawable.ic_option_pause : R.drawable.ic_option_recover, 0, 0);
        //Jaylin
        //popupLayout.findViewById(R.id.tvShare).setOnClickListener(onClickListener);
        //popupLayout.findViewById(R.id.tvShare).setAlpha((float) 0.3);
        //popupLayout.findViewById(R.id.tvShare2).setOnClickListener(onClickListener);
        //popupLayout.findViewById(R.id.tvShare2).setAlpha((float) 0.3);
        popupLayout.findViewById(R.id.tvPlayPause).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvDelete).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvDelete2).setOnClickListener(onClickListener);

    }

    public void show(View anchorView) {
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        pwindow.setHeight(anchorView.getHeight());
        pwindow.showAtLocation(popupLayout, Gravity.NO_GRAVITY, location[0], location[1] + anchorView.getHeight());
    }

    public void dismiss() {
        pwindow.dismiss();
    }

}
