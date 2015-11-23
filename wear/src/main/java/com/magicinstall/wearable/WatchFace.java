package com.magicinstall.wearable;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by wing on 15/11/22.
 * 哩个类用嚟代替CanvasWatchFaceService 类成为项目主类嘅父类, 令到主类嘅代码更简洁.
 */
public class WatchFace extends CanvasWatchFaceService {
    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
    private static final String TAG = "Wing.WatchFace";

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
    public static final int STATUSBAR_GRAVITY_DEFAULT = 0; // 0-2 4 6-17 19-31 128-159 256-287
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

    /**
     * 表盘宽度, 一般唔好修改佢,
     * 等同于getMonitoWidth(),
     * 子类可以跳过getMonitoWidth() 方法直接读取哩个数,
     * 提高Draw 的性能.
     */
    protected int mWidthPixels;
    /**
     * 取得屏幕宽度
     * @return 单位是像素.
     */
    public int getMonitoWidth() {return mWidthPixels;}
    /**
     * 表盘高度, 一般唔好修改佢,
     * 等同于getMonitoHeight(),
     * 子类可以跳过getMonitoHeight() 方法直接读取哩个数,
     * 提高Draw 的性能.
     */
    protected int mHeightPixels;
    /**
     * 取得屏幕高度
     * @return 单位是像素.
     */
    public int getMonitoHeight() {return mHeightPixels;}

    // 响应交互模式切换
    private boolean mIsInteractive;
    /**
     * 查询系唔系处于交互模式
     * @return true=交互模式
     */
    public boolean isInteractiveMode() {return mIsInteractive;}
    /**
     * 交互模式切换事件.
     * @param inInteractiveMode true = 交互模式
     */
    public void onInteractiveModeChanged(boolean inInteractiveMode) {}

    /**
     * 查询屏幕有冇着
     * @return  true = 屏幕着咗
     */
    public boolean isVisible() {return mEngine.isVisible();}
    /**
     * 可见模式切换事件
     * @param visible true = 屏幕着咗
     */
    public void onVisibilityChanged(boolean visible) {}

    /**
     * 查询系唔系处于环境模式
     * @return
     */
    public boolean isInAmbientMode() {return mEngine.isInAmbientMode();}
    /**
     * 环境模式切换事件
     * @param inAmbientMode true = 环境模式
     */
    public void onAmbientModeChanged(boolean inAmbientMode) {}

    /**
     * 查询系唔系处于低像素环境
     * @return true=低像素环境;
     */
    public boolean isLowBitAmbient(){
        return mEngine.mLowBitAmbient;
    }

    /**
     * 低像素环境切换事件.
     * 一般将用到嘅画笔对象, 放喺哩度切换抗锯齿开关
     * @param inAmbientMode true=低像素环境
     */
    public void onLowBitAmbientChanged(boolean inAmbientMode) {}

    // 保留一个Engine 嘅引用, 用嚟将佢嘅一D 方法曝露到WatchFace 类.
    private Engine mEngine = new Engine();

    /**
     * 请求重绘, 由CanvasWatchFaceService 类调用onDraw() 方法.
     * 注意: 必须系喺UI线程先至可以直接用哩个方法, 工作者线程可以用postInvalidate() 方法.
     */
    public final void invalidate(){
        // 映射到Engine 的invalidate() 方法.
        mEngine.invalidate();
    }

    /**
     * 请求重绘, 由CanvasWatchFaceService 类调用onDraw() 方法.
     * 可以喺工作者线程直接用哩个方法将重绘请求提交到UI线程.
     */
    public final void postInvalidate(){
        // 映射到Engine postInvalidate() 方法.
        mEngine.postInvalidate();
    }

    /**
     * 准备初始化表盘的事件.
     * @param holder
     */
    public void onCreate(SurfaceHolder holder) {}

    /**
     * 表盘被系统回收的事件.
     */
    public void onDestroy() {}

    /**
     * 每分钟触发一次
     */
    public void onTimeTick() {}

    /**
     * 表盘重绘事件
     * @param canvas
     * @param bounds
     */
    public void onDraw(Canvas canvas, Rect bounds) {}

    /*---------------------------------------------*/

    /**
     * TODO 改名为定时重绘MSG
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    /**
     * 哩个方法由WallpaperService 类调用.
     * @return
     */
    @Override
    public final Engine onCreateEngine() {
        return mEngine;
    }


    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
    /**
     * 哩个方法用嚟简化onCreate 事件内嘅代码,
     * 将WatchFaceStyle 对象嘅设置代码放喺哩度,
     *
     * @param defaultBuilder WatchFace 类喺其自身嘅onCreate 事件内,
     *                       会创建一个包含默认设置值嘅Builder 对象,
     *                       一般只修改哩个对象嘅一两个属性设置就可以用;
     *                       例如:修改StatusBarGravity, 或者打开AcceptsTapEvents...
     *                       </br>
     *                       唔需要调用Builder 对象嘅Build 方法,
     *                       WatchFace 类会去调用佢嘅Build 方法.
     * @return 返回一个WatchFaceStyle.Builder 对象供WatchFace 类使用.
     */
    public WatchFaceStyle.Builder onSetWatchFaceStyle(WatchFaceStyle.Builder defaultBuilder){
        // 唔使用Engine 嘅obTapCommand 事件嘅话唔需要打开哩个设置
        // Serveic 的onTouchEvent 事件系一直会触发嘅
//         defaultBuilder.setAcceptsTapEvents(true)

//         .setStatusBarGravity(5)
        return defaultBuilder;
    }

    /*---------------------------------------------*/

    private class Engine extends CanvasWatchFaceService.Engine
    {

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // 设置WatchFaceStyle
            WatchFaceStyle.Builder style_builder = new WatchFaceStyle.Builder(WatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false);

            setWatchFaceStyle(onSetWatchFaceStyle(style_builder)
                    .build()
            );

//            mFaceDrawer = new InformationsDrawer(this, resources, getBaseContext());

            // 取得屏幕尺寸
            DisplayMetrics display_metrics =
                    WatchFace.this.getResources().getDisplayMetrics();
            Log.i(TAG + ".Engine", display_metrics.toString());
            mWidthPixels = display_metrics.widthPixels;
            mHeightPixels = display_metrics.heightPixels;

            // 转发onCreate 事件.
            WatchFace.this.onCreate(holder);
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
            WatchFace.this.onDestroy();
            // TODO 删除
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        /**
         * Wing 暂时唔做哩个模式, 将哩个方法设置成final
         * @param properties
         */
        @Override
        public final void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            System.out.print("onPropertiesChanged-mLowBitAmbient:");
            System.out.print(mLowBitAmbient);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            System.out.print("->");
            System.out.println(mLowBitAmbient);

            // 触发低像素环境切换事件
            onLowBitAmbientChanged(mLowBitAmbient);
        }

        /**
         * Engine 响应可见模式切换 TODO 考虑下广播接收器点处理
         * @param visible true = 屏幕着咗
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
//                mTime.clear(TimeZone.getDefault().getID());
//                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            WatchFace.this.onVisibilityChanged(visible);
//            mFaceDrawer.setIsVisible(visible);
            // 判断系唔系交互模式
            if (isVisible() && !isInAmbientMode() && !mIsInteractive) {
                mIsInteractive = true;
                onInteractiveModeChanged(true);
            }
            else if (!isVisible() || isInAmbientMode()) {
                mIsInteractive = false;
                onInteractiveModeChanged(false);
            }
            /*---------------------------------------------*/
        }

        /**
         * Engine 响应环境模式和交互模式间切换
         * @param inAmbientMode true = 环境模式
         */
        @Override
        public final void onAmbientModeChanged(boolean inAmbientMode) {
//            if (mAmbient != inAmbientMode) {
//                mAmbient = inAmbientMode;
//                if (mLowBitAmbient) {
//                    mHandPaint.setAntiAlias(!inAmbientMode);
//                }
            // TODO 考虑下需唔需要留俾子类去做
            invalidate();
//            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();

            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            // 转发事件
            WatchFace.this.onAmbientModeChanged(inAmbientMode);

            // 判断系唔系交互模式
            if (isVisible() && !isInAmbientMode() && !mIsInteractive) {
                mIsInteractive = true;
                onInteractiveModeChanged(true);
            }
            else if (!isVisible() || isInAmbientMode()) {
                mIsInteractive = false;
                onInteractiveModeChanged(false);
            }
            /*---------------------------------------------*/
        }

        /**
         * 喺屏幕着嗰下触发一次, 然后每分钟触发一次
         * 一般用嚟喺环境模式每分钟重绘一次, 咁嘅话, 只需要一句invalidate() 就得.
         */
        @Override
        public final void onTimeTick() {
            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            WatchFace.this.onTimeTick();
//            Log.v(TAG + ".Engine", "onTimeTick");
//            WatchFace.this.setTimeToNow(); // 更新时间
            /*---------------------------------------------*/
//            invalidate();
        }

        /**
         * 转发重绘事件
         * @param canvas
         * @param bounds
         */
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
            // 触发重绘事件.
            WatchFace.this.onDraw(canvas, bounds);
            /*---------------------------------------------*/
        }

        // time-zone 广播接收
        // TODO 点样可以喺WatchFaceService 类注册, 哩度接收?
        final android.content.BroadcastReceiver mTimeZoneReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                mTime.clear(intent.getStringExtra("time-zone"));
//                mTime.setToNow();

                /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
//                WatchFace.this.setTimeToNow();

                // 响应时区切换 FIXME WatchFaceService抢注咗哩个广播, 搞到我个BroadcastReceiver 类冲突, 试下点样可以只接收
                //
                WatchFace.this.setTimeZone(intent.getStringExtra("time-zone"));
                /*---------------------------------------------*/
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        /**
         * 注册广播接收器
         */
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        /**
         * 注销广播接收器
         */
        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        // 下边一堆嘢要用哩个Handler
        final Handler mUpdateTimeHandler = new EngineHandler(this);

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
         * Update rate in milliseconds for interactive mode. We update once a second to advance the
         * second hand.
         */
        private final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

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

        /**
         * TODO 挠挠的关键代码
         * TODO X>319.0f = 挠挠
         * TODO 加一个开关set控制
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
            Log.d(TAG + ".Engine", String.format(
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
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<WatchFace.Engine> mWeakReference;

        public EngineHandler(WatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }


    /*+++++++++++++++++++ Wing ++++++++++++++++++++*/
    /**
     * 每个表盘都需要嘅日历对象,
     * 一般唔好修改佢
     */
    protected GregorianCalendar mCalendar  = new GregorianCalendar();

    /**
     * 更新当前时间.
     */
    public void setTimeToNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * 设置时区.
     * @param id 表示时区ID 嘅字串.
     */
    public void setTimeZone(String id) {
        mCalendar.setTimeZone(TimeZone.getTimeZone(id));
    }

    /**
     *
     * @return 取得年份.
     */
    public int getYear() {
        return mCalendar.get(Calendar.YEAR);
    }

    /**
     *
     * @return 取得月份.
     */
    public int getMonth() {
        return mCalendar.get(Calendar.MONTH) + 1; // 原方法返回嘅月份从0开始
    }

    /**
     *
     * @return 取得日号.
     */
    public int getDay() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     *
     * @return 取得12小时制嘅小时.
     */
    public int getHour12() {
        return mCalendar.get(Calendar.HOUR);
    }

    /**
     *
     * @return 取得24小时制嘅小时.
     */
    public int getHour24() {
        return mCalendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     *
     * @return 取得分钟.
     */
    public int getMinute() {
        return mCalendar.get(Calendar.MINUTE);
    }

    /**
     *
     * @return 取得秒.
     */
    public int getSecond() {
        return mCalendar.get(Calendar.SECOND);
    }

    /**
     * 取得星期几.
     * @return 星期日:0, 星期一:1...
     */
    public int getWeeks() {
        return mCalendar.get(Calendar.DAY_OF_WEEK) - 1; // 原方法返回星期日:1, 星期一:2... 星期六:7
    }

    /**
     * 取得昼夜划分.
     * @return AM:0, PM:1
     */
    public int getAM_PM() {
        return mCalendar.get(Calendar.AM_PM);
    }

    /*---------------------------------------------*/
}
