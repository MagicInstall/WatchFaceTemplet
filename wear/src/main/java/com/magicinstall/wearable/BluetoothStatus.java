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
 * 注意: 如果手表未配对手机, 可能要喺配对之后重启一下手表!(主要系要重启表盘的进程)
 */
public class BluetoothStatus /*implements BluetoothGattCallback*/{
    private BluetoothDevice mMobile;
    private BluetoothGatt mBluetoothGatt;

    /**
     *
     * @return 手机端的电量, 单位是百分比(10 = 10%, 100 = 100%, ...)
     */
    public int getMobileBattery() {return mMobileBattery;}
    private int mMobileBattery;


    BluetoothStatus(Context context) {
        // 取得蓝牙适配器对象
        BluetoothAdapter bluetooth_adapter =  BluetoothAdapter.getDefaultAdapter();
        // 检测蓝牙适配器有冇启动
        if (bluetooth_adapter != null && bluetooth_adapter.isEnabled()) {
            // 取得已配对的设备
            Set<BluetoothDevice> paired_devices = bluetooth_adapter.getBondedDevices();
            for (BluetoothDevice device : paired_devices) {

                Log.i("Bluetooth", device.getName() + " " + device.getType() + " UUID:" + device.getUuids());

                // 暂时未知有咩方法确定边只设备先至系当前配对嘅手机, 只取第一个成功建立GATT 嘅设备;
                // Ticwear 实际使用咗两个蓝牙端口(模式?)同iPhone 通信,
                // 平时推送系用Classic 模式, 但系哩个模式冇BLE 服务,
                // 电量信息实际上系通过另一个端口(模式?): Ticwear 用嚟实现模拟蓝牙耳机嗰个端口(模式?),
                // 间接通过哩个模拟嘅蓝牙耳机嘅BLE 协议得到电量信息.
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    mMobile = device;
                    mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
                    if (mBluetoothGatt != null) {
                        Log.i("Bluetooth", "Connect " + device.getName() + " Gatt...");
                        break;
                    }
                }
            }
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
                Log.i("Bluetooth", "Connected GATT");
                // 连接成功之后，我们应该立刻去寻找服务，只有寻找到服务之后，才可以和设备进行通信,
                // 查找服务嘅过程系异步嘅, 需要D 时间, 当揾到服务之后会调用onServicesDiscovered.
                mBluetoothGatt.discoverServices();
            }
            // 断开
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w("Bluetooth", "Disconnected GATT");
            }
        }

        /**
         * 发现设备服务
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 寻找到服务
                // 寻找服务之后，我们就可以和设备进行通信，比如下发配置值，获取设备电量什么的

                readBattery(); // 读取电量操作

            }else {
                Log.i("Bluetooth", "Gatt status: " + status);
            }
        }

        /**
         *  读取电量
         */
        private void readBattery() {
            // 如上面所说，想要和一个学生通信，先知道他的班级（ServiceUUID）和学号（CharacUUID）
            BluetoothGattService batteryService = mBluetoothGatt.getService(UUID
                    .fromString("0000180f-0000-1000-8000-00805f9b34fb")); // 此处的0000180f...是举例，实际开发需要询问硬件那边
            if (batteryService != null) {
                BluetoothGattCharacteristic batteryCharacteristic = batteryService
                        .getCharacteristic(UUID
                                .fromString("00002a19-0000-1000-8000-00805f9b34fb"));// 此处的00002a19...是举例，实际开发需要询问硬件那边
                if (batteryCharacteristic != null) {
                    // 读取电量,这是读取batteryCharacteristic值的方法，读取其他的值也是如此，只是它们的ServiceUUID和CharacUUID不一样
                    mBluetoothGatt.readCharacteristic(batteryCharacteristic);
                }
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
            if (characteristic.getUuid().toString()
                    .equals("00002a19-0000-1000-8000-00805f9b34fb")) {
                // 获取到电量
                mMobileBattery = characteristic.getValue()[0];
                Log.i("Bluetooth", "Mobile battery:" + mMobileBattery + "%");
            }
        }

        /**
         * Callback indicating the result of a characteristic write operation.
         * <p/>
         * <p>If this callback is invoked while a reliable write transaction is
         * in progress, the value of the characteristic represents the value
         * reported by the remote device. An application should compare this
         * value to the desired value to be written. If the values don't match,
         * the application must abort the reliable write transaction.
         *
         * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
         * @param characteristic Characteristic that was written to the associated
         *                       remote device.
         * @param status         The result of the write operation
         *                       {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt           GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        /**
         * Callback reporting the result of a descriptor read operation.
         *
         * @param gatt       GATT client invoked {@link BluetoothGatt#readDescriptor}
         * @param descriptor Descriptor that was read from the associated
         *                   remote device.
         * @param status     {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        /**
         * Callback indicating the result of a descriptor write operation.
         *
         * @param gatt       GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was writte to the associated
         *                   remote device.
         * @param status     The result of the write operation
         *                   {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        /**
         * Callback invoked when a reliable write transaction has been completed.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#executeReliableWrite}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable write
         */
        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        /**
         * Callback reporting the RSSI for a remote device connection.
         * <p/>
         * This callback is triggered in response to the
         * {@link BluetoothGatt#readRemoteRssi} function.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#readRemoteRssi}
         * @param rssi   The RSSI value for the remote device
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read successfully
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        /**
         * Callback indicating the MTU for a given device connection has changed.
         * <p/>
         * This callback is triggered in response to the
         * {@link BluetoothGatt#requestMtu} function, or in response to a connection
         * event.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#requestMtu}
         * @param mtu    The new MTU size
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the MTU has been changed successfully
         */
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };
}
