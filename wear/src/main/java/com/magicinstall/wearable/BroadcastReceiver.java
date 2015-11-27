package com.magicinstall.wearable;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.Set;

/**
 * Created by wing on 15/11/17.
 * 哩个类用嚟接收系统自带嘅一D 广播事件.
 * 挑咗几个手表可能用得上嘅广播事件, 例如手表电量变化等,
 * 具体有边D广播可以睇本类内置嘅静态常量(ACTION_XXXX)嘅注释.
 *
 * 用法十分简单, 只需要匿名继承哩个类, 重写onXXXChanged 方法就可以, 代码非常简洁.
 */
public class BroadcastReceiver extends android.content.BroadcastReceiver {
    private static final String TAG = "Wing.Broadcast";
    Context mContext;

    /**
     * wing 定义嘅手机电量变化广播
     */
    public static final String ACTION_MOBILE_BATTERY_CHANGED = "magicinstall.intent.action.ACTION_MOBILE_BATTERY_CHANGED";
    /**
     * wing 定义嘅手机连接状态变化广播
     */
    public static final String ACTION_MOBILE_CONNECTION_CHANGED = "magicinstall.intent.action.ACTION_MOBILE_CONNECTION_CHANGED";
    /**
     * 充电状态，或者电池的电量发生变化
     */
    public static final String ACTION_BATTERY_CHANGED = Intent.ACTION_BATTERY_CHANGED;
    /**
     *  低电量
     */
    public static final String ACTION_BATTERY_LOW = Intent.ACTION_BATTERY_LOW;
    /**
     * 从低电量回血至正常电量
     */
    public static final String ACTION_BATTERY_OKAY = Intent.ACTION_BATTERY_OKAY;
    /**
     * 插入充电器
     */
    public static final String ACTION_POWER_CONNECTED = Intent.ACTION_POWER_CONNECTED;
    /**
     * 掹出充电器
     */
    public static final String ACTION_POWER_DISCONNECTED = Intent.ACTION_POWER_DISCONNECTED;

    /**
     * 飞鸡模式
     */
    public static final String ACTION_AIRPLANE_MODE_CHANGED = Intent.ACTION_AIRPLANE_MODE_CHANGED;

    /**
     * 位置
     */
    public static final String ACTION_LOCALE_CHANGED = Intent.ACTION_LOCALE_CHANGED;
    /**
     * 时区
     * </br>
     * 用WatchFace 嘅onTimeZoneChanged 事件代替.
     */
    @Deprecated
    public static final String ACTION_TIMEZONE_CHANGED = Intent.ACTION_TIMEZONE_CHANGED;
    /**
     * 时间设置
     * </br>
     * 用WatchFace onTimeChanged 事件代替.
     */
    @Deprecated
    public static final String ACTION_TIME_CHANGED = Intent.ACTION_TIME_CHANGED;
    /**
     * 日期发生改变
     * </br>
     * 用WatchFace onTimeChanged 事件代替.
     */
    @Deprecated
    public static final String ACTION_DATE_CHANGED = Intent.ACTION_DATE_CHANGED;
    /**
     * 每分钟的时间变化
     * </br>
     * 用WatchFace onTimeChanged 事件代替.
     */
    @Deprecated
    public static final String ACTION_TIME_TICK = Intent.ACTION_TIME_TICK;


    /**
     * Service 的连接器
     * 哩个连接器用嚟调用MobileInfoService 得到手机电量变化通知
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "onServiceConnected");
        }
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected");
        }
    };

    /**
     * @param context 传入Engine 的BaseContext.
     */
    public BroadcastReceiver(Context context) {
        super();
        mContext = context;
   }

    @Override
    protected void finalize() throws Throwable {
        Log.d("Broadcast", "finalize - unregisterReceiver(this)");
        DeactivateReceiver();
        super.finalize();
    }


    /**
     * 停止接收所有广播.
     */
    public void DeactivateReceiver() {
        mContext.unregisterReceiver(this);
        mContext.unbindService(mServiceConnection);
    }

    /**
     * 接收指定广播.
     * @param actions 最好系写成BroadcastReceiver.ACTION_XXXX 的数组, 有助提高代码可读性.
     */
    public void ActivateReceiverWithType(String[] actions) {
        IntentFilter filter = new IntentFilter();
        boolean need_bind = false;

        for (String a : actions) {
//            mContext.registerReceiver(this, new IntentFilter(a));
            filter.addAction(a);

            // 睇下使唔使连接后台服务
            if (a.equals(ACTION_MOBILE_BATTERY_CHANGED) || a.equals(ACTION_MOBILE_CONNECTION_CHANGED))
                need_bind = true;

        }
        // 连接手机电量后台服务
        if (need_bind)
            mContext.bindService(new Intent(mContext, MobileInfoService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

        // TODO 唔知点解唔可以读返回值, 一读就收唔到广播
        mContext.registerReceiver(this, filter);
    }

    /**
     * 收到广播事件
     * 一般唔使重写哩个事件
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Java 唔可以用String 作为case 值, 唯有硬编码
        switch (intent.getAction()) {
            case "magicinstall.intent.action.ACTION_MOBILE_BATTERY_CHANGED":
                Log.v("Broadcast", "Mobile battery level:" + intent.getExtras().getInt("level") + "%");
                onMobileBatteryChanged(intent.getExtras().getInt("level"));
                break;

            case "magicinstall.intent.action.ACTION_MOBILE_CONNECTION_CHANGED":
                Log.v("Broadcast",
                        "Mobile isConnnected:" +
                                intent.getExtras().getBoolean("isConnnected")
                );
                onMobileConnectionChanged(intent.getExtras().getBoolean("isConnnected"));
                break;

            case "android.intent.action.BATTERY_CHANGED":
                Log.v("Broadcast", "Battery level:" + intent.getExtras().getInt("level") + "%");
                onBatteryChanged(
                        intent.getIntExtra("level", 0), // 实际上要求出与"scale"的比值, 但实测Ticwear 的scale 是100, 就费事整喇...
                        intent.getIntExtra("status", 1),
                        intent.getIntExtra("plugged", 0) > 0 ? true : false
                );
                break;

            case "android.intent.action.BATTERY_LOW":
                Log.v("Broadcast", "Battery low");
                onBatteryLow();
                break;

            case "android.intent.action.BATTERY_OKAY" :
                Log.v("Broadcast", "Battery Okay");
                onBatteryOkay();
                break;

            case "android.intent.action.ACTION_POWER_CONNECTED":
                Log.v("Broadcast", "Power connected");
                onPowerConnected();
                break;

            case "android.intent.action.ACTION_POWER_DISCONNECTED":
                Log.v("Broadcast", "Power disconnected");
                onPowerDisconnected();
                break;

            case "android.intent.action.AIRPLANE_MODE":
                Log.v("Broadcast", "Airplane mode:" + intent.getExtras().getBoolean("state"));
                onAirplaneMode(intent.getBooleanExtra("state", true));
                break;

            case "android.intent.action.LOCALE_CHANGED":
                onLocationChanged();
                break;

//            case "android.intent.action.TIMEZONE_CHANGED":
//                onTimezoneChanged();
//                break;

//            case "android.intent.action.TIME_SET":
//                onTimeSet();
//                break;

//            case "android.intent.action.DATE_CHANGED":
//                onDateChanged();
//                break;

            default:
                String debug_print = intent.getAction() + " ";
                try {
                    Set<String> keys = intent.getExtras().keySet();
                    for (String k : keys) {
                        debug_print += k + "=" + intent.getExtras().get(k) + " ";
                    }
                } catch (Exception e) {

                }
                Log.d("Broadcast", debug_print);
                break;
        }

    }

    /**
     * 收到手机电量变化广播事件
     *
     * @param level 电量, 单位是百分比(10 = 10%, 100 = 100%, ...);
     */
    public void onMobileBatteryChanged(int level) {}

    /**
     * 收到手机连接状态变化广播事件
     *
     * @param isConnnected true = 已连接.
     */
    public void onMobileConnectionChanged(boolean isConnnected) {}

    /**
     * 收到电量变化广播事件
     * </br>
     * 由于电压变化同样会触发哩个事件, 所以多次事件之间的电量值可能会一样.
     *
     * @param level 电量, 单位是百分比(10 = 10%, 100 = 100%, ...);
     * @param status 充电状态:
     *               BatteryManager.BATTERY_STATUS_UNKNOWN = 1;
     *               BatteryManager.BATTERY_STATUS_CHARGING = 2;
     *               BatteryManager.BATTERY_STATUS_DISCHARGING = 3;
     *               BatteryManager.BATTERY_STATUS_NOT_CHARGING = 4;
     *               BatteryManager.BATTERY_STATUS_FULL = 5;
     * @param plugged true = 连接上充电器
     */
    public void onBatteryChanged(int level, int status, boolean plugged) {}

    /**
     * 收到低电量广播事件
     */
    public void onBatteryLow() {}

    /**
     * 收到从低电量回血至正常电量广播事件
     */
    public void onBatteryOkay() {}

    /**
     * 收到插入充电器广播事件
     */
    public void onPowerConnected() {}

    /**
     * 收到掹出充电器广播事件
     */
    public void onPowerDisconnected() {}

    /**
     * 收到飞鸡模式切换广播事件
     * @param state true = 已切换到飞鸡模式
     */
    public void onAirplaneMode(boolean state) {
        Log.d("Broadcast", "Airplane mode:" + state);
    }

    /**
     * TODO 收到位置变化广播事件
     * 未完成
     */
    public void onLocationChanged(){}

//    /**
//     * 收到时区变化广播事件
//     */
//    public void onTimezoneChanged(){}
//
//    /**
//     * 收到时间设置广播事件
//     */
//    public void onTimeSet(){}
//
//    /**
//     * 收到日期变化广播事件
//     */
//    public void onDateChanged(){}
}
