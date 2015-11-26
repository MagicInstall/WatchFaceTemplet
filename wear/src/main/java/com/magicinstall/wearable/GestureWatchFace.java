package com.magicinstall.wearable;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by wing on 15/11/24.
 * 喺WatchFace 类嘅基础上增加更复杂嘅手势支持,
 * 其实主要目的系为咗支持Ticwatch 的挠挠触摸手势.
 * </br>
 * 如果只需要简单嘅单击手势, 唔需要用哩个类,
 * 只要喺WatchFace类嘅onSetWatchFaceStyle 事件之中,
 * 设置Builder 嘅setAcceptsTapEvents(true),
 * 就可以喺onTapCommand 收到事件.
 * TODO X>319.0f = 挠挠
 * </br>
 * 哩个类嘅代码非常之长! 头都晕埋!
 * 基本嘅思路系分开两个分析器,
 * 喺Engine 嘅onTouchEvent 事件中判断系主屏触摸定系挠挠触摸,
 * 然后分发事件到分析器,
 * 挠挠嘅分析器将手势发送至Engine, 主屏嘅分析器将手势发送至一个单独嘅内部类嘅实例,
 * Engine 同哩个内部类嘅实例都分别接收手势,
 * 最后将手势转发至GestureWatchFace.
 * </br>
 * 以onSidePanelXXXX 命名嘅都系挠挠嘅手势事件,
 * 其它系主屏嘅手势事件.
 */
public class GestureWatchFace extends WatchFace
{
    private static final String TAG = "Wing.Gesture";
    private static final float SIDE_PANEL_X_EDGE = 319.0f;

    /**
     * 一㩒主屏就触发事件
     * </br>
     * 唔需要调用父类方法.
     *
     * @param x
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onDown(float x, float y) {
        Log.v(TAG + ".Main", "onDown x:" + x + ",y:" + y);
        return false;
    }
    /**
     * 一㩒侧屏就触发事件
     * </br>
     * 唔需要调用父类方法.
     *
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelDown(float y) {
        Log.v(TAG + ".SidePanel", "onDown y:" + y);
        return false;
    }


    /**
     * 主屏单击事件
     * 最好用onSingleTapConfirmed 事件取代.
     * 触发顺序：
     * OnDown->OnsingleTapUp->OnsingleTapConfirmed
     * </br>
     * 唔需要调用父类方法.
     *
     * @param x
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSingleTapUp(float x, float y) {
        Log.v(TAG + ".Main", "onSingleTapUp x:" + x + ",y:" + y);
        return false;
    }
    /**
     * 侧屏单击事件
     * 最好用onSidePanelSingleTapConfirmed 事件取代.
     * 触发顺序：
     * OnSidePanelDown->onSidePanelSingleTapUp->onSidePanelSingleTapConfirmed
     * </br>
     * 唔需要调用父类方法.
     *
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelSingleTapUp(float y) {
        Log.v(TAG + ".SidePanel", "onSingleTapUp y:" + y);
        return false;
    }

    /**
     * 确定嘅主屏单击事件
     * 分析器确定喺onSingleTapUp 事件之后冇再产生点击(即系双击事件),
     * 就会触发哩个事件.
     * 触发顺序：
     * OnDown->OnsingleTapUp->OnsingleTapConfirmed
     * </br>
     * 唔需要调用父类方法.
     *
     * @param x
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSingleTapConfirmed(float x, float y) {
        Log.v(TAG + ".Main", "onSingleTapConfirmed x:" + x + ",y:" + y);
        return false;
    }

    /**
     * 确定嘅侧屏单击事件
     * 分析器确定喺onSidePanelSingleTapUp 事件之后冇再产生点击(即系双击事件),
     * 就会触发哩个事件.
     * 触发顺序：
     * OnSidePanelDown->onSidePanelSingleTapUp->onSidePanelSingleTapConfirmed
     * </br>
     * 唔需要调用父类方法.
     *
     * @param y
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelSingleTapConfirmed(float y) {
        Log.v(TAG + ".SidePanel", "onSingleTapConfirmed y:" + y);
        return false;
    }

    /**
     * 主屏长㩒手势事件
     * 触发顺序：
     * onDown->onShowPress->onLongPress
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 注意: 由于Ticwatch 默认长㩒手势会切换到更换表盘,
     *      目前未有办法更改系统嘅设置,
     *      所以最好酌情使用.
     *
     * @param x
     * @param y
     */
    public void onLongPress(float x, float y) {
        Log.v(TAG + ".Main", "onLongPress x:" + x + ",y:" + y);
    }
    /**
     * 侧屏长㩒手势事件
     * 触发顺序：
     * onSidePanelDown->onSidePanelShowPress->onSidePanelLongPress
     * </br>
     * 唔需要调用父类方法.
     *
     * @param y
     */
    public void onSidePanelLongPress(float y) {
        Log.v(TAG + ".SidePanel", "onLongPress y:" + y);
    }

    /**
     * 喺onDown 同onLongPress事件之间触发
     * 触发顺序：
     * onDown->onShowPress->onLongPress
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 注意: 由于Ticwatch 默认长㩒手势会切换到更换表盘,
     *      目前未有办法更改系统嘅设置,
     *      所以最好酌情使用.
     *
     * @param x
     * @param y
     */
    public void onShowPress(float x, float y) {
        Log.v(TAG + ".Main", "onShowPress x:" + x + ",y:" + y);
    }
    /**
     * 喺onSidePanelDown 同onSidePanelLongPress事件之间触发
     * 触发顺序：
     * onSidePanelDown->onSidePanelShowPress->onSidePanelLongPress
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 实测喺Ticwatch 准确率较低, 非常容易触发onSidePanelLongPress,
     * 唔建议使用.
     *
     * @param y
     */
    public void onSidePanelShowPress(float y) {
        Log.v(TAG + ".SidePanel", "onShowPress y:" + y);
    }

    /**
     * 主屏双击事件
     * </br>
     * 唔需要调用父类方法.
     *
     * @param x
     * @param y
     * @return 返回false 表示叫分析器继续做嘢;
     *         如果唔想分析器继续分析后面嘅手势, 可以返回一个true,
     *         哩个事件有些许特殊, 双击事件之后,
     *         唔放开手, 可以喺onDoubleTapEvent 事件继续响应后面嘅事件(例如滚动).
     */
    public boolean onDoubleTap(float x, float y) {
        Log.v(TAG + ".Main", "onDoubleTap x:" + x + ",y:" + y);
        return false;
    }
    /**
     * 侧屏双击事件
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 实测喺Ticwatch 准确率较低, 多数情况只触发单击事件,
     * 唔建议使用.
     *
     * @param y
     * @return 返回false 表示叫分析器继续做嘢;
     *         如果唔想分析器继续分析后面嘅手势, 可以返回一个true,
     *         哩个事件有些许特殊, 双击事件之后,
     *         唔放开手, 可以喺onSidePanelDoubleTapEvent 事件继续响应后面嘅事件(例如滚动).
     */
    public boolean onSidePanelDoubleTap(float y) {
        Log.v(TAG + ".SidePanel", "onDoubleTap y:" + y);
        return false;
    }

    /**
     * 喺主屏双击之后, 唔放开手, 后面触发嘅事件会送到哩度响应.
     * 可以实现类似微信嘅向上滑动取消发送之类嘅操作.
     * </br>
     * 唔需要调用父类方法.
     *
     * @param e 直接提供事件对象, 自己谂点样处理.
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.v(TAG + ".Main",
                "onDoubleTapEvent Action:" + e.getAction() + " x:" + e.getX() + ",y:" + e.getY());
        return false;
    }
    /**
     * 喺侧屏双击之后, 唔放开手, 后面触发嘅事件会送到哩度响应.
     * 可以实现类似微信嘅向上滑动取消发送之类嘅操作.
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 实测喺Ticwatch 准确率较低, 多数情况只触发单击事件,
     * 唔建议使用.
     *
     * @param e 直接提供事件对象, 自己谂点样处理.
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelDoubleTapEvent(MotionEvent e) {
        Log.v(TAG + ".SidePanel",
                "onDoubleTapEvent Action:" + e.getAction() + " y:" + e.getY());
        return false;
    }

    /**
     * 主屏滚动手势事件
     * 触发顺序：
     * onDown->onScroll->onScroll->...
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 注意: 由于Ticwatch 嚟表盘滑动系呼出各种系统界面,
     *      目前未有办法更改系统嘅设置,
     *      所以最好酌情使用.
     *
     * @param downX     最初落手嘅X轴位置.
     * @param downY     最初落手嘅Y轴位置.
     * @param distanceX X轴上相对于上一个onScroll 事件的移动距离;
     *                  唔系同最初落手嘅之间的距离.
     * @param distanceY Y轴上相对于上一个onScroll 事件的移动距离;
     *                  唔系同最初落手嘅之间的距离.
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onScroll(float downX, float downY, float distanceX, float distanceY) {
        Log.v(TAG + ".Main", "onScroll distanceX:" + distanceX + ",distanceY:" + distanceY);
        return false;
    }
    /**
     * 侧屏滚动手势事件
     * 触发顺序：
     * onSidePanelDown->onSidePanelScroll->onSidePanelScroll->...
     * </br>
     * 唔需要调用父类方法.
     *
     * @param downY     最初落手嘅Y轴位置.
     * @param distanceY Y轴上相对于上一个onScroll 事件的移动距离;
     *                  唔系同最初落手嘅之间的距离.
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelScroll(float downY, float distanceY) {
        Log.v(TAG + ".SidePanel", "onScroll distance:" + distanceY);
        return false;
    }

    /**
     * 主屏快速拨手势事件
     * 触发条件系快速拨屏后松开
     * 触发顺序：
     * onDown->onScroll->onScroll->...->onFling
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 注意: 由于Ticwatch 嚟表盘滑动系呼出各种系统界面,
     *      目前未有办法更改系统嘅设置,
     *      所以最好酌情使用.
     *
     * @param downX     最初落手嘅X轴位置.
     * @param downY     最初落手嘅Y轴位置.
     * @param velocityX X轴上的移动速度，像素/秒
     * @param velocityY Y轴上的移动速度，像素/秒
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onFling(float downX, float downY, float velocityX, float velocityY) {
        Log.v(TAG + ".Main", "onFling velocityX:" + velocityX + ",velocityY:" + velocityY);
        return false;
    }
    /**
     * 侧屏快速拨手势事件
     * 触发条件系快速拨屏后松开
     * 触发顺序：
     * onSidePanelDown->onSidePanelScroll->onSidePanelScroll->...->onSidePanelFling
     * </br>
     * 唔需要调用父类方法.
     * </br>
     * 实测喺Ticwatch 准确率较低, 极难触发哩个事件,
     * 唔建议使用.
     *
     * @param downY     最初落手嘅Y轴位置.
     * @param velocityY Y轴上的移动速度，像素/秒
     * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
     *         返回false 表示叫分析器继续做嘢.
     */
    public boolean onSidePanelFling(float downY, float velocityY) {
        Log.v(TAG + ".SidePanel", "onFling velocity:" + velocityY);
        return false;
    }

    /**
     * 主屏嘅手势分析器
     */
    protected GestureDetector mMainDetector = new GestureDetector(getBaseContext(), new GestureListener());

    /**
     * 主屏嘅手势事件接收器
     */
//    protected GestureListener mMainGestureListener = new GestureListener();

    /**
     * 哩个类转发主屏嘅触摸事件
     */
    protected class GestureListener
            implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        /**
         * 一㩒就触发事件
         *
         * @param e The down motion event.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return GestureWatchFace.this.onDown(e.getX(), e.getY());
        }

        /**
         * 单击事件
         * 触发顺序：
         * OnDown->OnsingleTapUp->OnsingleTapConfirmed
         *
         * @param e The up motion event that completed the first tap
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return GestureWatchFace.this.onSingleTapUp(e.getX(), e.getY());
        }

        /**
         * 确定嘅单击事件
         * 分析器确定喺onSingleTapUp 事件之后冇再产生点击(即系双击事件),
         * 就会触发哩个事件.
         * 触发顺序：
         * OnDown->OnsingleTapUp->OnsingleTapConfirmed
         *
         * @param e The down motion event of the single-tap.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return GestureWatchFace.this.onSingleTapConfirmed(e.getX(), e.getY());
        }

        /**
         * 长㩒手势事件
         * 触发顺序：
         * onDown->onShowPress->onLongPress
         *
         * @param e The initial on down motion event that started the longpress.
         */
        @Override
        public void onLongPress(MotionEvent e) {
            GestureWatchFace.this.onLongPress(e.getX(), e.getY());
        }

        /**
         * 喺onDown 同onLongPress事件之间触发
         * 触发顺序：
         * onDown->onShowPress->onLongPress
         *
         * @param e The down motion event
         */
        @Override
        public void onShowPress(MotionEvent e) {
            GestureWatchFace.this.onShowPress(e.getX(), e.getY());
        }

        /**
         * 双击事件
         *
         * @param e The down motion event of the first tap of the double-tap.
         * @return 返回false 表示叫分析器继续做嘢;
         *         如果唔想分析器继续分析后面嘅手势, 可以返回一个true,
         *         哩个事件有些许特殊, 双击事件之后,
         *         唔放开手, 可以喺onDoubleTapEvent 事件继续响应后面嘅事件(例如滚动).
         *
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return GestureWatchFace.this.onDoubleTap(e.getX(), e.getY());
        }

        /**
         * 喺双击之后, 唔放开手, 后面触发嘅事件会送到哩度响应.
         * 可以实现类似微信嘅向上滑动取消发送之类嘅操作.
         *
         * @param e The motion event that occurred during the double-tap gesture.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return GestureWatchFace.this.onDoubleTapEvent(e);
        }

        /**
         * 滚动手势事件
         * 触发顺序：
         * onDown->onScroll->onScroll->...
         *
         * @param e1        第1个ACTION_DOWN MotionEvent
         * @param e2        最后一个ACTION_MOVE MotionEvent
         * @param distanceX X轴上相对于上一个onScroll 事件的移动距离;
         *                  唔系e1 同e2 之间的距离.
         * @param distanceY Y轴上相对于上一个onScroll 事件的移动距离;
         *                  唔系e1 同e2 之间的距离.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return GestureWatchFace.this.onScroll(e1.getX(), e1.getY(), distanceX, distanceY);
        }

        /**
         * 快速拨手势事件
         * 触发条件系快速拨屏后松开
         * 触发顺序：
         * onDown->onScroll->onScroll->...->onFling
         *
         * @param e1        第1个ACTION_DOWN MotionEvent
         * @param e2        最后一个ACTION_MOVE MotionEvent
         * @param velocityX X轴上的移动速度，像素/秒
         * @param velocityY Y轴上的移动速度，像素/秒
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return GestureWatchFace.this.onFling(e1.getX(), e1.getY(), velocityX, velocityY);
        }
    }

    /**
     * 哩个方法由WallpaperService 类调用.
     * @return GestureWatchFace 类内部的Engine 类.
     */
    @Override
    public GestureWatchFace.Engine onCreateEngine() {
//        Log.d(TAG, "onCreateEngine");
        mEngine = (WatchFace.Engine)new Engine();
        return (GestureWatchFace.Engine)mEngine;
    }

    /**
     * 哩个Engine 除咗分发主屏触摸事件同挠挠触摸事件之外,
     * 仲充当挠挠嘅手势接收器.
     */
    public class Engine extends WatchFace.Engine
            implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
    {
        // 挠挠嘅手势分析器
        protected GestureDetector mSidePanelDetector = new GestureDetector(getBaseContext(), this);

        /**
         * 主触摸事件
         * TODO 要自己管理开关嘅逻辑.
         * @param event
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
//            Log.v(TAG + ".Engine", String.format(
//                    "onTouchEvent Action:%d x:%f,y:%f Pressure:%f Edge:%d Meta:%d ",
//                    event.getAction(),
//                    event.getX(),
//                    event.getY(),
//                    event.getPressure(),
//                    event.getEdgeFlags(),
//                    event.getMetaState()
//            ));
            if (event.getX() > SIDE_PANEL_X_EDGE)
                mSidePanelDetector.onTouchEvent(event);
            else
                mMainDetector.onTouchEvent(event);
        }

        /**
         * 一㩒就触发事件
         *
         * @param e The down motion event.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return GestureWatchFace.this.onSidePanelDown(e.getY());
        }

        /**
         * 单击事件
         * 触发顺序：
         * OnDown->OnsingleTapUp->OnsingleTapConfirmed
         *
         * @param e The up motion event that completed the first tap
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return GestureWatchFace.this.onSidePanelSingleTapUp(e.getY());
        }

        /**
         * 确定嘅单击事件
         * 分析器确定喺onSingleTapUp 事件之后冇再产生点击(即系双击事件),
         * 就会触发哩个事件.
         * 触发顺序：
         * OnDown->OnsingleTapUp->OnsingleTapConfirmed
         *
         * @param e The down motion event of the single-tap.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return GestureWatchFace.this.onSidePanelSingleTapConfirmed(e.getY());
        }

        /**
         * 长㩒手势事件
         * 触发顺序：
         * onDown->onShowPress->onLongPress
         *
         * @param e The initial on down motion event that started the longpress.
         */
        @Override
        public void onLongPress(MotionEvent e) {
            GestureWatchFace.this.onSidePanelLongPress(e.getY());

        }

        /**
         * 喺onDown 同onLongPress事件之间触发
         * 触发顺序：
         * onDown->onShowPress->onLongPress
         *
         * @param e The down motion event
         */
        @Override
        public void onShowPress(MotionEvent e) {
            GestureWatchFace.this.onSidePanelShowPress(e.getY());
        }

        /**
         * 双击事件
         *
         * @param e The down motion event of the first tap of the double-tap.
         * @return 返回false 表示叫分析器继续做嘢;
         *         如果唔想分析器继续分析后面嘅手势, 可以返回一个true,
         *         哩个事件有些许特殊, 双击事件之后,
         *         唔放开手, 可以喺onDoubleTapEvent 事件继续响应后面嘅事件(例如滚动).
         *
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return GestureWatchFace.this.onSidePanelDoubleTap(e.getY());
        }

        /**
         * 喺双击之后, 唔放开手, 后面触发嘅事件会送到哩度响应.
         * 可以实现类似微信嘅向上滑动取消发送之类嘅操作.
         *
         * @param e The motion event that occurred during the double-tap gesture.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return GestureWatchFace.this.onSidePanelDoubleTapEvent(e);
        }

        /**
         * 滚动手势事件
         * 触发顺序：
         * onDown->onScroll->onScroll->...
         *
         * @param e1        第1个ACTION_DOWN MotionEvent
         * @param e2        最后一个ACTION_MOVE MotionEvent
         * @param distanceX X轴上相对于上一个onScroll 事件的移动距离;
         *                  唔系e1 同e2 之间的距离.
         * @param distanceY Y轴上相对于上一个onScroll 事件的移动距离;
         *                  唔系e1 同e2 之间的距离.
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return GestureWatchFace.this.onSidePanelScroll(e1.getY(), distanceY);
        }

        /**
         * 快速拨手势事件
         * 触发条件系快速拨屏后松开
         * 触发顺序：
         * onDown->onScroll->onScroll->...->onFling
         *
         * @param e1        第1个ACTION_DOWN MotionEvent
         * @param e2        最后一个ACTION_MOVE MotionEvent
         * @param velocityX X轴上的移动速度，像素/秒
         * @param velocityY Y轴上的移动速度，像素/秒
         * @return 如果唔想分析器继续分析后面嘅手势, 可以返回一个true, 表示已经处理咗;
         *         返回false 表示叫分析器继续做嘢.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return GestureWatchFace.this.onSidePanelFling(e1.getY(), velocityY);
        }
    }
}
