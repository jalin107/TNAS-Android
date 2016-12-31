package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.terramaster.R;
import com.terramaster.model.FTPFileItem;
import com.terramaster.task.OnSuccessListener;
import com.terramaster.task.RenameFTPFileTask;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.IconUtils;
import com.terramaster.utils.KeyboardUtils;
import com.terramaster.utils.StringUtils;
import com.terramaster.widget.MyEditText;
import com.terramaster.widget.MyTextView;

public class RenameDialog extends Dialog implements android.view.View.OnClickListener {
    private LinearLayout llMain;
    private Context mContext;
    private MyEditText edtName;
    private OnSuccessListener listener;

    private FTPFileItem fileItem;

    @SuppressLint("InflateParams")
    public RenameDialog(Context context, FTPFileItem ftpFileItem, OnSuccessListener listener) {
        super(context, R.style.CustomDialog);
        // TODO Auto-generated constructor stub
        mContext = context;
        fileItem = ftpFileItem;
        this.listener = listener;

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null);

        setContentView(view);

        ((MyTextView) view.findViewById(R.id.tvTitle)).setText(mContext.getString(R.string.rename));

        view.findViewById(R.id.tvCancelButton).setOnClickListener(this);
        view.findViewById(R.id.tvConfirmButton).setOnClickListener(this);
        view.findViewById(R.id.ivCancel).setOnClickListener(this);

        ImageView ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
        if (fileItem.isDirectory()) {
            ivIcon.setImageResource(R.drawable.folder_ic);
        } else {
            ivIcon.setImageResource(IconUtils.findIcon(fileItem.getName()));
        }

        edtName = (MyEditText) view.findViewById(R.id.edtName);
        edtName.setText(fileItem.getName());
        edtName.setSelection(fileItem.getName().length());

        llMain = (LinearLayout) view.findViewById(R.id.llMain);
        edtName.setFilters(new InputFilter[]{
                new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals("_") && !Character.toString(source.charAt(i)).equals("-"))
                                return "";
                        }
                        return null;
                    }
                }
        });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // TODO Auto-generated method stub
                llMain.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
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
        startDismissDialog();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvCancelButton:
                startDismissDialog();
                break;
            case R.id.tvConfirmButton:
                renameFilename();
                break;
            case R.id.ivCancel:
                edtName.setText("");
                break;

            default:
                break;
        }
    }

    private void renameFilename() {
        String newName = edtName.getText().toString();
        if (StringUtils.isEmpty(newName)) {
            AlertUtils.showSimpleAlert(mContext, mContext.getString(R.string.e_blank_file_name));
            return;
        }
        new RenameFTPFileTask(mContext, fileItem, newName, new OnSuccessListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                startDismissDialog();
            }
        }).start();
    }

    private void startDismissDialog() {
        KeyboardUtils.hideKeyboard(mContext, edtName);
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
                    listener.onSuccess();
                }
            }
        });
        llMain.startAnimation(animation);
    }

}
