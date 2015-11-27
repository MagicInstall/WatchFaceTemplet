package com.magicinstall.wearable;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 哩个后台服务主要为表盘提供手机电量等信息嘅广播.
 * </br>
 * AndroidManifest.xml 要加上
 *     <uses-permission android:name="android.permission.BLUETOOTH"/> <!-- 蓝牙权限 -->
 *     <service
 *          android:name="lib.magicinstall.wearable.MobileInfoService"
 *          android:enabled="true"
 *          android:exported="true">
 *     </service>
 */
public class MobileInfoService extends Service {
    private static final String TAG = "InfoService";

    private BluetoothStatus mBluetoothStatus;
    private BroadcastReceiver mBroadcastReceiver;

    public MobileInfoService() {
    }

    /**
     * 喺哩度连接BLE
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        activateBroadcast(getBaseContext());
        activateBluetooth(getBaseContext());
    }

    /**
     * 喺哩度释放BLE 连接
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mBluetoothStatus.DisconnectGATT();
        mBroadcastReceiver.DeactivateReceiver();
    }

    /**
     * 规定要返回一个Binder 对象,
     * 唔使郁佢.
     */
    @Override
    public IBinder onBind(Intent intent) {
//        Log.d(TAG, "onBind");
//        return mBinder;
        return new Binder() {
            public MobileInfoService getService() {
                return MobileInfoService.this;
            }
        };
    }

    /**
     * onBind 要用嘅Binder 对象,
     * 唔使郁佢.
     */
//    private final Binder mBinder = new Binder(){
//        public MobileInfoService getService(){
//            return MobileInfoService.this;
//        }
//    };

    /**
     * 未见过触发哩个事件
     */
    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
    }

    /**
     * 未见过触发哩个事件
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
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
//                mPhoneBattery = "bat." + batteryLevel + "%\n";

                Intent intent = new Intent();
                intent.setAction(BroadcastReceiver.ACTION_MOBILE_BATTERY_CHANGED);
                intent.putExtra("level", batteryLevel);
                sendBroadcast(intent);
                Log.v(TAG, "sendBroadcast:onBatteryLevelChanged");
            }

            /**
             * 手机连接状态变化事件
             *
             * @param isConnnected true = 已连接
             */
            @Override
            public void onConnectStatusChanged(boolean isConnnected) {
                super.onConnectStatusChanged(isConnnected);

                Intent intent = new Intent();
                intent.setAction(BroadcastReceiver.ACTION_MOBILE_CONNECTION_CHANGED);
                intent.putExtra("isConnnected", isConnnected);
                sendBroadcast(intent);
                Log.v(TAG, "sendBroadcast:onConnectStatusChanged");

//                mPhoneConnect = "Phone: "+ (isConnnected ? "Connected " : "Disconnected ");
//                if (!isConnnected) mPhoneBattery = "bat.---\n";
            }
        };
        mBluetoothStatus.ConnectGATT();
    }


    /**
     * 启动广播接收
     * 主要用嚟处理飞鸡模式
     * @param context
     */
    private void activateBroadcast(Context context) {
        mBroadcastReceiver = new BroadcastReceiver(context) {
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

                    Intent intent = new Intent();
                    intent.setAction(BroadcastReceiver.ACTION_MOBILE_CONNECTION_CHANGED);
                    intent.putExtra("isConnnected", false);
                    sendBroadcast(intent);
                    Log.d(TAG, "sendBroadcast");

//                    mPhoneConnect = "Phone: "+ (mBluetoothStatus.getIsConnected() ? "Connected " : "Disconnected ");
//                    if (!mBluetoothStatus.getIsConnected()) mPhoneBattery = "bat.---\n";
                } else {
                    // 重连手机
                    mBluetoothStatus.ConnectGATT();
                }
            }
        };
        mBroadcastReceiver.ActivateReceiverWithType(new String[]{
                        BroadcastReceiver.ACTION_AIRPLANE_MODE_CHANGED
                }
        );
    }


}
