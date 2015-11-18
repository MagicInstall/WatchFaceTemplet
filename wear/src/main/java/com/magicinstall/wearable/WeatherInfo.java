package com.magicinstall.wearable;

import android.database.Cursor;

import java.util.Date;

/**
 * 天气信息
 */
public class WeatherInfo{
    /**
     * 时间
     */
    public Date date;
    /**
     * 温度
     */
    public float Temperature;
    /**
     * 具体地点
     * </br>
     * 屌9佢嗰mobvoi!!! 唔得就唔好整啦!!!
     * */
    @Deprecated
    public String Address;
    /**
     * 地区
     */
    public String Location;
    /**
     * 最高温度
     */
    public float MaxTemperature;
    /**
     * 最低温度
     */
    public float MinTemperature;
    /**
     * PM2.5
     * 单位喺 微克/立方米
     */
    public float PM25;
    /**
     * 天气情况
     */
    public String Stutas;
    /**
     * 日出时间
     * </br>
     * 屌9佢嗰mobvoi!!! 唔得就唔好整啦!!!
     * */
    @Deprecated
    public String Sunrise;
    /**
     * 日落时间
     * </br>
     * 屌9佢嗰mobvoi!!! 唔得就唔好整啦!!!
     */
    @Deprecated
    public String Sunset;

    private static final String[] COLUMN_NAMES = { "time", "temp", "address", "location", "maxtemp", "mintemp", "pm25", "weather", "sunset", "sunrise" };

    public WeatherInfo() {}
    /**
     * 一定要屌9佢嗰mobvoi 团队!!!
     * 有简单嘅基本类型唔用, 共享数据全部属性做9成String, 依家要转反去基本类型, 喺咪多9余吖!!!
     * 转换使用硬编码方式, 如果mobvoi 修改咗其中一个属性嘅表示方式, 就要跟住改过, 到时又要屌9佢!!!
     * @param cursor
     */
    public WeatherInfo(Cursor cursor) {
        if (cursor == null) return;

//        try {
            if (cursor.moveToFirst()) {
                date = new Date(Long.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[0]))));
                Temperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[1])));
                Address = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[2]));
                Location = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[3]));
                MaxTemperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[4])));
                MinTemperature = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[5])));
                PM25 = Float.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[6])));
                Sunrise = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[9]));
                Sunset = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[8]));
                Stutas = cursor.getString(cursor.getColumnIndex(COLUMN_NAMES[7]));
            }
//        } finally {
//            cursor.close();
//        }

    }

    @Override
    public String toString() {
        return String.format("WeatherInfo{%s %s %.1fC(MaxTemp.%sC MinTemp.%sC) PM25:%.1f (%s)%s Sunrise: Sunset:}",
                date, Stutas, Temperature, MaxTemperature, MinTemperature, PM25, Location, Address, Sunrise, Sunset
        );
    }
}
