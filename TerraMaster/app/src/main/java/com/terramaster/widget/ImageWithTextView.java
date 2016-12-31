package com.terramaster.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.terramaster.R;

public class ImageWithTextView extends LinearLayout {

    private Context mContext;
    private ImageView iv;
    private MyTextView tv;

    public ImageWithTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context, null);
    }

    public ImageWithTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setOrientation(VERTICAL);
        iv = new ImageView(mContext);
        addView(iv);

        tv = new MyTextView(mContext);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        llp.topMargin = Math.round(mContext.getResources().getDimension(R.dimen.dp_5));
        tv.setLayoutParams(llp);
        addView(tv);

        if (attrs != null) {
            final String xmlns = "http://schemas.android.com/apk/res/android";
            int text = attrs.getAttributeResourceValue(xmlns, "text", 0);
            tv.setText(text);

            int textColor = attrs.getAttributeResourceValue(xmlns, "textColor", 0);
            tv.setTextColor(textColor);

            int textSize = attrs.getAttributeResourceValue(xmlns, "textSize", 0);
            tv.setTextSize(mContext.getResources().getDimension(textSize));

            int src = attrs.getAttributeResourceValue(xmlns, "src", 0);
            iv.setImageResource(src);
        }
    }

}
