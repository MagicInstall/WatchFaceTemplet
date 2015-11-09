package com.magicinstall.watchfacetemplet;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Size;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Created by wing on 15/11/7.
 */
public class InformationsDrawer extends WatchFaceDrawer{
//    private String mDrawString;
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;

//    private GregorianCalendar mCalendar;
//    private Date mdate;
    private String mDateString;
    private DateFormat mDateFormat;

//    private float mFPS;
    private long  mPrevFrameMs;

    private Size mDisplaySize;


    public InformationsDrawer(Resources resources) {
        super(resources);
//        mDrawString = ("1234567890\n" +
//                "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n" +
//                "abcdefghijklmnopqrstuvwxyz\n" +
//                "~!@#$%^&*()_+-={}[]|;:'\"\\//,.<>");

        // 指定系统字体
//        Typeface mFace = Typeface.create("Roboto", Typeface.NORMAL);

        // 指定assets 目录入边嘅TTF字体
        Typeface mFace = Typeface.createFromAsset(mResources.getAssets(), "fonts/Roboto-Thin.ttf");

        // 设置字体画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(14);
        mTextPaint.setTypeface(mFace); // 指定字体

        // 取得屏幕尺寸
        mDisplaySize = new Size(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
//        mDisplayMetrics = new DisplayMetrics();
//        mDisplayMetrics = resources.getDisplayMetrics();

        // 使用StaticLayout 显示垂直居中嘅多行文本
        mTextLayout = new StaticLayout("", mTextPaint, mDisplaySize.getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // 更新时间
//        mCalendar = new GregorianCalendar();
//        mdate = new Date();
//        setDateToNow();

        // 设置时间格式
        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.getDefault());
    }

    /*
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    */

    // 重新组成字串, 新建一个居中Layout
    protected final void rebuildTextLayout(){
        // 可见度
        String str_visible = (IsVisible ? "Visible" : "Invisible") + "\n";

        // 刷新模式
        String str_ambient = (IsAmbient ? "Ambient" : "Interactive") + "\n";

        // 帧率
//        mFPS = (mPrevFrameMs - System.currentTimeMillis()) / 1000.0f;
        String str_FPS = " FPS:" +
                String.valueOf(1000.0f / (System.currentTimeMillis()- mPrevFrameMs))
                + "\n";
        mPrevFrameMs = System.currentTimeMillis();

        mTextLayout = new StaticLayout(
                str_visible +
                str_ambient +
                mDisplaySize.toString() + str_FPS +
                "\n\n\n\n\n\n" +
                mDateString,
                mTextPaint, mDisplaySize.getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

    }

    @Override
    public void Draw(Canvas canvas, Rect bounds) {
        mDateString = mDateFormat.format(System.currentTimeMillis());
        rebuildTextLayout();
        mTextLayout.draw(canvas);
    }

}
