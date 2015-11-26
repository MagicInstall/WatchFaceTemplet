package com.magicinstall.wearable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * Created by wing on 15/11/19.
 */
public class StepsObserver extends android.database.ContentObserver{
    /**
     * @param context 传入Engine 的BaseContext.
     */
    public StepsObserver(Context context/*, Handler handler*/){
        super(new Handler());
        mContext = context;
        mResolver = context.getContentResolver();
    }

    @Override
    protected void finalize() throws Throwable {
        DeactivateObserver();
        Log.d("StepsObserver", "finalize - unregisterContentObserver(this)");
        super.finalize();
    }

    // mobvoi 共享嘅当天累计步数和行走距离的数据
    private static final Uri CONTENT_STEP_URI = Uri.parse("content://com.mobvoi.ticwear.steps");

    /**
     * Engine 的BaseContext
     */
    protected Context mContext;

    /**
     * 内容解析者
     */
    private android.content.ContentResolver mResolver;

    /**
     * 步数
     */
    protected int mSteps = 0;
    /**
     * 步行距离, 单位是米
     */
    protected int mDistance = 0;

    /**
     * 解释步行数据
     */
    private void querySteps() {
        //query 接口只接收URI，不接收其参数，置为null即可。
        Cursor cursor = mResolver.query(CONTENT_STEP_URI, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    int steps = cursor.getInt(0);
                    int distance = cursor.getInt(1); // 距离的单位是米
                    Log.v("StepsObserver", "Steps:" + mSteps + " Distance:" + mDistance);

                    // 触发事件
                    if (steps != mSteps || distance != mDistance) {
                        onStepChanged(mSteps, mDistance);
                        mSteps = steps;
                        mDistance = distance;
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    // 收到数据变化通知, 哩个事件本身系唔会返回数据嘅, 要自行查询
    @Override
    public final void onChange(boolean selfChange) {
        super.onChange(selfChange);
        querySteps();
    }

    /**
     * 激活数据变化监视器.
     */
    public void ActivateObserver() {
        mResolver.registerContentObserver(CONTENT_STEP_URI, true, this);

        Log.d("StepsObserver", "Step start");
        querySteps(); // 主动查询一次
    }

    /**
     * 注销数据变化监视器.
     */
    public void DeactivateObserver() {
        mResolver.unregisterContentObserver(this);
    }

    /**
     * 步行数据变化事件
     * @param steps 当日的步数.
     * @param distance 当日嘅步行距离, 单位是米.
     */
    public void onStepChanged(int steps, int distance) {}
}
