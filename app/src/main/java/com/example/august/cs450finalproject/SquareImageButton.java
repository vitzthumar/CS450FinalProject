package com.example.august.cs450finalproject;

import android.content.Context;
import android.util.AttributeSet;

public class SquareImageButton  extends android.support.v7.widget.AppCompatImageButton {

    public SquareImageButton(Context context) {
        super(context);
    }

    public SquareImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredHeight();
        setMeasuredDimension(width, width);
    }
}