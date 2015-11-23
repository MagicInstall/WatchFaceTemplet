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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * TODO 试下将WatchFace 改名为TempletWatchFace, TempletWatchFace改名为TestWatchFace
 */
public class TempletWatchFace extends WatchFace {
    Paint mBackgroundPaint;
    Paint mHandPaint;

//    Time mTime;

    @Override
    public void onCreate(SurfaceHolder holder) {

        Resources resources = TempletWatchFace.this.getResources();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(resources.getColor(R.color.analog_background));

        mHandPaint = new Paint();
        mHandPaint.setColor(resources.getColor(R.color.analog_hands));
        mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
        mHandPaint.setAntiAlias(true);
        mHandPaint.setStrokeCap(Paint.Cap.ROUND);

//        mTime = new Time();
    }

    /**
     * 当发生日期变化/ 系统时间设置改变/ 同埋时区变化, 哩三种情况, 就触发哩个事件;
     * 另外, 当喺屏幕着咗嘅时候, 亦会每分钟触发一次
     * 一般只需要用一句invalidate() 重绘一下就得.
     * 唔使调用基类方法.
     */
    @Override
    public void onTimeChanged() {
        invalidate();
    }

    /**
     * 交互模式切换事件.
     * </br>
     * 唔需要调用父类方法.
     *
     * @param inInteractiveMode true = 交互模式
     */
    @Override
    public void onInteractiveModeChanged(boolean inInteractiveMode) {
        if (inInteractiveMode)
            startAnimation(
                    1000 - (System.currentTimeMillis() % 1000)
            );
        else
            stopAnimation();
    }

    /**
     * 响应可见模式切换
     *
     * @param visible true = 屏幕着咗
     */
    @Override
    public void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible) invalidate();
    }

    /**
     * 低像素环境切换事件.
     * 一般将用到嘅画笔对象, 放喺哩度切换抗锯齿开关
     *
     * @param inLowBitAmbient true=低像素环境
     */
    @Override
    public void onLowBitAmbientChanged(boolean inLowBitAmbient) {
        mHandPaint.setAntiAlias(!inLowBitAmbient);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        setTimeToNow(); // 更新时间
//        mTime.setToNow();

//        int width = bounds.width();
//        int height = bounds.height();

        // Draw the background.
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);

        // Find the center. Ignore the window insets so that, on round watches with a
        // "chin", the watch face is centered on the entire screen, not just the usable
        // portion.
        float centerX = mWidthPixels / 2f;
        float centerY = mHeightPixels / 2f;

        float secRot = getSecond()/*mTime.second*/ / 30f * (float) Math.PI;
        int minutes = getMinute();/*mTime.minute*/;
        float minRot = minutes / 30f * (float) Math.PI;
        float hrRot = ((getHour12()/*mTime.hour*/ + (minutes / 60f)) / 6f) * (float) Math.PI;

        float secLength = centerX - 20;
        float minLength = centerX - 40;
        float hrLength = centerX - 80;

        if (isInteractiveMode()) {
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

    }

//        /**
//         * 响应可见模式切换
//         * @param visible
//         */
//        @Override
//        public void onVisibilityChanged(boolean visible) {
//            super.onVisibilityChanged(visible);
//
//            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
//            mFaceDrawer.setIsVisible(visible);
//            /*---------------------------------------------*/
//            if (visible) {
//                registerReceiver();
//
//                // Update time zone in case it changed while we weren't visible.
//                mTime.clear(TimeZone.getDefault().getID());
//                mTime.setToNow();
//            } else {
//                unregisterReceiver();
//            }
//
//            // Whether the timer should be running depends on whether we're visible (as well as
//            // whether we're in ambient mode), so we may need to start or stop the timer.
//            updateTimer();
//        }


}
