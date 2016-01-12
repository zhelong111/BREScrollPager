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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Bruce on 2016/1/9.
 */
public class MyViewPager extends ViewGroup {

    private GestureDetector detector; // 手势识别器
    private Context ctx; // 上下文
    private int firstDownX; // 第一次按下的X轴的坐标
    private int currId = 0; // 记录当前View的index
    private MyScroller myScroller; // 模拟动画工具
    private int childWidth;
    private int childHeight;
    private int childPadding = 160;
    private float velocityX = 0;
    private static int MIN_SCROLL_VELOCITY = 1000;
    private Timer timer; // 滚动定时器
    private Handler handler;
    private int timerIndex; // 定时滚动到的view的index
    private boolean isTouchedOn; // 手指是否停留在视图上
    private long scrollInterval; // 定时滚动的周期ms
    private boolean isScheduleScroll; // 是否设置了定时滚动page
    private float currTouchX; // 手指按下的位置
    private PageTransformer pagerTransformer;
    private float initScaleX;
    private float initScaleY;
    private float initAlpha = 0.5f;
    private OnItemClickListener onItemClickListener;
    private OnPageChangeListener onPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setPagerTransformer(PageTransformer pagerTransformer) {
        this.pagerTransformer = pagerTransformer;
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init();
    }

    public interface OnItemClickListener {
        void onItemClicked(View itemView, int position);
    }

    public interface OnPageChangeListener {
        void onPageChanged(int position);
    }

    private void init() {
        MIN_SCROLL_VELOCITY *= getResources().getDisplayMetrics().density;
        Log.e("ss", MIN_SCROLL_VELOCITY + "");
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
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClicked(getChildAt(getCurrItem()), getCurrItem());
                        }
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                            float distanceX, float distanceY) {
                        // 手指拖动
                        scrollBy((int) distanceX, 0);
                        if (pagerTransformer != null) {
                            int currChildIndex = (int) (currTouchX/childWidth);
                            if (currChildIndex < getChildCount()) {
                                pagerTransformer.transformPage(getChildAt(currChildIndex), getOffset());
                            }
                        }
//                        Log.e("ss", "----onScroll-----  " + getOffset()) ;
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
//                        currTouchX = getScrollX();
                        // 必须点击位置相对屏幕左边的距离，防止连续滑动出现bug
                        currTouchX = getScrollX() + e.getX() + getPaddingLeft()+1;
                        return false;
                    }
                });

    } // init

    /**
     * 获得当前View的偏移
     * @return -1 ~ 0 ~ 1
     */
    private float getOffset() {
        int currChildIndex = (int) (currTouchX/childWidth);
        float offset = (currChildIndex * childWidth - getScrollX()) / (childWidth * 1.0f);
        if (offset < 0) {
            return Math.max(offset, -1);
        } else {
            return Math.min(offset, 1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        childWidth = getWidth() - childPadding - getPaddingLeft() - getPaddingRight();
        childHeight = getHeight() - 0 - getPaddingTop() - getPaddingBottom();

        measureChildren(widthMeasureSpec - childPadding, heightMeasureSpec);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        initScaleX = (childWidth - childPadding / 2f) / childWidth;
        initScaleY = (childHeight - childPadding / 2f) / childHeight;
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
            // 指定子View的位置 ，左、上、右、下，是指在ViewGroup坐标系中的位置
            int left = (getWidth() - childWidth)/2;
            int top = (getHeight() - childHeight)/2;
            view.layout(left + i * childWidth, top, left + childWidth +  i * childWidth,
                    top + childHeight);

            if (i != 0) {
                ViewHelper.setScaleX(view, initScaleX);
                ViewHelper.setScaleY(view, initScaleY);
//                ViewHelper.setAlpha(view, initAlpha);
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
                isTouchedOn = true;
                stopSchecule(); // 停止定时滚动
                firstDownX = (int) event.getX();
                velocityX = 0;
                break;
            case MotionEvent.ACTION_MOVE : // 移动
                break;
            case MotionEvent.ACTION_UP : // 抬起
                isTouchedOn = false;
                if (isScheduleScroll) { // 手指离开屏幕时，若果设置了定时滚动，则重新滚动起来
                    startSchedule(scrollInterval);
                }
                int nextId = 0; // 记录下一个View的id
//                if (Math.abs(myScroller.getCurrVelocity()) > 600) {
//                Log.e("ss", "---------" + velocityX) ;
                if (Math.abs(velocityX) > MIN_SCROLL_VELOCITY) {
                    if (velocityX > MIN_SCROLL_VELOCITY) {
                        nextId = (currId - 1) <= 0 ? 0 : currId - 1;
                        if (nextId == currId - 1) {
                            timerIndex--;
                        }
                    } else if (velocityX < -MIN_SCROLL_VELOCITY) {
                        nextId = currId + 1;
                        if (nextId < getChildCount() - 1) {
                            timerIndex++;
                        }
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
                        if (nextId < getChildCount() - 1) {
                            timerIndex++;
                        }
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

    public int getCurrItem() {
        return (int)(currTouchX / childWidth);
    }

    public View getLeftView() {
        int currItemIndex = (int)(currTouchX / childWidth);
        if (currItemIndex > 0) {
            return getChildAt(currItemIndex - 1);
        }
        return null;
    }

    public View getRightView() {
        int currItemIndex = (int)(currTouchX / childWidth);
        if (currItemIndex < getChildCount() - 1) {
            return getChildAt(currItemIndex + 1);
        }
        return null;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (currId != (getScrollX() + childPadding + childWidth) / childWidth) {
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageChanged(currId);
            }
        }
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
            ObjectAnimator.ofFloat(child, "scaleX", 1f, initScaleX).setDuration(300).start();
            ObjectAnimator.ofFloat(child, "scaleY", 1f, initScaleY).setDuration(300).start();
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
            ObjectAnimator.ofFloat(child, "scaleX", initScaleX, 1f).setDuration(300).start();
            ObjectAnimator.ofFloat(child, "scaleY", initScaleY, 1f).setDuration(300).start();
        }

        // 刷新视图
        invalidate();
    }

    public void setCurrItem(int itemIndex) {
        moveToDest(itemIndex);
    }

    public void startSchedule(final long timeMillis) {
        if (timer == null) {
            timer = new Timer();
        }
        isScheduleScroll = true;
        scrollInterval = timeMillis;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isTouchedOn) { // 手指没有按住屏幕
                    timerIndex++;
                    handler.sendEmptyMessage(0); // 发送滚动消息
                }
            }
        }, scrollInterval, scrollInterval);
    }

    public void stopSchecule() {
        if (isScheduleScroll) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }

    /**
     * invalidate()会导致这个方法的执行
     */
    @Override
    public void computeScroll() {
        if (myScroller.computeScrollOffset()) {
            int newX = (int) myScroller.getCurrX();
            scrollTo(newX, 0);

            if (pagerTransformer != null) {
                int currChildIndex = (int) (currTouchX/childWidth);
                if (currChildIndex < getChildCount()) {
                    pagerTransformer.transformPage(getChildAt(currChildIndex), getOffset());
                }
            }
//            Log.e("ss", "----computeScroll-----  " + getOffset()) ;

            invalidate();
        }
    }

    public interface PageTransformer {
        /**
         * Apply a property transformation to the given page.
         *
         * @param page Apply the transformation to this page
         * @param position Position of page relative to the current front-and-center
         *                 position of the pager. 0 is front and center. 1 is one full
         *                 page position to the right, and -1 is one page position to the left.
         */
        void transformPage(View page, float position);
    }

}