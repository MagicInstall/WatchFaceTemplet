package com.magicinstall.watchfacetemplet;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.content.res.Resources;
import android.content.res.AssetManager;

/**
 * Created by wing on 15/11/7.
 */
public class InformationsDrawer extends WatchFaceDrawer{
    private Paint mTextPaint;

    public InformationsDrawer() {
        Typeface mFace = Typeface.createFromAsset(Resources.getSystem().getAssets(), "fonts/04B08.TTF");

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(8);
        mTextPaint.setTypeface(mFace);
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
