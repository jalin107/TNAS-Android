package com.terramaster.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.terramaster.R;


public class AlertUtils {
    public static void showInProgress(Context context) {
        showSimpleAlert(context, "Still under development!");
    }


    public static AlertDialog showSimpleAlert(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(0);
        builder.setTitle("");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null, false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        Button btnNeutral = (Button) view.findViewById(R.id.btnNeutral);
        btnNeutral.setVisibility(View.VISIBLE);
        btnNeutral.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        dialog.show();

        return dialog;
    }

    public static AlertDialog showConfirmAlert(Context context, String title, String message, final DialogInterface.OnClickListener onYesClick) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(0);
        builder.setTitle("");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert, null, false);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        Button btnPositive = (Button) view.findViewById(R.id.btnPositive);
        btnPositive.setVisibility(View.VISIBLE);
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                if (onYesClick != null) {
                    onYesClick.onClick(dialog, 0);
                }
            }
        });
        Button btnNegative = (Button) view.findViewById(R.id.btnNegative);
        btnNegative.setVisibility(View.VISIBLE);
        btnNegative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        try {
            dialog.show();
        } catch (Exception e) {

        }
        return dialog;
    }

    public static AlertDialog showListAlert(Context context, String title, String[] items, final DialogInterface.OnClickListener onItemClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(0);
        builder.setTitle(title);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);
        builder.setAdapter(adapter, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (onItemClick != null) {
                    onItemClick.onClick(dialog, which);
                }
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);

        AlertDialog dialog = builder.show();
        changeDefaultColor(dialog);
        return dialog;
    }

    public static void changeDefaultColor(AlertDialog dialog) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // only for gingerbread and newer versions
                Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (b != null)
                    b.setTextColor(dialog.getContext().getResources().getColor(android.R.color.black));

                b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (b != null)
                    b.setTextColor(dialog.getContext().getResources().getColor(android.R.color.black));

                b = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                if (b != null)
                    b.setTextColor(dialog.getContext().getResources().getColor(android.R.color.black));
            } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ViewGroup decorView = (ViewGroup) dialog.getWindow().getDecorView();
                FrameLayout windowContentView = (FrameLayout) decorView.getChildAt(0);
                FrameLayout contentView = (FrameLayout) windowContentView.getChildAt(0);
                LinearLayout parentPanel = (LinearLayout) contentView.getChildAt(0);
                LinearLayout topPanel = (LinearLayout) parentPanel.getChildAt(0);
                View titleDivider = topPanel.getChildAt(2);
                LinearLayout titleTemplate = (LinearLayout) topPanel.getChildAt(1);
                TextView alertTitle = (TextView) titleTemplate.getChildAt(1);

                int textColor = dialog.getContext().getResources().getColor(android.R.color.black);
                alertTitle.setTextColor(textColor);

                int primaryColor = dialog.getContext().getResources().getColor(android.R.color.black);
                titleDivider.setBackgroundColor(primaryColor);
            }

        } catch (Exception e) {
            // e.printStackTrace();

        }
    }
}
