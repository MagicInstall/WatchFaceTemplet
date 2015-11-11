package com.magicinstall.wearable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 在CanvasWatchFaceService.Engine 的子类引入哩个接口,
 * 主要用嚟调用Engine 的刷新方法.
 */
interface WatchFaceCallback {
    /**
     * 当WatchFaceDrawer 需要主动要求CanvasWatchFaceService.Engine 刷新嘅时候就会调用哩个方法.
     * 哩个方法体一般只需要一句postInvalidate();
     */
    public void needPostInvalidate();
}

/**
 * Created by wing on 15/11/7.
 * 哩个类主要提供一个标准的Draw() 方法.
 */
public abstract class WatchFaceDrawer {

    // 响应可见模式切换
    private boolean mIsVisible;
    public boolean getIsVisible() {return mIsVisible;}
    public void setIsVisible(boolean visible) {
        if (visible != mIsVisible) {
            System.out.println(
                    "onVisibilityChanged:"
                    + mIsVisible + "->" + visible
            );
            mIsVisible = visible;
            onVisibilityChanged(visible);

            // 判断系唔系交互模式
            if (mIsVisible && !mIsAmbient && !mIsInteractive) {
                mIsInteractive = true;
                onInteractiveModeChanged(true);
            }
            else if (!mIsVisible || mIsAmbient) {
                mIsInteractive = false;
                onInteractiveModeChanged(false);
            }
        }
    }
    /**
     * 屏幕开关切换事件
     * @param visible true:Visible; false:Invisible.
     */
    protected void onVisibilityChanged(boolean visible) {}

    // 响应环境模式切换
    private boolean mIsAmbient;
    public boolean getIsAmbient() {return mIsAmbient;}
    public void setIsAmbient(boolean isAmbient) {
        if (isAmbient != mIsAmbient) {
            System.out.println(
                    "onAmbientModeChanged:"
                    + mIsAmbient + "->" + isAmbient
            );
            mIsAmbient = isAmbient;
            onAmbientModeChanged(isAmbient);
        }

        // 判断系唔系交互模式
        if (mIsVisible && !mIsAmbient && !mIsInteractive) {
            mIsInteractive = true;
            onInteractiveModeChanged(true);
        }
        else if (!mIsVisible || mIsAmbient) {
            mIsInteractive = false;
            onInteractiveModeChanged(false);
        }
    }
    /**
     * 环境模式切换事件.
     * @param inAmbientMode true:Ambient mode; false:Interactive mode.
     */
    protected void onAmbientModeChanged(boolean inAmbientMode) {}

    // 响应交互模式间切换
    private boolean mIsInteractive;
    /**
     * 交互模式间切换事件.
     * @param inInteractiveMode
     */
    protected void onInteractiveModeChanged(boolean inInteractiveMode) {}


    /**
     * 表盘宽度, 一般唔好修改佢
     */
    protected int mWidthPixels;

    /**
    * 表盘高度, 一般唔好修改佢
    */
    protected int mHeightPixels;

    /**
     * CanvasWatchFaceService 类的资源访问对象.
     */
    protected Resources mResources;

    /**
     * CanvasWatchFaceService 类的上下文对象.
     */
    protected Context mContext;

    /**
     * 指向CanvasWatchFaceService 的回调接收方.
     */
    protected WatchFaceCallback mCallbakeReceiver;

    /**
     * 屏幕参数对象
     */
    protected DisplayMetrics mDisplayMetrics;

    /**
     * 硬件操作对象
     */
    protected Hardware mHardwear;

    private GregorianCalendar mCalendar;

    public WatchFaceDrawer(CanvasWatchFaceService.Engine engine, Resources resources, Context context) {
        mCalendar = new GregorianCalendar();

        mResources = resources;
        mDisplayMetrics = resources.getDisplayMetrics();
        // 取得屏幕尺寸
        mWidthPixels = mDisplayMetrics.widthPixels;
        mHeightPixels = mDisplayMetrics.heightPixels;

        mCallbakeReceiver = (WatchFaceCallback)engine;
        mContext = context;
        mHardwear = new Hardware(context);
    }


    /**
     * 更新当前时间.
     */
    public void setTimeToNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * 设置时区.
     * @param id 表示时区ID 嘅字串.
     */
    public void setTimeZone(String id) {
        mCalendar.setTimeZone(TimeZone.getTimeZone(id));
    }

    /**
     *
     * @return 取得年份.
     */
    public int getYear() {
        return mCalendar.get(Calendar.YEAR);
    }

    /**
     *
     * @return 取得月份.
     */
    public int getMonth() {
        return mCalendar.get(Calendar.MONTH) + 1; // 原方法返回嘅月份从0开始
    }

    /**
     *
     * @return 取得日号.
     */
    public int getDay() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     *
     * @return 取得12小时制嘅小时.
     */
    public int getHour12() {
        return mCalendar.get(Calendar.HOUR);
    }

    /**
     *
     * @return 取得24小时制嘅小时.
     */
    public int getHour24() {
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     *
     * @return 取得分钟.
     */
    public int getMinute() {
        return mCalendar.get(Calendar.MINUTE);
    }

    /**
     *
     * @return 取得秒.
     */
    public int getSecond() {
        return mCalendar.get(Calendar.SECOND);
    }

    /**
     * 取得星期几.
     * @return 星期日:0, 星期一:1...
     */
    public int getWeeks() {
        return mCalendar.get(Calendar.DAY_OF_WEEK) - 1; // 原方法返回星期日:1, 星期一:2... 星期六:7
    }

    /**
     * 取得昼夜划分.
     * @return AM:0, PM:1
     */
    public int getAM_PM() {
        return mCalendar.get(Calendar.AM_PM);
    }


    /**
     * 子类必须实现表盘嘅重绘方法.
     * @param canvas
     * @param bounds
     */
    public abstract void Draw(Canvas canvas, Rect bounds);

}
