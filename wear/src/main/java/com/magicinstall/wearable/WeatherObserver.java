package com.magicinstall.wearable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Date;

/**
 * Created by wing on 15/11/19.
 */
public class WeatherObserver extends android.database.ContentObserver{
    /**
     * @param context 传入Engine 的BaseContext.
     */
    public WeatherObserver(Context context/*, Handler handler*/){
        super(new Handler());
        mContext = context;
        mResolver = context.getContentResolver();
    }

    @Override
    protected void finalize() throws Throwable {
        DeactivateObserver();
        Log.d("WeatherObserver", "finalize - unregisterContentObserver(this)");
        super.finalize();
    }

    // mobvoi 共享嘅天气数据
    private static final Uri WEATHER_URI = Uri.parse("content://com.mobvoi.provider.weather");
    private static final String[] COLUMN_NAMES = { "time", "temp", "address", "location", "maxtemp", "mintemp", "pm25", "weather", "sunset", "sunrise" };

    /**
     * Engine 的BaseContext
     */
    protected Context mContext;

    /**
     * 内容解析者
     */
    private android.content.ContentResolver mResolver;

    protected WeatherInfo mWeatherInfo;

    /**
     * 解释天气数据
     */
    private void queryWeather() {
        Cursor cursor = mResolver.query(WEATHER_URI, COLUMN_NAMES, null, null, null);
        if (cursor != null) {
            try {
                WeatherInfo info = new WeatherInfo();
                /*
                 * 一定要屌9佢嗰mobvoi 团队!!!
                 * 有简单嘅基本类型唔用, 共享数据全部属性做9成String, 依家要转反去基本类型, 喺咪多9余吖!!!
                 * 转换使用硬编码方式, 如果mobvoi 修改咗其中一个属性嘅表示方式, 就要跟住改过, 到时又要屌9佢!!!
                 * @param cursor
                 */
                if (cursor.moveToFirst()) {
                    info.date = new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[0]))));
                    info.Temperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[1])));
                    info.Address = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[2]));
                    info.Location = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[3]));
                    info.MaxTemperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[4])));
                    info.MinTemperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[5])));
                    info.PM25 = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[6])));
                    info.Sunrise = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[9]));
                    info.Sunset = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[8]));
                    info.setStringStutas(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[7])));
                }

                Log.v("WeatherObserver", info.toString());
                onWeatherChanged(info);
            } finally {
                cursor.close();
            }
        }
    }

    // 收到数据变化通知, 哩个事件本身系唔会返回数据嘅, 要自行查询
    @Override
    public final void onChange(boolean selfChange) {
        super.onChange(selfChange);
       queryWeather();
    }

    /**
     * 激活数据变化监视器.
     */
    public void ActivateObserver() {
        mResolver.registerContentObserver(WEATHER_URI, true, this);

        Log.d("WeatherObserver", "Weather start");
        queryWeather();  // 主动查询一次
    }

    /**
     * 注销数据变化监视器.
     */
    public void DeactivateObserver() {
        mResolver.unregisterContentObserver(this);
    }

    /**
     * 天气数据变化事件
     * @param weather
     */
    public void onWeatherChanged(WeatherInfo weather) {}
}
