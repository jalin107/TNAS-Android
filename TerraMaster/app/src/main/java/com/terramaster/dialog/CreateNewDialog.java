package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.terramaster.R;
import com.terramaster.model.FTPFileItem;
import com.terramaster.task.CreateNewFolderTask;
import com.terramaster.task.OnSuccessListener;
import com.terramaster.widget.MyEditText;
import com.terramaster.widget.MyTextView;

public class CreateNewDialog {

    private AlertDialog dialog;
    private Context mContext;
    private FTPFileItem fileItem;
    private MyEditText editText;
    private OnSuccessListener listener;

    @SuppressLint("InflateParams")
    public CreateNewDialog(Context context, FTPFileItem item, OnSuccessListener listener) {
        // TODO Auto-generated constructor stub
        fileItem = item;
        mContext = context;
        this.listener = listener;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(0);

        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_enter_value, null, false);

        builder.setView(view);

        ((MyTextView) view.findViewById(R.id.tvTitle)).setText(context.getString(R.string.create_new_folder));

        editText = (MyEditText) view.findViewById(R.id.edt);

        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        InputFilter[] filters = new InputFilter[2];
        filters[0] = new InputFilter.LengthFilter(30); // Filter to 10
        filters[1] = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.toString(source.charAt(i)).equals("_") && !Character.toString(source.charAt(i)).equals("-"))
                        return "";
                }
                return null;
            }
        };
        editText.setFilters(filters);


        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface d) {
                // TODO Auto-generated method stub
                final Button btnPositive = (Button) view.findViewById(R.id.btnPositive);
                if (editText.getText().length() <= 0) {
                    btnPositive.setEnabled(false);
                }

                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        createNewFolder();
                    }
                });

                Button btnNegative = (Button) view.findViewById(R.id.btnNegative);
                btnNegative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();

                    }
                });

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > 0) {
                            btnPositive.setEnabled(true);
                        } else {
                            btnPositive.setEnabled(false);
                        }
                    }
                });
            }
        });
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    private void createNewFolder() {
        String folderName = editText.getText().toString();
        new CreateNewFolderTask(mContext, fileItem, folderName, new OnSuccessListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                dialog.dismiss();
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        }).start();
    }
}
