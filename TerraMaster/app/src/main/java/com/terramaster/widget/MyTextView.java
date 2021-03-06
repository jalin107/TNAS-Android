package com.terramaster.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.terramaster.R;

public class MyTextView extends TextView {

    private Context mContext;

    public MyTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        mContext = context;
        init(null);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // TODO Auto-generated constructor stub
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        String fontName = null;
        int textStyle = Typeface.NORMAL;
        if (attrs != null) {
            final String xmlns = "http://schemas.android.com/apk/res/android";
            textStyle = attrs.getAttributeIntValue(xmlns, "textStyle", 0);

            TypedArray a = mContext.obtainStyledAttributes(attrs,
                    R.styleable.MyFont);
            fontName = a.getString(R.styleable.MyFont_fontName);
            a.recycle();
        }
        setFont(fontName, textStyle);

    }

    public void setFont(String fontName, int textStyle) {
        if (isInEditMode())
            return;

        if (fontName == null || fontName.equals(""))
            fontName = mContext.getString(R.string.font_regular);

        setTypeface(Typeface.createFromAsset(mContext.getAssets(), fontName),
                textStyle);
    }

}
