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
import android.os.BatteryManager;
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
    private int mStepCount = 0;
    private String mAccelerometer = "  Accelerometer\n";
    private String mLinearAcceleration = "L.Acceleration\n";
    private String mMagneticField = "MagneticField\n";
    private String mOrientation = "Orientation\n";
    private String mGyroscope = "Gyroscope\n";
    private String mGravity = "Gravity\n";
    private String mRotationVector = "RotationVector\n";

    /**
     * 广播
     */
    private BroadcastReceiver mBroadcastReceiver;
    private String mBatteryString = "Battery:--- --------\n";

    /**
     * 共享数据接收
     */
    private StepsObserver mStepsObserver;
    private int mContentSteps = 0;
    private int mContentDistance = 0;
    private WeatherObserver mWeatherObserver;
    private String mWeather;
    private String mLocation = "\n";
//    private String mContentSteps = "Steps:---/---m(";

    /**
     * 定位服务
     */
//    private LocationService mLocationService;

    public InformationsDrawer(CanvasWatchFaceService.Engine engine, Resources resources, Context context) {
        super(engine, resources, context);
        // 指定系统字体
        Typeface mFace = Typeface.create("Roboto", Typeface.NORMAL);

        // 指定assets 目录入边嘅TTF字体
//        Typeface mFace = Typeface.createFromAsset(mResources.getAssets(), "fonts/Roboto-Thin.ttf");

        // 设置字体画笔
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(13);
//        mTextPaint.setStrokeWidth(1.0f);
        mTextPaint.setAntiAlias(true); // 打开抗锯齿
        mTextPaint.setTypeface(mFace); // 指定字体

        // 使用StaticLayout 显示垂直居中嘅多行文本
        mTextLayout = new StaticLayout("", mTextPaint, mWidthPixels, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // 设置时间格式
//        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.getDefault());
        mDateFormat = new SimpleDateFormat("yyyy/MMMM/d EEEEH:m:s\nz", Locale.getDefault());

        // 激活传感器
        mSensorMonitor = newSensorsMonitor(context);
//        Log.i("InfoDrawer", mSensorMonitor.toString());
//        mSensorInfoString = mSensorMonitor.toString();
//        mSensorMonitor.ActivateAccelerometerSensor();

        // 启动共享数据接收
        activateContentObserver(context);

        // 启动广播接收
        activateBroadcast(context);

        // 蓝牙启动BLE 连接
        activateBluetooth(context);

        // 启动定位服务
//        mLocationService = new LocationService(context);


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
     * 返回当前系统版本名
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
                        mBatteryString +
                        mPhoneConnect + mPhoneBattery +
                        mDateString + mLocation +
                        mWeather +
                        "\n" +
                        mAccelerometer +
                        mLinearAcceleration +
                        mMagneticField +
                        mOrientation +
                        mRotationVector +
                        mGyroscope +
                        mGravity +
                        mHeartString + "Steps:" + mContentSteps + "(" + mStepCount + ")/" + mContentDistance + "m\n" +
                        "\n" +
                        mSystemVersionString +
                        "\nVer. " + mAppVersionString +
                        "\nMagic Install\n---- ----"
                ,
                mTextPaint, mWidthPixels, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false
        );

    }

    @Override
    public void Draw(Canvas canvas, Rect bounds) {
        mDateString = mDateFormat.format(System.currentTimeMillis());
        rebuildTextLayout();
        mTextLayout.draw(canvas);

//        // 竖
//        int S1x = 28, S2x = 88, S3x = 129, S4x = 189, S5x = 253, S6x = 291;
//        canvas.drawLine(S1x, 0, S1x, 319, mTextPaint);
//        canvas.drawLine(S2x, 0, S2x, 319, mTextPaint);
//        canvas.drawLine(S3x, 0, S3x, 319, mTextPaint);
//        canvas.drawLine(S4x, 0, S4x, 319, mTextPaint);
//        canvas.drawLine(S5x, 0, S5x, 319, mTextPaint);
//        canvas.drawLine(S6x, 0, S6x, 319, mTextPaint);
//        // 横
//        int W1y = 9, W2y = 40, W3y = 88, W7y = 110, W4y = 136, W5y = 238, W6y = 271;
//        canvas.drawLine(0, W1y, 319, W1y, mTextPaint);
//        canvas.drawLine(0, W2y, 319, W2y, mTextPaint);
//        canvas.drawLine(0, W3y, 319, W3y, mTextPaint);
//        canvas.drawLine(0, W7y, 319, W7y, mTextPaint);
//        canvas.drawLine(0, W4y, 319, W4y, mTextPaint);
//        canvas.drawLine(0, W5y, 319, W5y, mTextPaint);
//        canvas.drawLine(0, W6y, 319, W6y, mTextPaint);


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
                            Sensor.TYPE_STEP_COUNTER,
//                            Sensor.TYPE_HEART_RATE
                    }
            );
        }
        else {
            mSensorMonitor.DeactivateSensors();
//            mBroadcastReceiver.DeactivateReceiver();
        }
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
        mBluetoothStatus.ConnectGATT();
    }

    /**
     * 启动共享数据接收
     * @param context
     */
    private void activateContentObserver(Context context) {
        mStepsObserver = new StepsObserver(context) {
            /**
             * 步行数据变化事件
             *
             * @param steps    当日的步数.
             * @param distance 当日嘅步行距离, 单位是米.
             */
            @Override
            public void onStepChanged(int steps, int distance) {
                super.onStepChanged(steps, distance);
                mContentSteps = steps;
                mContentDistance = distance;
            }
        };
        mStepsObserver.ActivateObserver();

        mWeatherObserver = new WeatherObserver(context){
            /**
             * 天气数据变化事件
             *
             * @param weather
             */
            @Override
            public void onWeatherChanged(WeatherInfo weather) {
                super.onWeatherChanged(weather);
                mWeather = String.format("%s %.0f°(%.0f°~%.0f°) PM2.5:%.0f\n",
                        weather.StringStutas, weather.Temperature, weather.MaxTemperature, weather.MinTemperature, weather.PM25
                );
                mLocation = " " + weather.Location + "\n";
            }
        };
        mWeatherObserver.ActivateObserver();
    }

    /**
     * 启动广播接收
     * @param context
     */
    private void activateBroadcast(Context context) {
        mBroadcastReceiver = new BroadcastReceiver(context) {
            /**
             * 电量变化事件
             * </br>
             * 由于电压变化同样会触发哩个事件, 所以多次事件之间的电量值可能会一样.
             *
             * @param level   电量, 单位是百分比(10 = 10%, 100 = 100%, ...);
             * @param status  充电状态:
             *                BatteryManager.BATTERY_STATUS_UNKNOWN = 1;
             *                BatteryManager.BATTERY_STATUS_CHARGING = 2;
             *                BatteryManager.BATTERY_STATUS_DISCHARGING = 3;
             *                BatteryManager.BATTERY_STATUS_NOT_CHARGING = 4;
             *                BatteryManager.BATTERY_STATUS_FULL = 5;
             * @param plugged true = 连接上充电器
             */
            @Override
            public void onBatteryChanged(int level, int status, boolean plugged) {
                super.onBatteryChanged(level, status, plugged);
                switch(status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        mBatteryString = "Battery:" + level + "% Charging\n";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        mBatteryString = "Battery:" + level + "% Discharging\n";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        mBatteryString = "Battery:" + level + "% Not charging\n";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        mBatteryString = "Battery:" + level + "% Full\n";
                        break;
                    default:
                        mBatteryString = "Battery:" + level + "% Unknow\n";
                        break;
                }
            }

            /**
             * 收到飞鸡模式切换广播事件
             *
             * @param state true = 已切换到飞鸡模式
             */
            @Override
            public void onAirplaneMode(boolean state) {
                super.onAirplaneMode(state);
                if (state == true) {
                    // 关闭连接
                    mBluetoothStatus.DisconnectGATT();

                    mPhoneConnect = "Phone: "+ (mBluetoothStatus.getIsConnected() ? "Connected " : "Disconnected ");
                    if (!mBluetoothStatus.getIsConnected()) mPhoneBattery = "bat.---\n";
                } else {
                    // 重连手机
                    mBluetoothStatus.ConnectGATT();
                }
            }
        };
        mBroadcastReceiver.ActivateReceiverWithType(new String[]{
                        BroadcastReceiver.ACTION_BATTERY_CHANGED,
                        BroadcastReceiver.ACTION_BATTERY_LOW,
                        BroadcastReceiver.ACTION_BATTERY_OKAY,
                        BroadcastReceiver.ACTION_POWER_CONNECTED,
                        BroadcastReceiver.ACTION_POWER_DISCONNECTED,
                        BroadcastReceiver.ACTION_AIRPLANE_MODE_CHANGED,
                        BroadcastReceiver.ACTION_LOCALE_CHANGED
                }
        );
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
             * 方向传感器(软件)角度变化事件.
             * 0.23mA+6.8mA高耗电
             * <p/>
             * 哩个事件必须依赖加速度传感器同磁力传感器.
             *
             * @param azimuth 表示手机顶部朝向与正北方向的角度, 值的范围是360度.
             *                该角度值为0时，表示手机顶部指向正北；
             *                该角度为90度时，代表手机顶部指向正东；
             *                该角度为180度时，代表手机顶部指向正南；
             *                该角度为270度时，代表手机顶部指向正西.
             * @param pitch   表示手机顶部或尾部翘起的角度, 值的范围是-180到180度.
             *                假设将手机屏幕朝上水平放在桌子上，如果桌子是完全水平的，该角度应该是0;
             *                假如从手机顶部抬起，直到将手机沿x轴旋转180度（屏幕向下水平放在桌面上），这个过程中，该角度值会从0变化到-180;
             *                如果从手机底部开始抬起，直到将手机沿x轴旋转180度（屏幕向下水平放在桌面上），该角度的值会从0变化到180.
             * @param roll    表示手机左侧或右侧翘起的角度, 值的范围是-180到180度.
             *                将手机屏幕朝上水平放在桌子上，如果桌子是完全水平的，该角度应该是0;
             *                假如将手机左侧逐渐抬起，直到将手机沿Y轴旋转90度（手机与桌面垂直），在这个旋转过程中，该角度会从0变化到-90;
             */
            @Override
            public void onOrientationChanged(float azimuth, float pitch, float roll) {
                super.onOrientationChanged(azimuth, pitch, roll);
                mOrientation = String.format("Orientation:%.2f %.2f %.2f\n",
                        azimuth, pitch, roll
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
                mStepCount = steps;


//                mLocationService.getLocation();
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
