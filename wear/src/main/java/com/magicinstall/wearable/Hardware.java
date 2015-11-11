package com.magicinstall.wearable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.List;

/**
 * 为咗增强代码可读性, 将传感器事件单独拆开, 用接口实现.
 */
interface SensorsEventCallback {
    /**
     * 加速度传感器值改变事件.
     *
     * 将手机平放在桌面上，x轴默认为0，y轴默认0，z轴默认9.81。
     * 将手机朝下放在桌面上，z轴为-9.81。
     * 将手机向左倾斜，x轴为正值。
     * 将手机向右倾斜，x轴为负值。
     * 将手机向上倾斜，y轴为负值。
     * 将手机向下倾斜，y轴为正值。
     */
    public void onAccelerationChanged(float x, float y, float z);
}

/**
 * Created by wing on 15/11/10.
 */
public class Hardware implements SensorEventListener {
    /**
     * 传感器管理对象.
     */
    protected SensorManager mSensorManager;

    /**
     * 传感器值改变事件的接收者.
     */
    protected SensorsEventCallback mSensorReceiver;

    protected Sensor mAccelerometerSensor;

    public Hardware(Context context) {
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        System.out.println("finalize - unregisterListener(this)");
        mSensorManager.unregisterListener(this);
    }

    /**
     * 设置传感器值改变事件的接收者.
     * @param receiver
     */
    public void setSensorEventReceiver(SensorsEventCallback receiver) {
        mSensorReceiver = receiver;
    }

    /**
     * 停止监听所有传感器.
     */
    public void DeactivateSensors() {
        mSensorManager.unregisterListener(this);
    }

    /**
     * 激活指定的传感器
     * @param types 必需系Sensor.TYPE_XXXX 的数组, 有助提高代码可读性.
     */
    public void ActivateSensorsWithType(int[] types) {
        boolean result;
        Sensor sensor;
        for (int t : types) {
            sensor = null;
            sensor = mSensorManager.getDefaultSensor(t);
            if (sensor == null) {
                System.out.println("Sensors(Type:" + t + ") does not exist!");
                continue;
            }
            result = mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (!result) {
                System.out.println("Sensors(Type:" + t + ") Unable to register!");
            }
        }
    }

    /**
     * 激活加速度传感器
     */
    @Deprecated
    public boolean ActivateAccelerometerSensor() {
        if (mAccelerometerSensor == null) {
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 注销加速度传感器
     */
    @Deprecated
    public void DeactivateAccelerometerSensor() {
        mSensorManager.unregisterListener(this, mAccelerometerSensor);
    }


    /**
     * 传感器精度改变事件
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        System.out.println(
                "onAccuracyChanged - sensor:" +
                sensor.getType() +
                sensor.getName() +
                " Accuracy:" + i
        );
        // 冇人接收事件就收工
        if (mSensorReceiver == null) return;
    }


    /**
     * 传感器值改变事件
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        System.out.print(
                "onSensorChanged - "
                + sensorEvent.sensor.getType()
                + sensorEvent.sensor.getName()
        );
        // 冇人接收事件就收工
        if (mSensorReceiver == null) {
            System.out.println("No receivers");
            return;
        }


        switch (sensorEvent.sensor.getType()) {
            // 1. 加速度
            case Sensor.TYPE_ACCELEROMETER:
                System.out.println(
                        String.format(
                                "Accelerometer %f %f %f",
                                sensorEvent.values[0],
                                sensorEvent.values[1],
                                sensorEvent.values[2]
                        )
                );
                mSensorReceiver.onAccelerationChanged(
                        sensorEvent.values[0],
                        sensorEvent.values[1],
                        sensorEvent.values[2]
                );
                break;

            // 2. 磁力场
            case Sensor.TYPE_MAGNETIC_FIELD:
                break;

            // 4. 陀螺仪
            case Sensor.TYPE_GYROSCOPE:
                break;

            // 5. 光照(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_LIGHT:
                break;

            // 6. 压力(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_PRESSURE:
                break;

            // 8. 接近(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_PROXIMITY:
                break;

            // 9. 重力
            case Sensor.TYPE_GRAVITY:
                break;

            // 10. 线性加速度
            case Sensor.TYPE_LINEAR_ACCELERATION:
                break;

            // 11. 旋转量(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_ROTATION_VECTOR:
                break;

            // 14. 未校准磁力场(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                break;

            // 16. 未校准陀螺仪(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                break;

            // 18. 步行检测，每走一步就触发一次事件(Ticwatch 冇讲有哩个传感器)
            case Sensor.TYPE_STEP_DETECTOR:
                break;

            // 19. 计步
            case Sensor.TYPE_STEP_COUNTER:
                break;

            // 21. 心率
            case Sensor.TYPE_HEART_RATE:
                break;

            // Android API 弃用
            case Sensor.TYPE_ORIENTATION: // 3. 方向(Ticwatch 冇讲有哩D传感器)
            default:
                System.out.println(sensorEvent.sensor.getType() + sensorEvent.sensor.getName() + "(Unknow)");
                break;
        }
    }

    @Override
    public String toString() {
        //从传感器管理器中获得全部的传感器列表
        List<Sensor> allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        //打印传感器信息
        String out_string = allSensors.size() + "Sensors:\n";
        for (Sensor s : allSensors) {
            out_string += s.getType() + s.getName() + "\n";

//            String tempString = "\n" + "  设备名称：" + s.getName() + "\n" + "  设备版本：" + s.getVersion() + "\n" + "  供应商："
//                    + s.getVendor() + "\n";
//
//            switch (s.getType()) {
//                case Sensor.TYPE_ACCELEROMETER:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 加速度传感器accelerometer" + tempString);
//                    break;
//                case Sensor.TYPE_GYROSCOPE:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 陀螺仪传感器gyroscope" + tempString);
//                    break;
//                case Sensor.TYPE_LIGHT:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 环境光线传感器light" + tempString);
//                    break;
//                case Sensor.TYPE_MAGNETIC_FIELD:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 电磁场传感器magnetic field" + tempString);
//                    break;
//                case Sensor.TYPE_ORIENTATION:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 方向传感器orientation" + tempString);
//                    break;
//                case Sensor.TYPE_PRESSURE:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 压力传感器pressure" + tempString);
//                    break;
//                case Sensor.TYPE_PROXIMITY:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 距离传感器proximity" + tempString);
//                    break;
//                case Sensor.TYPE_TEMPERATURE:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 温度传感器temperature" + tempString);
//                    break;
//                default:
//                    tx1.setText(tx1.getText().toString() + s.getType() + " 未知传感器" + tempString);
//                    break;
//            }
        }
        return out_string;
    }

}
