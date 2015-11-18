package com.magicinstall.wearable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by wing on 15/11/19.
 */
public class ContentObserver extends android.database.ContentObserver{
    /**
     * @param context 传入Engine 的BaseContext.
     */
    public ContentObserver(Context context/*, Handler handler*/){
        super(new Handler());
        mContext = context;
        mResolver = context.getContentResolver();
    }

    @Override
    protected void finalize() throws Throwable {
        DeactivateObserver();
        Log.d("Observer", "finalize - unregisterContentObserver(this)");
        super.finalize();
    }

    // mobvoi 共享嘅当天累计步数和行走距离的数据
    public static final Uri CONTENT_STEP_URI = Uri.parse("content://com.mobvoi.ticwear.steps");

    // mobvoi 共享嘅天气数据
    public static final Uri WEATHER_URI = Uri.parse("content://com.mobvoi.provider.weather");
    private static final String[] COLUMN_NAMES = { "time", "temp", "address", "location", "maxtemp", "mintemp", "pm25", "weather", "sunset", "sunrise" };

    /**
     * Engine 的BaseContext
     */
    protected Context mContext;

    /**
     * 内容解析者
     */
    private android.content.ContentResolver mResolver;

//    android.database.ContentObserver mObserver;

    /**
     * 步数
     */
    protected int mSteps = 0;
    /**
     * 步行距离, 单位是米
     */
    protected int mDistance = 0;

    private boolean mStepEnabled = false;

    protected WeatherInfo mWeatherInfo;
    private boolean mWeatherEnabled = false;

    /**
     * 解释步行数据
     */
    private void querySteps() {
//        int steps = 0;
//        int distance = 0;
        //query 接口只接收URI，不接收其参数，置为null即可。
        Cursor cursor = mResolver.query(CONTENT_STEP_URI, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int steps = cursor.getInt(0);
                    int distance = cursor.getInt(1); // 距离的单位是米
                    Log.v("Observer", "Steps:" + mSteps + " Distance:" + mDistance);

                    // 触发事件
                    if (steps != mSteps || distance != mDistance) {
                        onStepChanged(mSteps, mDistance);
                        mSteps = steps;
                        mDistance = distance;
                    }
                }
            } finally {
                cursor.close();
            }
        }
//        return steps;
    }

    /**
     * 解释天气数据
     */
    private void fetchWeatherInfo() {
        Cursor cursor = mResolver.query(WEATHER_URI, COLUMN_NAMES, null, null, null);
        if (cursor != null) {
            try {
                WeatherInfo info = new WeatherInfo(cursor);
                Log.v("Observer", info.toString());
                onWeatherChanged(info);
//                if (cursor.moveToFirst()) {
//                    Log.v("Observer", "Weather:" + cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[7])) +
//                                    " Temperature:" + cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[1])) +
//                                    " Date:" + cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[0])) + "(" + System.currentTimeMillis() + ")" +
//                            " Sunrise:" + cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[9])) +
//                            " Sunset:" + cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[8]))
//                    );
//
////                    WeatherInfo info = new WeatherInfo();
////                    info.Date = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[0]));
////                    info.Temperature = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[1]));
////                    info.Address = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[2]));
////                    info.Location = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[3]));
////                    info.MaxTemperature = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[4]));
////                    info.MinTemperature = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[5]));
////                    info.PM25 = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[6]));
////                    info.Weather = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[7]));
////                    info.Sunset = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[8]));
////                    info.Sunrise = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[9]));
////                    return info;
//                }
            } finally {
                cursor.close();
            }
        }
//        return null;
    }


    // 定义内容观察者监听计步数据变化
//    mObserver = new android.database.ContentObserver(mHandler) {
//        @Override
//        public boolean deliverSelfNotifications() {
//            return super.deliverSelfNotifications();
//        }

    @Override
    public final void onChange(boolean selfChange) {
        super.onChange(selfChange);
        /*int steps = */
        if (mStepEnabled) querySteps();
        if (mWeatherEnabled) fetchWeatherInfo();
//        else Log.e("Observer", "Unknow data on change!");
//            Message.obtain(mHandler, steps).sendToTarget();
    }
//    };

    // 定义Handler，在数据变化时更新UI
//    private Handler mHandler = new Handler() {
//
//        // 未知有乜春用...
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
////            mStepTv.setText(getString(R.string.step_count) + msg.what);
//        }
//
//    };

    // 注册与注销内容观察者，建议在onCreate()中注册，在onDestroy()中注销
    public void ActivateObserverWithType(Uri[] types) {
//        mResolver.registerContentObserver(CONTENT_STEP_URI, true, this);
        for (Uri u : types) {
            mResolver.registerContentObserver(u, true, this);

            if (u.equals(CONTENT_STEP_URI)) {
                mStepEnabled = true;
                querySteps(); // 主动查询一次
                Log.d("Observer", "Step start");
            }
            if (u.equals(WEATHER_URI)) {
                mWeatherInfo = new WeatherInfo();
                mWeatherEnabled = true;
                fetchWeatherInfo();  // 主动查询一次
                Log.d("Observer", "Weather start");
            }
        }
    }

    public void DeactivateObserver() {
        mStepEnabled = false;
        mWeatherEnabled = false;
        mWeatherInfo = null;
        mResolver.unregisterContentObserver(this);
    }

    /**
     * 步行数据变化事件
     * @param steps 当日的步数.
     * @param distance 当日嘅步行距离, 单位是米.
     */
    public void onStepChanged(int steps, int distance) {}

    /**
     * 天气数据变化事件
     * @param weather
     */
    public void onWeatherChanged(WeatherInfo weather) {}
}
