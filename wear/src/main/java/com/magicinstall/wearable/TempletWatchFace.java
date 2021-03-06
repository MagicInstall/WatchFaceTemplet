/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.magicinstall.wearable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * TODO 谂下点想整一个子类包装CanvasWatchFaceService 服务
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class TempletWatchFace extends CanvasWatchFaceService {
    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
    /**
     * 状态图标在最顶部边缘.
     * Ticwear 实测单图标Xl=142, Yt=0, Xr=177, Yb=29;双图标Xl=131, Yt=0, Xr=187, Yb=29
     */
    public static final int STATUSBAR_GRAVITY_TOP_EDGE = 32; // 32-47  64-79 96-127 160-175 192-207 224-255 288
    /**
     * 状态图标在顶部.
     * Ticwear 实测单图标Xl=142, Yt=11, Xr=177, Yb=40;双图标Xl=131, Yt=11, Xr=187, Yb=40
     */
    public static final int STATUSBAR_GRAVITY_TOP = 48; // 48  50-63 176-191
    /**
     * 状态图标在比顶部稍低.
     * Ticwear 实测单图标Xl=142, Yt=42, Xr=177, Yb=71;双图标Xl=131, Yt=42, Xr=187, Yb=71
     */
    public static final int STATUSBAR_GRAVITY_TOP_BEFORE = 49; // 49
    /**
     * 状态图标使用Ticwear默认高度(中间偏上).
     * Ticwear 实测单图标Xl=142, Yt=90, Xr=177, Yb=119;双图标Xl=131, Yt=90, Xr=187, Yb=119
     */
    public static final int STATUSBAR_GRAVITY_DEFAULT90 = 0; // 0-2 4 6-17 19-31 128-159 256-287
    /**
     * 状态图标在接近中间.
     * Ticwear 实测单图标Xl=142, Yt=112, Xr=177, Yb=141;双图标Xl=131, Yt=112, Xr=187, Yb=141
     */
    public static final int STATUSBAR_GRAVITY_CENTER_AFTER = 18; // 18
    /**
     * 状态图标在圆心</br>
     * Ticwear 实测单图标Xl=142, Yt=138, Xr=177, Yb=167;双图标Xl=131, Yt=138, Xr=187, Yb=167
     */
    public static final int STATUSBAR_GRAVITY_CENTER = 80; // 80 82-95 208-223
    /**
     * 状态图标在左边</br>
     * Ticwear 实测单图标Xl=30, Yt=138, Xr=64, Yb=167;双图标Xl=30, Yt=138, Xr=86, Yb=167
     */
    public static final int STATUSBAR_GRAVITY_LEFT = 3;
    /**
     * 状态图标在右边</br>
     * Ticwear 实测单图标 Xl=255, Yt=138, Xr=289, Yb=167;双图标Xl=233, Yt=138, Xr=289, Yb=167
     */
    public static final int STATUSBAR_GRAVITY_RIGHT = 5;
    /**
     * 状态图标在下方</br>
     * Ticwear 实测单图标Xl=142, Yt=81, Xr=177, Yb=110;双图标Xl=131, Yt=81, Xr=187, Yb=110
     */
    public static final int STATUSBAR_GRAVITY_BOTTOM = 81; // 81



    private WatchFaceDrawer mFaceDrawer;
    /*---------------------------------------------*/

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine
            implements WatchFaceCallback /* Wing */
    {
        Paint mBackgroundPaint;
        Paint mHandPaint;
        boolean mAmbient;
        Time mTime;

        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
                /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
                // 响应时区切换 FIXME 哩个广播注册同我个BroadcastReceiver 类冲突, 有时间就解决下
                mFaceDrawer.setTimeZone(intent.getStringExtra("time-zone"));
                /*---------------------------------------------*/
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        /**
         * TODO 挠挠的关键代码
         * Called as the user performs touch-screen interaction with the
         * window that is currently showing this wallpaper.  Note that the
         * events you receive here are driven by the actual application the
         * user is interacting with, so if it is slow you will get fewer
         * move events.
         *
         * @param event
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            Log.d("Engine" , String.format(
                    "onTouchEvent DeviceId:%d Action:%d x:%f,y:%f Pressure:%f Edge:%d Meta:%d ",
                    event.getDeviceId(),
                    event.getAction(),
                    event.getX(),
                    event.getY(),
                    event.getPressure(),
                    event.getEdgeFlags(),
                    event.getMetaState()
            ));
        }

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(TempletWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
                    // 唔使用Engine 嘅obTapCommand 事件嘅话唔需要打开哩个设置
                    // Serveic 的onTouchEvent 事件系一直会触发嘅
//                    .setAcceptsTapEvents(true)

                    // TODO 指示器
//                    .setStatusBarGravity(5)
                    /*---------------------------------------------*/
                    .build());

            Resources resources = TempletWatchFace.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.analog_background));

            mHandPaint = new Paint();
            mHandPaint.setColor(resources.getColor(R.color.analog_hands));
            mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mTime = new Time();

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            mFaceDrawer = new InformationsDrawer(this, resources, getBaseContext());
            /*---------------------------------------------*/

        }

//        private int testGravity = 207; // 起步数
//        @Override
//        public void onTapCommand(int tapType, int x, int y, long eventTime) {
//            super.onTapCommand(tapType, x, y, eventTime);
////            Log.d("EngineTest", "Tap Type:" + tapType);
//            if (tapType < 2) return;
//
//            setWatchFaceStyle(new WatchFaceStyle.Builder(TempletWatchFace.this)
//                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
//                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
//                    .setShowSystemUiTime(false)
//                    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
//                            // 唔使用Engine 嘅obTapCommand 事件嘅话唔需要打开哩个设置
//                            // Serveic 的onTouchEvent 事件系一直会触发嘅
//                    .setAcceptsTapEvents(true)
//
//                    .setStatusBarGravity(testGravity)
//                    /*---------------------------------------------*/
//                    .build());
//
//            Log.d("EngineTest", "Gravity:" + testGravity);
//            testGravity ++;
//        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            // Wing 暂时唔做哩个模式
            System.out.print("onPropertiesChanged-mLowBitAmbient:");
            System.out.print(mLowBitAmbient);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            System.out.print("->");
            System.out.println(mLowBitAmbient);
        }

        /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
        @Override
        public void needPostInvalidate() {
            postInvalidate();
        }
        /*---------------------------------------------*/

        /**
         * 每分钟触发一次
         */
        @Override
        public void onTimeTick() {
            super.onTimeTick();
            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            Log.v("Engine" , "onTimeTick");
            mFaceDrawer.setTimeToNow(); // 更新时间
            /*---------------------------------------------*/
            invalidate();
        }

        /**
         * 响应环境模式和交互模式间切换
         * @param inAmbientMode
         */
        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            mFaceDrawer.setIsAmbient(inAmbientMode);
            /*---------------------------------------------*/

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            int width = bounds.width();
            int height = bounds.height();

            // Draw the background.
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            float secRot = mTime.second / 30f * (float) Math.PI;
            int minutes = mTime.minute;
            float minRot = minutes / 30f * (float) Math.PI;
            float hrRot = ((mTime.hour + (minutes / 60f)) / 6f) * (float) Math.PI;

            float secLength = centerX - 20;
            float minLength = centerX - 40;
            float hrLength = centerX - 80;

            if (!mAmbient) {
                float secX = (float) Math.sin(secRot) * secLength;
                float secY = (float) -Math.cos(secRot) * secLength;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mHandPaint);
            }

            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mHandPaint);

            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHandPaint);

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            mFaceDrawer.Draw(canvas, bounds);
            /*---------------------------------------------*/
        }

        /**
         * 响应可见模式切换
         * @param visible
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            mFaceDrawer.setIsVisible(visible);
            /*---------------------------------------------*/
            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * 注册广播接收器
         */
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            TempletWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        /**
         * 注销广播接收器
         */
        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            TempletWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<TempletWatchFace.Engine> mWeakReference;

        public EngineHandler(TempletWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            TempletWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }


}
