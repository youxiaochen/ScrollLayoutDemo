package you.xiaochen;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

/**
 * Created by you on 2015/12/31.
 * 真正解决多层滑动嵌套问题
 */

public class ScrollLayout extends ViewGroup {

    /**
     * 滑动时的最大动画时间
     */
    private static final int MAX_DURATION = 600;

    private static final int MIN_FLING_VELOCITY = 400; // dips

    private static final int INVALID_POINTER = -1;

    private int mPointerId = INVALID_POINTER;
    /**
     * 滑动器
     */
    private Scroller mScroller;
    /**
     * 速度追踪器
     */
    private VelocityTracker mTracker;
    /**
     * 滑动最小单位
     */
    private int mTouchSlop;
    /**
     * 最大速度单位和最小速度单位
     */
    private int mMaxVelocity, mMinVelocity;
    /**
     * 记录上次点击的Y轴点
     */
    private float mLastMotionY, mLastMotionX;
    /**
     * 按下时的初始值
     */
    private float mInitialMotionX, mInitialMotionY;
    /**
     * 滑动的高度即第一个项的高度
     */
    private int scrollHeight;
    /**
     * 是否正在滑动
     */
    private boolean mIsBeingDragged;
    /**
     * 子控件是否正在滑动
     */
    private boolean mIsUnableToDrag;

    public ScrollLayout(Context context) {
        super(context);
        init(context);
    }

    public ScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;
        mMinVelocity = (int) (MIN_FLING_VELOCITY * density);
        //mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        //这里额外定义最小速度单位,不要太敏感
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //这里图方便,此控件一般是上层,具体需要修改可以根据需求定
    }

    /**
     * 当线性布局一样布局,  滑动的高度则为第一个item的高度
     * @param changed
     * @param l
     * @param t
     * @param r
     * @param b
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int top = getPaddingTop();
        if (getChildCount() > 1) {
            scrollHeight = getChildAt(0).getMeasuredHeight();
        } else {
            scrollHeight = 0;
        }
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = getChildAt(i);
            int childHeight = child.getMeasuredHeight();
            child.layout(paddingLeft, top, child.getMeasuredWidth(), childHeight + top);
            top += childHeight;//每次显示一个子项,实际高度就要加上子项的高度
        }
        if (changed) {
            scrollLayoutChanged();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        int actionIndex = MotionEventCompat.getActionIndex(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPointerId = MotionEventCompat.getPointerId(ev, 0);
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                mIsUnableToDrag = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mIsBeingDragged = true;  //正处于拖拽中
                } else {
                    mIsBeingDragged = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //这里根据android定义的水平与垂直滑动的标准
                if (mIsBeingDragged) {
                    //Log.i("Log", "isBeingDrag...");
                    return true;
                }
                if (mIsUnableToDrag) {
                    //Log.i("Log", "mIsUnableToDrag...");
                    return false;
                }
                int index = getPointerIndex(ev, mPointerId);
                float x = MotionEventCompat.getX(ev, index);
                float diffX = x - mLastMotionX;
                float mDiffX = Math.abs(diffX);
                float y = MotionEventCompat.getY(ev, index);
                float diffY = y - mLastMotionY;
                float mDiffY = Math.abs(diffY);
                int intDiffy = (int) diffY;

                if (diffY != 0 && (canScroll(intDiffy) || canScroll(this, false, intDiffy, (int) x, (int) y) )) {
                    //注意canScroll只兼容4.0以上的滑动冲突,google工程师有明确标出,如果需要兼容4.0以下版本
                    //可以用我写好的ScrollLayoutCompat类,判断如果4.0以下版本时,用isChildCanScroll判断是否能滑动
                    mLastMotionX = x;
                    mLastMotionY = y;
                    mIsUnableToDrag = true;
                    return false;
                }

                if (mDiffX > mTouchSlop && mDiffX * 0.5f > mDiffY) {
                    //没办法,ViewPager要这样写,也许是x轴滑动优先要比Y轴滑动优先低些吧,但是为了兼容ViwPager不得不跟着这么写
                    mIsUnableToDrag = true;
                } else if (mDiffY > mTouchSlop) {
                    mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    mLastMotionY = diffY > 0
                            ? mInitialMotionY + mTouchSlop : mInitialMotionY - mTouchSlop;
                    mLastMotionX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //Log.i("you", "onInterceptTouchEvent  up or cancel...");
                mPointerId = INVALID_POINTER;
                releaseVelocityTracker();
                scrollToEdage(getScrollY());
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mPointerId = MotionEventCompat.getPointerId(ev, actionIndex);
                mLastMotionX = MotionEventCompat.getX(ev, actionIndex);
                mLastMotionY = MotionEventCompat.getY(ev, actionIndex);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mIsBeingDragged;
    }

    /**
     * 子控件是否能够滑动,递归查询
     * @param v
     * @param checkV
     * @param dy
     * @param x
     * @param y
     * @return
     */
    protected boolean canScroll(View v, boolean checkV, int dy, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                // 这里只能兼容到4.0或以上版本,fucking compat,如果要兼容4.0以下滑动,请参考写好的ScrollLayoutCompat类
                //根据如果小于４.0版本用ScrollLayoutCompat.canScroll方法
                //  fucking compat
                //  fucking compat
                // This will not work for transformed views in Honeycomb+
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                        && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()
                        && canScroll(child, true, dy, x + scrollX - child.getLeft(),
                        y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }
        return checkV && ViewCompat.canScrollVertically(v, -dy);
    }

    /**
     * 在初始化与展开的时候,要考虑两个情况子控件优先滑动, 也可以根据实际需求去掉此功能
     * (如果不加此逻辑,初始状态时,下滑子控件不能滑动再上滑会导致第一个界面没有展开就自身滑动了)
     * @param dy
     * @return
     */
    protected boolean canScroll(int dy) {
        if (scrollHeight == 0) return false;
        if (getScrollY() == 0) {
            return dy > 0;
        }
        if (getScrollY() == scrollHeight) {
            return dy < 0;
        }
        return false;
    }

    /**
     * 请求上层放行事件
     * @param disallowIntercept
     */
    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        //Log.i("you", "requestDisallowInterceptTouchEvent");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        obtainVelocityTracker(event);
        int actionIndex = MotionEventCompat.getActionIndex(event);
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mScroller.abortAnimation();
                mPointerId = MotionEventCompat.getPointerId(event, 0);
                mLastMotionX = mInitialMotionX = event.getX();
                mLastMotionY = mInitialMotionY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int index = getPointerIndex(event, mPointerId);
                float x = MotionEventCompat.getX(event, index);
                float y = MotionEventCompat.getY(event, index);
                float diffY = y - mLastMotionY;
                float mDiffY = Math.abs(diffY);
                if (!mIsBeingDragged) {//如果滑动尚未产生
                    if(mDiffY > mTouchSlop) {
                        mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        mLastMotionY = diffY > 0
                                ? mInitialMotionY + mTouchSlop : mInitialMotionY - mTouchSlop;
                        mLastMotionX = x;
                    }
                    ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (mIsBeingDragged) {
                    int intDiffy = (int) diffY;
                    if (performDragY(intDiffy)) {
                        scrollBy(0, -intDiffy);
                    }
                    mLastMotionY = y;
                    mLastMotionX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.computeCurrentVelocity(1000, mMaxVelocity);
                float velocityY = mTracker.getYVelocity();
                scrollByVelocityY(velocityY);
                releaseVelocityTracker();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mLastMotionY = MotionEventCompat.getY(event, actionIndex);
                mPointerId = MotionEventCompat.getPointerId(event, actionIndex);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                mLastMotionY = MotionEventCompat.getY(event, getPointerIndex(event, mPointerId));
                break;
        }
        return true;
    }

    /**
     * 获取手指触控位置
     */
    private int getPointerIndex(MotionEvent event, int pointerId) {
        int index = MotionEventCompat.findPointerIndex(event, pointerId);
        if (index != -1)
            return index;
        return 0;
    }

    /**
     * @param e
     */
    private void onSecondaryPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (MotionEventCompat.getPointerId(e, actionIndex) == mPointerId) {
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mPointerId = MotionEventCompat.getPointerId(e, newIndex);
            mLastMotionX = MotionEventCompat.getX(e, newIndex);
            mLastMotionY = MotionEventCompat.getY(e, newIndex);
        }
    }

    /**
     * 根据加速度来判断是否滑动,往哪个方向滑动
     */
    private void scrollByVelocityY(float velocityY) {
        int scrollY = getScrollY();
        if (Math.abs(velocityY) > mMinVelocity) {
            if (velocityY > 0) {  //向下的加速滑动
                if (scrollY > 0) { //必须能够滑动才可以滑
                    mScroller.startScroll(0, scrollY, 0, -scrollY, computeScrollDuration(scrollY, velocityY));
                    invalidate();
                }
            } else {  //向上的加速滑动
                if (scrollY < scrollHeight) {
                    mScroller.startScroll(0, scrollY, 0, scrollHeight - scrollY, computeScrollDuration(scrollHeight - scrollY, velocityY));
                    invalidate();
                }
            }
        } else {  //速度不够
            scrollToEdage(scrollY);
        }
    }

    /**
     * 如果有滑动偏移,让它回到原处
     */
    private void scrollToEdage(int scrollY) {
        if (scrollY == 0 || scrollY == scrollHeight)
            return;
        if (scrollY >= scrollHeight / 2) {
            mScroller.startScroll(0, scrollY, 0, scrollHeight - scrollY, computeScrollDuration(scrollHeight - scrollY, 0));
        } else {
            mScroller.startScroll(0, scrollY, 0, -scrollY, computeScrollDuration(scrollY, 0));
        }
        invalidate();
    }

    /**
     * 根据加速度与需要滑动的偏移计算滑动时间
     * @param dy
     * @param velocityY
     * @return
     */
    private int computeScrollDuration(int dy, float velocityY) {
        final int height = getHeight();
        final int halfHeight = height / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dy) / height);
        final float distance = halfHeight + halfHeight * distanceInfluenceForSnapDuration(distanceRatio);
        velocityY = Math.abs(velocityY);
        int duration;
        if (velocityY > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocityY));
        } else {
            final float pageDelta = (float) Math.abs(dy) / height;
            duration = (int) ((pageDelta + 1) * 200);
        }
        duration = Math.min(duration, MAX_DURATION);
        //Log.i("you", "duration "+duration);
        return duration;
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    /**
     * 根据移动的距离判断是否ViewGroup中的内容能否移动
     */
    private boolean performDragY(int diffY) {
        int scrollY = getScrollY();
        if (diffY > 0) {  //手指向下移动
            if (scrollY <= 0)  //向下移动时若没有偏移量则无法移动
                return false;
            else if (diffY - scrollY > 0) { //当快速滑动时,下滑的距离大于偏移量时
                scrollTo(0, 0); //回到原始位置
                return false;
            }
        } else {  //手指向上移动
            if (scrollY >= scrollHeight) //滑动到顶端了,不能再滑动
                return false;
            else if (scrollY - diffY > scrollHeight) {  //快速滑动时,上滑的距离大于最大偏移量时
                scrollTo(0, scrollHeight);
                return false;
            }
        }
        return true;
    }

    /**
     * 设置速度追踪器事件
     */
    private void obtainVelocityTracker(MotionEvent event) {
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        }
        mTracker.addMovement(event);
    }

    /**
     * 释放加速器类
     */
    private void releaseVelocityTracker() {
        if (mTracker != null) {
            mTracker.recycle();
            mTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {  //根据scroller的位置来滑动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    /**
     * 是否展开状态
     */
    public boolean isClosed() {
        return this.getScrollY() > 0;
    }

    public int getScrollHeight() {
        return scrollHeight;
    }

    /**
     * 展开
     */
    public void open() {
        int scrollY = getScrollY();
        if (scrollY == 0)
            return;
        mScroller.startScroll(0, scrollY, 0, -scrollY, computeScrollDuration(scrollY, 0));
        invalidate();
    }

    /**
     * 高度有变化时需要调整大小
     */
    private void scrollLayoutChanged() {
        if (isClosed()) {
            scrollTo(0, scrollHeight);
        }
    }

    /**
     * 闭合
     */
    public void close() {
        int scrollY = getScrollY();
        if (scrollY == scrollHeight)
            return;
        mScroller.startScroll(0, scrollY, 0, scrollHeight - scrollY, computeScrollDuration(scrollHeight - scrollY, 0));
        invalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener != null) {
            mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    private OnScrollChangeListener mOnScrollChangeListener;

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mOnScrollChangeListener = listener;
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v The view whose scroll position has changed.
         * @param scrollX Current horizontal scroll origin.
         * @param scrollY Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(ScrollLayout v, int scrollX, int scrollY,
                            int oldScrollX, int oldScrollY);
    }

}
