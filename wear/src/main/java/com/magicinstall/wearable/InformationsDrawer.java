package com.magicinstall.wearable;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.os.Build;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by wing on 15/11/7.
 */
public class InformationsDrawer extends WatchFaceDrawer{
    private String mSystemVersionString;
    private String mAppVersionString;

    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;

    private String mDateString;
    private DateFormat mDateFormat;

    private long  mPrevFrameMs;

    /**
     * 蓝牙状态对象
     */
    BluetoothStatus mBluetoothStatus;
    private String mPhoneConnect = "Phone: Disconnected ";
    private String mPhoneBattery = "bat.---\n";

    /**
     * 传感器对象
     */
    private SensorMonitor mSensorMonitor;
    private String mSensorInfoString;
    private String mHeartString = "HeartRate:--- | ";
    private String mStepCount = "Steps:---\n";
    private String mAccelerometer = "  Accelerometer\n";
    private String mLinearAcceleration = "L.Acceleration\n";
    private String mMagneticField = "MagneticField\n";
    private String mGyroscope = "Gyroscope\n";
    private String mGravity = "Gravity\n";
    private String mRotationVector = "RotationVector\n";

    public InformationsDrawer(CanvasWatchFaceService.Engine engine, Resources resources, Context context) {
        super(engine, resources, context);
        // 指定系统字体
        Typeface mFace = Typeface.create("Roboto", Typeface.NORMAL);

        // 指定assets 目录入边嘅TTF字体
//        Typeface mFace = Typeface.createFromAsset(mResources.getAssets(), "fonts/Roboto-Thin.ttf");

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

        // 激活传感器
        mSensorMonitor = newSensorsMonitor(context);
//        Log.i("InfoDrawer", mSensorMonitor.toString());
//        mSensorInfoString = mSensorMonitor.toString();
//        mSensorMonitor.ActivateAccelerometerSensor();


        // 蓝牙启动BLE 连接
        activateBluetooth(context);

        mAppVersionString = getAppVersionName(context);
        mSystemVersionString = getSystemVersionName();
    }

    /*
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    */

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = null;
        int versioncode = -1;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("getAppVersionName", "Exception", e);
        }
        return versionName;
    }

    /**
     * 返回当前程序版本名
     */
    public static String getSystemVersionName() {
        return /*"Product Model: " +*/ android.os.Build.MODEL + ","
                + Build.VERSION.SDK_INT + ","
                + android.os.Build.VERSION.RELEASE ;
    }


    /**
     * 重新组成字串, 新建一个居中Layout
     */
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
                str_ambient
                        +
                        mWidthPixels + "x" + mHeightPixels + str_FPS +
                        mPhoneConnect + mPhoneBattery +
                        mHeartString + mStepCount +
                        mDateString +
                        mAccelerometer +
                        mLinearAcceleration +
                        mMagneticField +
                        mRotationVector +
                        mGyroscope +
                        mGravity +
                        "Ver. " + mAppVersionString
                ,
                mTextPaint, mWidthPixels, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false
        );

    }

    @Override
    public void Draw(Canvas canvas, Rect bounds) {
        mDateString = mDateFormat.format(System.currentTimeMillis()) + "\n";
        rebuildTextLayout();
        mTextLayout.draw(canvas);
    }

    /**
     * 交互模式切换事件
     * @param inInteractiveMode
     */
    @Override
    protected void onInteractiveModeChanged(boolean inInteractiveMode) {
//        super.onInteractiveModeChanged(inInteractiveMode);
        if (inInteractiveMode) {
            mSensorMonitor.ActivateSensorsWithType(new int[]{
                            Sensor.TYPE_ACCELEROMETER,
                            Sensor.TYPE_MAGNETIC_FIELD,
                            Sensor.TYPE_GYROSCOPE,
                            Sensor.TYPE_GRAVITY,
                            Sensor.TYPE_LINEAR_ACCELERATION,
                            Sensor.TYPE_ROTATION_VECTOR,
                            Sensor.TYPE_STEP_COUNTER
//                            Sensor.TYPE_HEART_RATE
                    }
            );
        }
        else mSensorMonitor.DeactivateSensors();
    }

    /**
     * 激活BLE 连接取得电量
     * @param context
     */
    private void activateBluetooth(Context context) {
        mBluetoothStatus = new BluetoothStatus(context) {
            /**
             * 手机端电量变化事件
             * @param batteryLevel 手机端的电量, 单位是百分比(10 = 10%, 100 = 100%, ...)
             */
            @Override
            public void onBatteryLevelChanged(int batteryLevel) {
                super.onBatteryLevelChanged(batteryLevel);
//                Log.i("SensorMoor", "onBatteryLevelChanged:" + batteryLevel + "%");
                mPhoneBattery = "bat." + batteryLevel + "%\n";
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
                if (!isConnnected) mPhoneBattery = "bat.---\n";
            }
        };
    }

    /**
     * 新建一个匿名继承SensorsMonitor 的实例, 方便写事件代码.
     * @param context
     * @return
     */
    private SensorMonitor newSensorsMonitor(Context context) {
        return new SensorMonitor(context) {
            /**
             * 1. 加速度传感器值改变事件.
             * 0.23mA低耗电
             */
            @Override
            public void onAccelerometerChanged(float x, float y, float z) {
                super.onAccelerometerChanged(x, y, z);
                mAccelerometer = String.format("  Accelerometer:%.2f %.2f %.2f\n",
                        x, y, z
                );
            }

            /**
             * 2. 磁力传感器值改变事件.
             * 6.8mA高耗电
             */
            @Override
            public void onMagneticFieldChanged(float x, float y, float z) {
                super.onMagneticFieldChanged(x, y, z);
                mMagneticField = String.format("MagneticField:%.2f %.2f %.2f\n",
                        x, y, z
                );
            }

            /**
             * 4. 陀螺仪传感器值改变事件.
             * 6.1mA高耗电
             */
            @Override
            public void onGyroscopeChanged(float x, float y, float z) {
                super.onGyroscopeChanged(x, y, z);
                mGyroscope = String.format("Gyroscope:%.2f %.2f %.2f\n",
                        x, y, z
                );
            }

            /**
             * 9. 重力传感器值改变事件.
             * 0.2mA低耗电
             */
            @Override
            public void onGravityChanged(float x, float y, float z) {
                super.onGravityChanged(x, y, z);
                mGravity = String.format("Gravity:%.2f %.2f %.2f\n",
                        x, y, z
                );
            }

            /**
             * 10. 线性加速度传感器值改变事件.
             * 0.2mA低耗电
             */
            @Override
            public void onLinearAccelerationChanged(float x, float y, float z) {
                super.onLinearAccelerationChanged(x, y, z);
                mLinearAcceleration = String.format("L.Acceleration:%.2f %.2f %.2f\n",
                        x, y, z
                );
            }

            /**
             * 11. 旋转量传感器值改变事件.
             * 6.1mA高耗电
             */
            @Override
            public void onRotationVectorChanged(float x, float y, float z, float cosHalfOfTheta, float heading) {
                super.onRotationVectorChanged(x, y, z, cosHalfOfTheta, heading);
                mRotationVector = String.format("RotationVector:%.2f %.2f %.2f %.2f\n",
                        x, y, z, cosHalfOfTheta
                );
            }

            /**
             * 19. 计步器值改变事件.
             * 6.1mA高耗电?
             */
            @Override
            public void onStepCounterChanged(int steps) {
                super.onStepCounterChanged(steps);
                mStepCount = "Steps:" + steps + "\n";
            }

            /**
             * 21. 心率传感器值改变事件.
             * 1.0mA低耗电
             */
            @Override
            public void onHeartRateChanged(float rate) {
                super.onHeartRateChanged(rate);
                mHeartString = "HeartRate:" + rate + "/Minute | ";
            }
        };
    }
}
