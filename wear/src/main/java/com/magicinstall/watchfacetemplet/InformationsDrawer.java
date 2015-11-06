package com.magicinstall.watchfacetemplet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by wing on 15/11/7.
 */
public class InformationsDrawer extends WatchFaceDrawer{
    private Paint mTextPaint;

    public InformationsDrawer() {
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(8);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void Draw(Canvas canvas, Rect bounds) {
        canvas.drawText("999x999", 40, 60, mTextPaint);

    }
}
