package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.terramaster.R;

public class MoreOptionsDialog extends Dialog implements android.view.View.OnClickListener {
    private LinearLayout llContent;
    private Context mContext;
    private OnOptionSelectListener listener;

    @SuppressLint("InflateParams")
    public MoreOptionsDialog(Context context, OnOptionSelectListener listener) {
        super(context, R.style.CustomDialog);
        // TODO Auto-generated constructor stub
        mContext = context;
        this.listener = listener;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_more_options, null);

        setContentView(view);

        view.findViewById(R.id.llMain).setOnClickListener(this);
        view.findViewById(R.id.llContent).setOnClickListener(this);
        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        view.findViewById(R.id.llCreateNewFolder).setOnClickListener(this);
        view.findViewById(R.id.llFileDownload).setOnClickListener(this);

        llContent = (LinearLayout) view.findViewById(R.id.llContent);

        setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                // TODO Auto-generated method stub
                llContent.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
            }
        });
    }

    @Override
    public void show() {
        super.show();
        getWindow().setWindowAnimations(R.style.DialogNoAnimation);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        startDismissDialog(0);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.llContent:
                break;

            case R.id.btnCancel:
            case R.id.llMain:
            case R.id.llCreateNewFolder:
            case R.id.llFileDownload:
                startDismissDialog(v.getId());
                break;

            default:
                break;
        }
    }

    private void startDismissDialog(final int id) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                dismiss();
                if (listener != null) {
                    listener.onSelectOption(id);
                }
            }
        });
        llContent.startAnimation(animation);
    }

}
