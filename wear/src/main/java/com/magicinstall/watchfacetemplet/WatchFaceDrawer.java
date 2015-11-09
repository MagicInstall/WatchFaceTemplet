package com.magicinstall.watchfacetemplet;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by wing on 15/11/7.
 * 哩个类主要提供一个标准的Draw() 方法.
 */
public abstract class WatchFaceDrawer {
    public boolean IsVisible;
    public boolean IsAmbient;

    private GregorianCalendar mCalendar;

    public WatchFaceDrawer() {
        mCalendar = new GregorianCalendar();
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
