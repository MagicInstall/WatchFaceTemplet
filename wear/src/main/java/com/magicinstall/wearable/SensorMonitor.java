package com.magicinstall.wearable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

/**
 * Created by wing on 15/11/10.
 * 用法很简单, 只需要用匿名继承哩个类, 重写onXXXChanged 方法就可以, 代码非常简洁;
 * 用ActivateSensorsWithType 启动需要的传感器, 喺对应的onXXXChanged 方法内就可以得到相应的传感器数值变化;
 * 关屏或环境模式下用DeactivateSensors 停止传感器可以悭反D电,
 * 可以随时再启动需要的传感器(例如切换至交互模式).
 */
public class SensorMonitor implements SensorEventListener {
    /**
     * @param context 传入Engine 的BaseContext.
     */
    public SensorMonitor(Context context) {
        mContext = context;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d("SensorMonitor", "finalize - unregisterListener(this)");
        DeactivateSensors();
        super.finalize();
    }

    /**
     * Engine 的BaseContext
     */
    protected Context mContext;

    /**
     * 传感器管理对象.
     */
    protected SensorManager mSensorManager;

    // 测试用
    protected Sensor mAccelerometerSensor;


    // 计算方向
    private float[] mAccelerometerValues = new float[3];
    private boolean mAccelerometerChanged = false;
    private float[] mMagneticFieldValues = new float[3];
    private boolean mMagneticFieldChanged = false;

    /**
     * 停止监听所有传感器.
     */
    public void DeactivateSensors() {
        mSensorManager.unregisterListener(this);
    }

    /**
     * 激活指定的传感器.
     * @param types 最好系写成Sensor.TYPE_XXXX 的数组, 有助提高代码可读性.
     */
    public void ActivateSensorsWithType(int[] types) {
        boolean result;
        Sensor sensor;
        for (int t : types) {
            sensor = mSensorManager.getDefaultSensor(t);
            if (sensor == null) {
                Log.e("SensorMonitor", "Sensors(Type:" + t + ") does not exist!");
                continue;
            }
            result = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (!result) {
                Log.e("SensorMonitor", "Sensors(Type:" + t + ") Unable to register!");
            }
        }
    }

    /**
     * 传感器精度改变事件.
     * 由于唔多常用, 所以唔为每种传感器单独实现哩个事件
     * @param sensor 发生精度变化嘅传感器
     * @param i 精度, SENSOR_STATUS_ACCURACY_XXX
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d("SensorMonitor",
                "onAccuracyChanged - sensor:" +
                        sensor.getType() +
                        sensor.getName() +
                        " Accuracy:" + i
        );
    }


    /**
     * 传感器值改变事件
     * 除非要添加新嘅传感器事件, 否则一般唔使重写哩个事件,
     * 佢内部只系将数据简单分发至具体某个传感器对应的onXXXXChanged 事件,
     * 使用onXXXXChanged 事件更方便, 代码更简洁.
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            // 1. 加速度
            case Sensor.TYPE_ACCELEROMETER:
                Log.v("SensorMonitor",
                        String.format(
                                "Accelerometer %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onAccelerometerChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                // 计算方向
                if (mMagneticFieldChanged) {
                    calculateOrientation(); // 交俾佢调用方向变化事件
                    mMagneticFieldChanged = false;
                    mAccelerometerChanged = false;
                }
                // 只更新加速度
                else {
                    mAccelerometerValues = sensorEvent.values;
                    mAccelerometerChanged = true;
                }
                break;

            // 2. 磁力场
            case Sensor.TYPE_MAGNETIC_FIELD:
                Log.v("SensorMonitor",
                        String.format(
                                "MagneticField %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onMagneticFieldChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                // 计算方向
                if (mAccelerometerChanged) {
                    calculateOrientation(); // 交俾佢调用方向变化事件
                    mMagneticFieldChanged = false;
                    mAccelerometerChanged = false;
                }
                // 只更新磁力场
                else {
                    mMagneticFieldValues = sensorEvent.values;
                    mAccelerometerChanged = true;
                }
                break;

            // 4. 陀螺仪
            case Sensor.TYPE_GYROSCOPE:
                Log.v("SensorMonitor",
                        String.format(
                                "Gyroscope %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onGyroscopeChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 5. 光照(Ticwatch 冇哩个传感器)
            case Sensor.TYPE_LIGHT:
                Log.v("SensorMonitor",
                        String.format("Light %f",
                                sensorEvent.values[0]
                        )
                );
                onLightChanged(sensorEvent.values[0]);
                break;

            // 6. 压力(Ticwatch 一直返回0)
            case Sensor.TYPE_PRESSURE:
                Log.v("SensorMonitor",
                        String.format("Pressure %f",
                                sensorEvent.values[0]
                        )
                );
                onPressureChanged(sensorEvent.values[0]);
                break;

            // 8. 接近(Ticwatch 一直返回0)
            case Sensor.TYPE_PROXIMITY:
                Log.v("SensorMonitor",
                        String.format("Proximity %f",
                                sensorEvent.values[0]
                        )
                );
                onProximityChanged(sensorEvent.values[0]);
                break;

            // 9. 重力
            case Sensor.TYPE_GRAVITY:
                Log.v("SensorMonitor",
                        String.format(
                                "Gravity %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onGravityChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 10. 线性加速度
            case Sensor.TYPE_LINEAR_ACCELERATION:
                Log.v("SensorMonitor",
                        String.format(
                                "LinearAcceleration %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onLinearAccelerationChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 11. 旋转量(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_ROTATION_VECTOR:
                Log.v("SensorMonitor",
                        String.format(
                                "RotationVector %f %f %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2],
                                sensorEvent.values[3],
                                sensorEvent.values[4]
                        )
                );
                onRotationVectorChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2],
                        sensorEvent.values[3],
                        sensorEvent.values[4]
                );
                break;

            // 14. 未校准磁力场(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                Log.v("SensorMonitor",
                        String.format(
                                "MagneticFieldUncalibrated %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onMagneticFieldUncalibratedChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 16. 未校准陀螺仪(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                Log.v("SensorMonitor",
                        String.format(
                                "GyroscopeUncalibrated %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                onGyroscopeUncalibratedChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 18. 步行检测，每走一步就触发一次事件(Ticwatch 一直冇触发)
            case Sensor.TYPE_STEP_DETECTOR:
                Log.v("SensorMonitor","StepDetector occurred");
                onStepDetectorChanged();
                break;

            // 19. 步数记录
            case Sensor.TYPE_STEP_COUNTER:
                Log.v("SensorMonitor", "StepCounter " + sensorEvent.values[0]);
                onStepCounterChanged((int)sensorEvent.values[0]);
                break;

            // 21. 心率
            case Sensor.TYPE_HEART_RATE:
                Log.v("SensorMonitor", "HeartRate " + sensorEvent.values[0]);
                onHeartRateChanged(sensorEvent.values[0]);
                break;

            // Android API 弃用
            case Sensor.TYPE_ORIENTATION: // 3. 方向(Ticwatch 冇讲有哩个传感器)
            default:
                Log.w("SensorMonitor",
                        String.format(
                                "%s %s(Unknow sensor) %f %f %f",
                                sensorEvent.sensor.getType(),
                                sensorEvent.sensor.getName(),
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                break;
        }
    }

    /**
     * 计算方向
     */
    private void calculateOrientation() {
        float[] c_values = new float[3];
        float[] r_matrix = new float[9];
        SensorManager.getRotationMatrix(r_matrix, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(r_matrix, c_values);
        Log.v("SensorMonitor",
                String.format(
                        "Orientation %f %f %f",
                        (float) Math.toDegrees(c_values[0]),
                        (float) Math.toDegrees(c_values[1]),
                        (float) Math.toDegrees(c_values[2])
                )
        );
        onOrientationChanged(
                (float) Math.toDegrees(c_values[0]),
                (float) Math.toDegrees(c_values[1]),
                (float) Math.toDegrees(c_values[2])
        );
    }

    /**
     * 1. 加速度传感器值改变事件.
     * 0.23mA低耗电
     *
     * 将手机平放在桌面上，x轴默认为0，y轴默认0，z轴默认9.81。
     * 将手机朝下放在桌面上，z轴为-9.81。
     * 将手机向左倾斜，x轴为正值。
     * 将手机向右倾斜，x轴为负值。
     * 将手机向上倾斜，y轴为负值。
     * 将手机向下倾斜，y轴为正值。
     *
     * maxRange:19.61
     */
    public void onAccelerometerChanged(float x, float y, float z){}

    /**
     * 2. 磁力传感器值改变事件.
     * 6.8mA高耗电
     *
     * 返回x、y、z三轴的环境磁场数据。
     *
     * 硬件上一般没有独立的磁力传感器，磁力数据由电子罗盘传感器提供（E-compass）;
     * 电子罗盘传感器同时提供方向传感器数据。
     *
     * maxRange:200, 该数值的单位是微特斯拉（micro-Tesla），用uT表示
     */
    public void onMagneticFieldChanged(float x, float y, float z){}

    /**
     * 方向传感器(软件)角度变化事件.
     * 0.23mA+6.8mA高耗电
     *
     * 哩个事件必须依赖加速度传感器同磁力传感器.
     *
     * @param azimuth 表示手机顶部朝向与正北方向的角度, 值的范围是360度.
     *                该角度值为0时，表示手机顶部指向正北；
     *                该角度为90度时，代表手机顶部指向正东；
     *                该角度为180度时，代表手机顶部指向正南；
     *                该角度为270度时，代表手机顶部指向正西.
     * @param pitch 表示手机顶部或尾部翘起的角度, 值的范围是-180到180度.
     *              假设将手机屏幕朝上水平放在桌子上，如果桌子是完全水平的，该角度应该是0;
     *              假如从手机顶部抬起，直到将手机沿x轴旋转180度（屏幕向下水平放在桌面上），这个过程中，该角度值会从0变化到-180;
     *              如果从手机底部开始抬起，直到将手机沿x轴旋转180度（屏幕向下水平放在桌面上），该角度的值会从0变化到180.
     * @param roll 表示手机左侧或右侧翘起的角度, 值的范围是-180到180度.
     *             将手机屏幕朝上水平放在桌子上，如果桌子是完全水平的，该角度应该是0;
     *             假如将手机左侧逐渐抬起，直到将手机沿Y轴旋转90度（手机与桌面垂直），在这个旋转过程中，该角度会从0变化到-90;
     *             如果从手机的右侧开始抬起，直到将手机沿Y轴旋转90度（手机与桌面垂直），该角度的值会从0变化到90度.
     */
    public void onOrientationChanged(float azimuth, float pitch, float roll) {}

    /**
     * 4. 陀螺仪传感器值改变事件.
     * 6.1mA高耗电
     *
     * 当手机逆时针旋转时，角速度为正值，顺时针旋转时，角速度为负值。
     * 陀螺仪传感器经常被用来计算手机已转动的角度.
     *
     * 返回x、y、z三轴的角加速度数据.
     * 角加速度的单位是radians/second。
     *
     * 根据Nexus S手机实测：
     * 水平逆时针旋转，Z轴为正。
     * 水平逆时针旋转，z轴为负。
     * 向左旋转，y轴为负。
     * 向右旋转，y轴为正。
     * 向上旋转，x轴为负。
     * 向下旋转，x轴为正。
     *
     * maxRange:40
     */
    public void onGyroscopeChanged(float x, float y, float z){}

    /**
     * 5. 光照强度传感器值改变事件.
     * Ticwatch 冇哩个传感器
     *
     * 主要用于Android系统的LCD自动亮度功能.
     *
     * Android SDK将光线强度分为不同的等级，
     * 每一个等级的最大值由一个常量表示，
     * 这些常量都定义在SensorManager类中，代码如下：
     * LIGHT_SUNLIGHT_MAX =120000.0f
     * LIGHT_SUNLIGHT=110000.0f
     * LIGHT_SHADE=20000.0f
     * LIGHT_OVERCAST= 10000.0f
     * LIGHT_SUNRISE= 400.0f
     * LIGHT_CLOUDY= 100.0f
     * LIGHT_FULLMOON= 0.25f
     * LIGHT_NO_MOON= 0.001f
     *
     * @param lux 单位是lux
     */
    @Deprecated
    public void onLightChanged(float lux){}

    /**
     * 6. 压力传感器值改变事件.
     * Ticwatch 一直返回0
     *
     * @param hPa 压力传感器返回当前的压强，单位是百帕斯卡hectopascal（hPa）
     */
    @Deprecated
    public void onPressureChanged(float hPa){}

    /**
     * 8. 接近传感器值改变事件.
     * Ticwatch 一直返回0
     *
     * @param cm 单位是厘米
     */
    @Deprecated
    public void onProximityChanged(float cm){}

    /**
     * 9. 重力传感器值改变事件.
     * 0.2mA低耗电
     *
     * 倾斜传感器?
     *
     * 对于加速度传感器, 当设备处于静止时，重力传感器的输出应该是相同的。
     *
     * maxRange:9.81, 单位是m/s^2
     */
    public void onGravityChanged(float x, float y, float z){}

    /**
     * 10. 线性加速度传感器值改变事件.
     * 0.2mA低耗电
     *
     * 线性加速度传感器是加速度传感器减去重力影响获取的数据.
     * 加速度传感器、重力传感器和线性加速度传感器的计算公式如下：
     * 加速度 = 重力 + 线性加速度
     *
     * maxRange:19.61, 单位是m/s^2
     */
    public void onLinearAccelerationChanged(float x, float y, float z){}

    /**
     * 11. 旋转量传感器值改变事件.
     * 6.1mA高耗电
     *
     * Ticwatch 冇讲有哩个传感器
     *
     * 代表设备的方向，是一个将坐标轴和角度混合计算得到的数据
     *
     * 好卵复杂唔识解释...
     *
     * @param x
     * @param y
     * @param z
     * @param cosHalfOfTheta
     * @param heading Ticwear 一直返回0
     */
    public void onRotationVectorChanged(float x, float y, float z
            , float cosHalfOfTheta, float heading){}

    /**
     * 14. 未校准磁力传感器值改变事件.
     * 6.8mA高耗电
     * Ticwatch 冇讲有哩个传感器
     *
     * 参考onMagneticFieldChanged 的注释.
     *
     * 返回x、y、z三轴的环境磁场数据, maxRange:200,
     * 单位是微特斯拉（micro-Tesla），用uT表示
     */
    public void onMagneticFieldUncalibratedChanged(float x, float y, float z){}

    /**
     * 16. 未校准陀螺仪传感器值改变事件.
     * 6.1mA高耗电
     * Ticwatch 冇讲有哩个传感器
     *
     * 参考onGyroscopeChanged 的注释.
     *
     * 返回x、y、z三轴的角加速度数据, maxRange:40,
     * 角加速度的单位是radians/second。
     */
    public void onGyroscopeUncalibratedChanged(float x, float y, float z){}

    /**
     * 18. 步行检测传感器事件. 为咗视觉上嘅统一, 事件名加上Changed.
     * Ticwatch 一直冇触发
     *
     * 每走一步就触发一次事件.
     */
    @Deprecated
    public void onStepDetectorChanged() {}

    /**
     * 19. 计步器值改变事件.
     * 6.1mA高耗电?
     *
     * 需要走大概十几步，计步器确定当前喺走路状态, 先至会产生哩个事件(?)
     *
     * 想要得到当日嘅步数统计, 建议使用Ticwear SDK提供的接口,
     * 直接用哩个计步器需要自行处理业务逻辑.
     *
     * @param steps 返回从系统启动开始, 到事件发生嗰时录得嘅步数.
     */
    public void onStepCounterChanged(int steps) {}

    /**
     * 21. 心率传感器值改变事件.
     * 1.0mA低耗电
     * 刚开始测量时返回值可能为0，等到测量10秒左右就可以获得当前的心率值。
     *
     * 由于心率传感器取值时候比表盘锁屏时间长好多,
     * 建议整个点击开关, 当需要心率取值嗰时令心率传感器长期运行.
     *
     * @param rate 每分钟的心跳数(beats/minute)
     */
    public void onHeartRateChanged(float rate) {}


    @Override
    public String toString() {
        //从传感器管理器中获得全部的传感器列表
        List<Sensor> allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        //打印传感器信息
        String out_string = allSensors.size() + "Sensors:\n";
        for (Sensor s : allSensors) {
            out_string += String.format("%s%s maxRange:%.2f resolution:%.2f power:%.2f minDelay:%d maxDelay:%d\n",
                    s.getType(), s.getName(),
                    s.getMaximumRange(),
                    s.getResolution(),
                    s.getPower(),
                    s.getMinDelay(),
                    s.getMaxDelay()
            );

//            String tempString = "\n" + "  设备名称：" + s.getName() + "\n" + "  设备版本：" + s.getVersion() + "\n" + "  供应商："
//                    + s.getVendor() + "\n";

        }
        return out_string;
    }
}
