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

import com.terramaster.R;

public class DeviceFileOptionPopup {

    private PopupWindow pwindow;
    private View popupLayout;
    private LayoutInflater inflater;

    @SuppressWarnings("deprecation")
    @SuppressLint("InflateParams")
    public DeviceFileOptionPopup(Context context, boolean isDirectory, final OnClickListener onClickListener, OnDismissListener onDismissListener) {
        // TODO Auto-generated constructor stub

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupLayout = inflater.inflate(R.layout.popup_device_file_option, null);

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

        popupLayout.findViewById(R.id.llFileOptions).setVisibility(isDirectory ? View.GONE : View.VISIBLE);
        popupLayout.findViewById(R.id.llDirectoryOptions).setVisibility(isDirectory ? View.VISIBLE : View.GONE);

        //Jaylin
        //popupLayout.findViewById(R.id.tvShare).setOnClickListener(onClickListener);
        //popupLayout.findViewById(R.id.tvShare).setAlpha((float) 0.3);
        popupLayout.findViewById(R.id.tvDownload).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvRename).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvDelete).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvDeleteDirectory).setOnClickListener(onClickListener);
        popupLayout.findViewById(R.id.tvRenameDirectory).setOnClickListener(onClickListener);

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
