package com.magicinstall.wearable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.Set;
import java.util.UUID;

/**
 * Created by wing on 15/11/14.
 * 哩个类嘅主要目的系取得手机端嘅电量,
 * 因为未有确切方法知道喺所有已配对设备之中,
 * 边个先至系当前的伴侣手机,
 * 所以唔保证一定准确...
 * 有可能取得嘅电量系其它设备嘅值 (-_-b)
 *
 * 注意:
 * 1. 如果手表未配对手机, 可能要喺配对之后重启一下手表!(主要系要重启表盘的进程);
 * 2. 哩个类喺为表盘服务而设计, 一但分配实例, 就立即启动,
 *
 * 用法很简单, 只需要用匿名继承哩个类, 重写onXXXChanged 方法就可以, 代码非常简洁.
 */
public class BluetoothStatus {
    /**
     * GATT 电量服务UUID
     * https://developer.bluetooth.org/gatt/services/Pages/ServiceViewer.aspx?u=org.bluetooth.service.battery_service.xml
     */
    private static final UUID BATTERY_SERVICE_UUID
            = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    /**
     * GATT 电量属性UUID
     * https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.battery_level.xml
     */
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID
            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    /**
     * GATT 客户端属性配置描述符, 用嚟注册属性值变化通知
     * https://developer.bluetooth.org/gatt/descriptors/Pages/DescriptorViewer.aspx?u=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
     */
    private static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID
            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     *
     * @return 手机端的电量, 单位是百分比(10 = 10%, 100 = 100%, ...)
     */
    public int getMobileBattery() {return mMobileBattery;}
    private int mMobileBattery;

    /**
     *
     * @return 表示手机连接状态.
     */
    public boolean getIsConnected() {return mIsConnected & mGattIsSuccess;}
    private boolean mIsConnected;
    private boolean mGattIsSuccess;


//    private Context mContext;
//    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattDescriptor mBluetoothDescriptor;

    public BluetoothStatus(Context context) {
        mIsConnected = false;
        mGattIsSuccess = false;

        // 取得蓝牙适配器对象
        BluetoothAdapter bluetooth_adapter =  BluetoothAdapter.getDefaultAdapter();
        // 检测蓝牙适配器有冇启动
        if (bluetooth_adapter != null && bluetooth_adapter.isEnabled()) {
            // 取得已配对的设备
            Set<BluetoothDevice> paired_devices = bluetooth_adapter.getBondedDevices();
            for (BluetoothDevice device : paired_devices) {

//                Log.i("Bluetooth", device.getName() + " " + device.getType() + " UUID:" + device.getUuids());

                // 暂时未知有咩方法确定边只设备先至系当前配对嘅手机, 只取第一个成功建立GATT 嘅设备;
                // Ticwear 实际使用咗两个蓝牙端口(模式?)同iPhone 通信,
                // 平时推送系用Classic 模式, 但系哩个模式冇BLE 服务,
                // 电量信息实际上系通过另一个端口(模式?): Ticwear 用嚟实现模拟蓝牙耳机嗰个端口(模式?),
                // 间接通过哩个模拟嘅蓝牙耳机嘅BLE 协议得到电量信息.
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {

                    /* 暂时只有简单判断一下, 如果Ticwear 修改咗模拟蓝牙耳机个名, 就要再改改... */
                    if (device.getName().equals("iPhone")) {
                        // 连接GATT
                        BluetoothGatt gatt = device.connectGatt(context, true, mGattCallback);
                        if (gatt != null) {
                            Log.v("Bluetooth", "Connect " + device.getName() + " Gatt...");

                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        // mBluetoothGatt 只会喺onConnectionStateChange 事件中赋值
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接断开的状态改变
         * Callback indicating when GATT client has connected/disconnected to/from a remote
         * GATT server.
         *
         * @param gatt     GATT client
         * @param status   Status of the connect or disconnect operation.
         *                 {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of
         *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
         *                 {@link BluetoothProfile#STATE_CONNECTED}
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            // 首先判断这个status 如果等于BluetoothGatt.GATT_SUCCESS（value=0）代表这个回调是正常的，
            // 如果不等于 0，那边就代表没有成功，也不需要进行其他操作了，
            // 连接成功和断开连接都会走到这里
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                if (!mIsConnected && mGattIsSuccess) onConnectStatusChanged(true);
                mIsConnected = true;
//                Log.i("Bluetooth", "Connect GATT successful");

                // mBluetoothGatt 只喺哩度赋值, 用于垃圾回收嗰时中止GATT 连接
                mBluetoothGatt = gatt;

                // 连接成功之后，我们应该立刻去寻找服务，只有寻找到服务之后，才可以和设备进行通信,
                // 查找服务嘅过程系异步嘅, 需要D 时间, 当揾到服务之后会调用onServicesDiscovered.
                boolean result = gatt.discoverServices();
//                Log.i("Bluetooth", "discoverServices:" + result);
            }
            // 断开
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                if (mIsConnected) onConnectStatusChanged(false);
                Log.w("Bluetooth", "GATT has disconnected");
                mIsConnected = false;
                onConnectStatusChanged(false);
            }
        }

        /**
         * 发现设备服务
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device has been explored successfully.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS ) {
                // 寻找到服务
                // 寻找服务之后，我们就可以和设备进行通信，比如下发配置值，获取设备电量什么的

//                if (!mGattIsSuccess && mIsConnected) onConnectStatusChanged(true);
                mGattIsSuccess = true;
                onConnectStatusChanged(true);

//                Log.i("Bluetooth", "Reading battery...");
                readBattery(gatt); // 读取电量操作

            }else {
//                if (mGattIsSuccess) onConnectStatusChanged(false);
                mGattIsSuccess = false;
                Log.w("Bluetooth", "Gatt status: " + status);
            }
        }

        /**
         * 收到设备通信数据
         * Callback reporting the result of a characteristic read operation.
         *
         * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * @param characteristic Characteristic that was read from the associated
         *                       remote device.
         * @param status         {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            // 读取到值,根据UUID来判断读到的是什么值
            if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                // 获取到电量
                mMobileBattery = characteristic.getValue()[0];
                // 调用事件
                onBatteryLevelChanged(mMobileBattery);

//                Log.i("Bluetooth", "Mobile battery:" + mMobileBattery + "%");

                // 将已经设置成启用属性改变通知的描述符写入GATT 设备, 就可以启动通知
                boolean result = gatt.writeDescriptor(mBluetoothDescriptor);
//                Log.d("Bluetooth", "writeDescriptor:" + result);
            }
        }

        /**
         * 已注册属性改变事件通知
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt           GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            Log.v("Bluetooth", "onCharacteristicChanged");

            // 读取到值,根据UUID来判断读到的是什么值
            if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                // 获取到电量
                mMobileBattery = characteristic.getValue()[0];
                // 调用事件
                onBatteryLevelChanged(mMobileBattery);

//                Log.i("Bluetooth", "Mobile battery:" + mMobileBattery + "%");
            }
        }


    };

    /**
     *  读取电量
     */
    private void readBattery(BluetoothGatt gatt) {
        // 如上面所说，想要和一个学生通信，先知道他的班级（ServiceUUID）和学号（CharacUUID）
        BluetoothGattService batteryService = gatt.getService(BATTERY_SERVICE_UUID);
        if (batteryService == null) {
            Log.e("Bluetooth", "Unable to get battery service!");
            return;
        }

        BluetoothGattCharacteristic batteryCharacteristic
                = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID);
        if (batteryCharacteristic != null) {
            // 先主动读一次电量.
            // 异步读取电量,这是读取batteryCharacteristic值的方法，读取其他的值也是如此，只是它们的ServiceUUID和CharacUUID不一样
            gatt.readCharacteristic(batteryCharacteristic);

            // 注册电量改变通知
            if (!gatt.setCharacteristicNotification(batteryCharacteristic, true)) {
                Log.w("Bluetooth", "Unable to set battery notification!");
                return;
            }
            mBluetoothDescriptor = batteryCharacteristic
                    .getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            if (mBluetoothDescriptor == null) {
                Log.w("Bluetooth", "Unable to get battery descriptor!");
                return;
            }
            mBluetoothDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            /* 喺哩度writeDescriptor 只会直接返回false !
             * 因为上边readCharacteristic 咗一次, GATT 读写系异步操作, 必需等上一次操作完成,
             * 再去writeDescriptor 先至唔会因为Device Busy 而返回false !
             * 所以将writeDescriptor 移至onCharacteristicRead 回调方法内.
            Log.d("Bluetooth", "writeDescriptor:" + gatt.writeDescriptor(mBluetoothDescriptor));
             */
        }

    }

    /**
     * 手机端电量变化事件
     * @param batteryLevel 手机端的电量, 单位是百分比(10 = 10%, 100 = 100%, ...)
     */
    public void onBatteryLevelChanged(int batteryLevel) {}

    /**
     * 手机连接状态变化事件
     * @param isConnnected true = 已连接
     */
    public void onConnectStatusChanged(boolean isConnnected) {}
}
