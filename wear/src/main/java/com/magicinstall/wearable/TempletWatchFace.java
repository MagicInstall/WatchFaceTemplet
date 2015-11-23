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
import android.text.format.Time;
import android.view.SurfaceHolder;

/**
 * TODO 试下将WatchFace 改名为TempletWatchFace, TempletWatchFace改名为TestWatchFace
 */
public class TempletWatchFace extends WatchFace {
    Paint mBackgroundPaint;
    Paint mHandPaint;

    Time mTime;

    @Override
    public void onCreate(SurfaceHolder holder) {
        super.onCreate(holder);

        Resources resources = TempletWatchFace.this.getResources();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(resources.getColor(R.color.analog_background));

        mHandPaint = new Paint();
        mHandPaint.setColor(resources.getColor(R.color.analog_hands));
        mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
        mHandPaint.setAntiAlias(true);
        mHandPaint.setStrokeCap(Paint.Cap.ROUND);

        mTime = new Time();
    }

    /**
     * 喺屏幕着嗰下触发一次, 然后每分钟触发一次
     */
    @Override
    public void onTimeTick() {
        super.onTimeTick();
        invalidate();
    }

//    /**
//     * 响应可见模式切换
//     *
//     * @param visible true = 屏幕着咗
//     */
//    @Override
//    public void onVisibilityChanged(boolean visible) {
//        super.onVisibilityChanged(visible);
//        setTimeToNow();
//    }

    /**
     * 低像素环境切换事件.
     * 一般将用到嘅画笔对象, 放喺哩度切换抗锯齿开关
     *
     * @param inAmbientMode true=低像素环境
     */
    @Override
    public void onLowBitAmbientChanged(boolean inAmbientMode) {
        mHandPaint.setAntiAlias(!inAmbientMode);
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
        setTimeToNow(); // 更新时间
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

        if (!isInAmbientMode()) {
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
