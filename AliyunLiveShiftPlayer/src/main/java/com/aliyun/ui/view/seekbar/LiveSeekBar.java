package com.aliyun.ui.view.seekbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.aliyun.sdk.player.aliyunliveshiftplayer.R;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
@SuppressLint("AppCompatCustomView")
public class LiveSeekBar extends SeekBar {

    private long currentLiveTime = 0;
    private long mShiftStartTime = 0;
    private long mEndTime = 0;


    private Paint linePaint;

    public LiveSeekBar(Context context) {
        super(context);
        init();
    }

    public LiveSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LiveSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.alivc_common_bg_red));
        linePaint.setAntiAlias(true);
    }


    public void updateRange(long shitStartTime, long endTime) {
        mShiftStartTime = shitStartTime;
        mEndTime = endTime;

        setMax((int) (mEndTime - shitStartTime));
    }

    public void setLiveTime(long time) {
        currentLiveTime = time;
        setSecondaryProgress((int) (currentLiveTime - mShiftStartTime));
    }

    public void setPlayProgress(long currentTime) {
        setProgress((int) (currentTime - mShiftStartTime));
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        //再绘制一遍Thumb，防止被覆盖掉
        drawThumb(canvas);

    }

    private void drawLine(Canvas canvas) {
        int offsetX = getPaddingLeft() + getPaddingRight();
        int width = getWidth() - offsetX;
        int height = getHeight();
        if (width > 0 && height > 0) {

            float lineWith = width / 100f;
            float lineHeight = height;

            float startX = getPaddingLeft() + getSecondaryProgress() * 1.0f / getMax() * width;
            float stopX = startX;
            float startY = 0;
            float stopY = lineHeight;

            linePaint.setStrokeWidth(lineWith);
            canvas.drawLine(startX, startY, stopX, stopY, linePaint);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    void drawThumb(Canvas canvas) {
        Drawable mThumb = getThumb();
        if (mThumb != null) {
            final int saveCount = canvas.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
            mThumb.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }
}
