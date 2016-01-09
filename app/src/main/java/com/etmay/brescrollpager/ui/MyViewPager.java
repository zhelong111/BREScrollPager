package com.etmay.brescrollpager.ui;

/**
 * Created by Bruce on 2016/1/9.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Timer;
import java.util.TimerTask;

public class MyViewPager extends ViewGroup {

    /** 手势识别器 */
    private GestureDetector detector;
    /** 上下文 */
    private Context ctx;
    /** 第一次按下的X轴的坐标 */
    private int firstDownX;
    /** 记录当前View的id */
    private int currId = 0;
    /** 模拟动画工具 */
    private MyScroller myScroller;

    private int childWidth;
    private int childHeight;
    private int childPadding = 160;
    private float velocityX = 0;
    private static final int MIN_SCROLL_VELOCITY = 4000;
    private Timer timer; // 滚动定时器
    private Handler handler;
    private int timerIndex;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init();
    }

    private void init() {
        timer = new Timer();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                setCurrItem(timerIndex % getChildCount());
            }
        };

        myScroller = new MyScroller(ctx);
        detector = new GestureDetector(ctx,
                new GestureDetector.OnGestureListener() {

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                            float distanceX, float distanceY) {
                        // 手指滑动
                        scrollBy((int) distanceX, 0);
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        MyViewPager.this.velocityX = velocityX;
                        return false;
                    }

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return false;
                    }
                });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        childWidth = getWidth() - childPadding;
        childHeight = getHeight() - childPadding;
    }

    /**
     * 对子View进行布局，确定子View的位置 changed 若为true，
     * 说明布局发生了变化 l
      指当前View位于父View的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);

//            ViewHelper.setScaleX(view, ViewHelper.getScaleX(view) * .9f);
//            ViewHelper.setScaleY(view, ViewHelper.getScaleY(view) * .9f);

            // 指定子View的位置 ，左、上、右、下，是指在ViewGroup坐标系中的位置

            int left = (getWidth() - childWidth)/2;
            view.layout(left + i * childWidth , 0, left + childWidth + i * childWidth ,
                        childHeight);

            if (i != 0) {
                ViewHelper.setScaleX(view, (childWidth - childPadding/2.0f) * 1.0f/childWidth );
                ViewHelper.setScaleY(view, (childHeight - childPadding/2.0f) * 1.0f/childHeight );
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event); // 指定手势识别器去处理滑动事件
        // 还是得自己处理一些逻辑
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN : // 按下
                firstDownX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE : // 移动
                break;
            case MotionEvent.ACTION_UP : // 抬起
                int nextId = 0; // 记录下一个View的id
//                if (Math.abs(myScroller.getCurrVelocity()) > 600) {
                Log.e("ss", "---------" + velocityX) ;
                if (Math.abs(velocityX) > MIN_SCROLL_VELOCITY) {
//                    if (event.getX() - firstDownX > 60) {
//                        // 手指离开点的X轴坐标-firstDownX > 屏幕宽度的一半，左移
//                        nextId = (currId - 1) <= 0 ? 0 : currId - 1;
//                    } else if (firstDownX - event.getX() > 60) {
//                        // 手指离开点的X轴坐标 - firstDownX < 屏幕宽度的一半，右移
//                        nextId = currId + 1;
//                    } else {
//                        nextId = currId;
//                    }
                    if (velocityX > MIN_SCROLL_VELOCITY) {
                        // 手指离开点的X轴坐标-firstDownX > 屏幕宽度的一半，左移
                        nextId = (currId - 1) <= 0 ? 0 : currId - 1;
                        if (nextId == currId - 1) {
                            timerIndex--;
                        }
                    } else if (velocityX < -MIN_SCROLL_VELOCITY) {
                        // 手指离开点的X轴坐标 - firstDownX < 屏幕宽度的一半，右移
                        nextId = currId + 1;
                    } else {
                        nextId = currId;
                    }
                } else {
                    if (event.getX() - firstDownX > getWidth() / 5.0f) {
                        // 手指离开点的X轴坐标-firstDownX > 屏幕宽度的一半，左移
                        nextId = (currId - 1) <= 0 ? 0 : currId - 1;
                        if (nextId == currId - 1) {
                            timerIndex--;
                        }
                    } else if (firstDownX - event.getX() > getWidth() / 5.0f) {
                        // 手指离开点的X轴坐标 - firstDownX < 屏幕宽度的一半，右移
                        nextId = currId + 1;
                    } else {
                        nextId = currId;
                    }
                }
                moveToDest(nextId);
                break;
            default :
                break;
        }
        return true;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.e("ss", "---------" + (getScrollX() - currId * getWidth())/(getWidth() * 1.0f) ) ;
    }

    /**
     * 控制视图的移动
     *
     * @param nextId
     */
    private void moveToDest(int nextId) {

        boolean shouldAnim = (currId != nextId);
//        shouldAnim = false;
        View child;
        if (shouldAnim) {
            child = getChildAt(currId);
            ObjectAnimator.ofFloat(child, "scaleX", 1f, 0.9f).setDuration(300).start();
            ObjectAnimator.ofFloat(child, "scaleY", 1f, 0.9f).setDuration(300).start();
        }

        // nextId的合理范围是，nextId >=0 && nextId <= getChildCount()-1
        currId = (nextId >= 0) ? nextId : 0;
        currId = (nextId <= getChildCount() - 1)
                ? nextId
                : (getChildCount() - 1);

        // 视图移动,太直接了，没有动态过程
        // scrollTo(currId * getWidth(), 0);
        // 要移动的距离 = 最终的位置 - 现在的位置
//        int distanceX = currId * getWidth() - getScrollX();
        int distanceX = currId * childWidth - getScrollX();
        // 设置运行的时间
        myScroller.startScroll(getScrollX(), 0, distanceX, 0);

        if (shouldAnim) {
            child = getChildAt(currId);
            ObjectAnimator.ofFloat(child, "scaleX", 0.9f, 1f).setDuration(300).start();
            ObjectAnimator.ofFloat(child, "scaleY", 0.9f, 1f).setDuration(300).start();
        }

        // 刷新视图
        invalidate();
    }

    public void setCurrItem(int itemIndex) {
        moveToDest(itemIndex);
    }

    public void startSchedule(long timeMillis) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerIndex++;
                handler.sendEmptyMessage(0);
            }
        }, timeMillis, timeMillis);
    }

    public void stopSchecule() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * invalidate();会导致这个方法的执行
     */
    @Override
    public void computeScroll() {
        if (myScroller.computeScrollOffset()) {
            int newX = (int) myScroller.getCurrX();
            scrollTo(newX, 0);
            invalidate();
        }
    }

}