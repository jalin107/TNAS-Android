package com.terramaster.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.terramaster.R;
import com.terramaster.task.DeterminePublicIpTask;
import com.terramaster.task.UpdateShareFileTask;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SharedPrefUtils;
import com.terramaster.utils.StringUtils;

import java.util.ArrayList;

public class ShareDialog extends Dialog implements android.view.View.OnClickListener {
    private LinearLayout llContent;
    private Context mContext;
    private String message;

    @SuppressLint("InflateParams")
    public ShareDialog(Context context, String message) {
        super(context, R.style.CustomDialog);
        // TODO Auto-generated constructor stub
        mContext = context;
        this.message = message;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_share, null);

        setContentView(view);

        view.findViewById(R.id.llMain).setOnClickListener(this);
        view.findViewById(R.id.llContent).setOnClickListener(this);
        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        view.findViewById(R.id.tvFacebook).setOnClickListener(this);
        view.findViewById(R.id.tvTwitter).setOnClickListener(this);
        view.findViewById(R.id.tvGooglePlus).setOnClickListener(this);
        view.findViewById(R.id.tvInstagram).setOnClickListener(this);
        view.findViewById(R.id.tvPintrest).setOnClickListener(this);
        view.findViewById(R.id.tvLinkedIn).setOnClickListener(this);
        view.findViewById(R.id.tvWeChat).setOnClickListener(this);
        view.findViewById(R.id.tvQQ).setOnClickListener(this);
        view.findViewById(R.id.tvMail).setOnClickListener(this);

        llContent = (LinearLayout) view.findViewById(R.id.llContent);

        setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                // TODO Auto-generated method stub
                llContent.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_up));
            }
        });
    }

    public static void startShareLinks(Context mContext, final String port, final ArrayList<String> list) {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "", "Preparing for share...Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        new DeterminePublicIpTask(mContext) {
            @Override
            protected synchronized void onWorkFinished(final String ip) {
                super.onWorkFinished(ip);
                if (ip != null) {
                    LogM.e("Public ip is " + ip);

                    new UpdateShareFileTask(mContext, list) {
                        @Override
                        protected synchronized void onWorkFinished(String result) {
                            super.onWorkFinished(result);
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e) {

                            }
                            if (result != null) {
                                new ShareDialog(mContext, createShareMessage()).show();
                            } else {
                                new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
                            }

                        }

                        ;

                        private String createShareMessage() {
                            String user = SharedPrefUtils.getInstance(mContext).getUsername();
                            StringBuilder sb = new StringBuilder();
                            sb.append("Shared files:\n");
                            for (String SID : shareFileSIDList) {
                                LogM.e("SID result: " + SID);
                                sb.append("http://" + ip +(StringUtils.isEmpty(port)?"":":"+port) +"/3.0/index.php?share/file&user=" + user + "&sid=" + SID);
                                sb.append("\n");
                            }

                            return sb.toString();
                        }
                    }.execute();
                } else {
                    new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {

                    }
                }
            }

            ;
        }.execute();
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
            case R.id.btnCancel:
                startDismissDialog();
                break;
            case R.id.llMain:
                startDismissDialog();
                break;
            case R.id.llContent:
                break;

            case R.id.tvFacebook:
                shareOnSocial(v.getId(), "com.facebook.katana");
                break;
            case R.id.tvTwitter:
                shareOnSocial(v.getId(), "com.twitter.android");
                break;
            case R.id.tvGooglePlus:
                shareOnGoogleplus(v.getId(), "com.google.android.apps.plus");
                break;
            case R.id.tvLinkedIn:
                shareOnSocial(v.getId(), "com.linkedin.android");
                break;
            case R.id.tvWeChat:
                shareOnSocial(v.getId(), "com.tencent.mm");
                break;

            case R.id.tvQQ:
                shareOnSocial(v.getId(), "com.tencent.qqlite");
                break;
            case R.id.tvMail:
                shareInEmail();
                break;
            case R.id.tvPintrest:
            case R.id.tvInstagram:
                AlertUtils.showInProgress(mContext);
                break;

            default:
                break;
        }
    }

    private void startDismissDialog() {
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
            }
        });
        llContent.startAnimation(animation);
    }

    private boolean appInstalledOrNot(String pkg) {
        PackageManager localPackageManager = mContext.getPackageManager();
        try {
            localPackageManager.getPackageInfo(pkg, 1);
            return true;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            return false;
        }

    }

    private void openPlayStore(String appPackageName) {
        try {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private String getConfirmMessage(int id) {
        switch (id) {
            case R.id.tvFacebook:
                return mContext.getString(R.string.confirm_facebook_install);
            case R.id.tvTwitter:
                return mContext.getString(R.string.confirm_twitter_install);
            case R.id.tvGooglePlus:
                return mContext.getString(R.string.confirm_googleplus_install);
            case R.id.tvLinkedIn:
                return mContext.getString(R.string.confirm_linkedin_install);
            case R.id.tvWeChat:
                return mContext.getString(R.string.confirm_wechat_install);
            case R.id.tvQQ:
                return mContext.getString(R.string.confirm_qq_install);
            default:
                break;
        }
        return "Would you like to install this app?";
    }

    private void shareOnSocial(int id, final String packageName) {
        // TODO Auto-generated method stub
        if (!appInstalledOrNot(packageName)) {
            AlertUtils.showConfirmAlert(mContext, "", getConfirmMessage(id), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    openPlayStore(packageName);
                }
            });
            return;
        }
        try {
            Intent sendIntent = new Intent();
            sendIntent.setPackage(packageName);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.share_subject));
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            mContext.startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

    private void shareOnGoogleplus(int id, final String packageName) {
        if (!appInstalledOrNot(packageName)) {
            AlertUtils.showConfirmAlert(mContext, "", getConfirmMessage(id), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    openPlayStore(packageName);
                }
            });
            return;
        }
        try {
            Intent shareIntent = ShareCompat.IntentBuilder.from((Activity) mContext).setText(message).setType("text/*").getIntent().setPackage(packageName);
            mContext.startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

    private void shareInEmail() {
        // TODO Auto-generated method stub

        try {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SENDTO);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.share_subject));
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            // sendIntent.setType("message/rfc822");
            sendIntent.setData(Uri.parse("mailto:"));
            mContext.startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            AlertUtils.showConfirmAlert(mContext, "", mContext.getString(R.string.confirm_mailapp_install), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    String link = "https://play.google.com/store/search?q=mail&c=apps";
                    try {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                    }
                }
            });
        }
    }

}
