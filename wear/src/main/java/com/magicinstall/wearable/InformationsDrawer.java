package com.magicinstall.wearable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by wing on 15/11/7.
 */
public class InformationsDrawer extends WatchFaceDrawer implements SensorsEventCallback{
//    private String mDrawString;
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;

//    private GregorianCalendar mCalendar;
//    private Date mdate;
    private String mDateString;
    private DateFormat mDateFormat;

//    private float mFPS;
    private long  mPrevFrameMs;

//    private Size mDisplaySize;

    private String mSensorInfoString;

    /**
     * 蓝牙状态对象
     */
    BluetoothStatus mBluetoothStatus;
    private String mPhoneConnect = "Phone: Disconnected ";
    private String mPhoneBattery = "battery:---\n";



    public InformationsDrawer(CanvasWatchFaceService.Engine engine, Resources resources, Context context) {
        super(engine, resources, context);
        // 指定系统字体
//        Typeface mFace = Typeface.create("Roboto", Typeface.NORMAL);

        // 指定assets 目录入边嘅TTF字体
        Typeface mFace = Typeface.createFromAsset(mResources.getAssets(), "fonts/Roboto-Thin.ttf");

        // 设置字体画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(14);
        mTextPaint.setAntiAlias(true); // 打开抗锯齿
        mTextPaint.setTypeface(mFace); // 指定字体

        // 使用StaticLayout 显示垂直居中嘅多行文本
        mTextLayout = new StaticLayout("", mTextPaint, mWidthPixels, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // 设置时间格式
//        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.getDefault());
        mDateFormat = new SimpleDateFormat("yyyy/MMMM/d EEEE\nH:m:s z", Locale.getDefault());

        mSensorInfoString = mHardwear.toString();
        mHardwear.setSensorEventReceiver(this);
//        mHardwear.ActivateAccelerometerSensor();

        activateBluetooth(context);
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
//        String str_visible = (getIsVisible() ? "Visible" : "Invisible") + "\n";

        // 刷新模式
        String str_ambient = (getIsAmbient() ? "Ambient" : "Interactive") + "\n";

        // 帧率
        String str_FPS =
                " FPS:" +
                String.format("%.2f", 1000.0f / (System.currentTimeMillis()- mPrevFrameMs))
                + "\n";
        mPrevFrameMs = System.currentTimeMillis();

        mTextLayout = new StaticLayout(
//                str_visible +
                    str_ambient +
                    mWidthPixels + "x" + mHeightPixels + str_FPS +
                    mPhoneConnect + mPhoneBattery +
                    mSensorInfoString +
                    mDateString,
                    mTextPaint, mWidthPixels, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false
        );

    }

    @Override
    public void Draw(Canvas canvas, Rect bounds) {
        mDateString = mDateFormat.format(System.currentTimeMillis());
        rebuildTextLayout();
        mTextLayout.draw(canvas);
    }

    @Override
    protected void onInteractiveModeChanged(boolean inInteractiveMode) {
//        super.onInteractiveModeChanged(inInteractiveMode);
        if (inInteractiveMode) {
//            mHardwear.ActivateSensorsWithType(new int[]{
//                            SensorMonitor.TYPE_ACCELEROMETER
//                    }
//            );
        }
        else mHardwear.DeactivateSensors();
    }

    /**
     * 激活BLE 连接取得电量
     * @param context
     */
    protected void activateBluetooth(Context context) {
        mBluetoothStatus = new BluetoothStatus(context) {
            /**
             * 手机端电量变化事件
             * @param batteryLevel 手机端的电量, 单位是百分比(10 = 10%, 100 = 100%, ...)
             */
            @Override
            public void onBatteryLevelChanged(int batteryLevel) {
                super.onBatteryLevelChanged(batteryLevel);
//                Log.i("SensorMonitor", "onBatteryLevelChanged:" + batteryLevel + "%");
                mPhoneBattery = "battery:" + batteryLevel + "%\n";
            }

            /**
             * 手机连接状态变化事件
             *
             * @param isConnnected true = 已连接
             */
            @Override
            public void onConnectStatusChanged(boolean isConnnected) {
                super.onConnectStatusChanged(isConnnected);

                mPhoneConnect = "Phone: "+ (isConnnected ? "Connected " : "Disconnected ");
                if (!isConnnected) mPhoneBattery = "battery:---\n";
            }
        };
    }

    /**
     * 加速度传感器值改变事件.
     */
    @Override
    public void onAccelerationChanged(float x, float y, float z) {
//        System.out.println("needPostInvalidate");
//        mCallbakeReceiver.needPostInvalidate();
    }
}
