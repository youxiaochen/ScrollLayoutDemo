package you.xiaochen;

import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by you on 2016/1/5.
 * 为了把这逻辑写清楚,我也给大家写个Compat兼容4.0以下版本的兼容类吧
 */

public class ScrollLayoutCompat {

    /**
     * 4.0以上版本
     */
    public static boolean hasSANDWICH() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean canScroll(View v, boolean checkV, int diffY, int x, int y) {
        if (v instanceof RecyclerView) {
            return isRecyclerViewCanScroll((RecyclerView) v, diffY);
        }else if (v instanceof NestedScrollView) {
            return isNestedScrollViewCanScroll((NestedScrollView) v, diffY);
        } else if (v instanceof ScrollView) {
            return isScrollViewCanScroll((ScrollView) v, diffY);
        } else if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int count = group.getChildCount();
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (isChildViewOnTouch(child, x, y, scrollX, scrollY) && canScroll(child, true, diffY, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 当控件被触摸时判断它的子控件是否为触摸范围
     */
    private static boolean isChildViewOnTouch(View child, int x, int y, int scrollX, int scrollY) {
        return x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                y + scrollY >= child.getTop() && y + scrollY < child.getBottom();
    }

    /**
     * 判断ScrollView能否滑动
     */
    private static boolean isScrollViewCanScroll(ScrollView v, int dy) {
        if (v.getChildCount() <= 0) return false;
        int scrollY = v.getScrollY();
        if (dy > 0) {  //向上滑子控件优先
            return scrollY > 0;
        } else {
            return v.getChildAt(0).getHeight() - v.getHeight() > v.getScrollY();


            //return this.getScrollY() >= scrollHeight;
        }
    }

    /**
     * 判断ScrollView能否滑动
     */
    private static boolean isNestedScrollViewCanScroll(NestedScrollView v, int dy) {
        if (v.getChildCount() <= 0) return false;
        int scrollY = v.getScrollY();
        if (dy > 0) {  //向上滑子控件优先
            return scrollY > 0;
        } else {
            return v.getChildAt(0).getHeight() - v.getHeight() > v.getScrollY();


            //return this.getScrollY() >= scrollHeight;
        }
    }

    /**
     * 判断RecyclerView能否滑动,向上拖动时,RecyclerView默认就为优先滑动,只需处理向下滑动时
     */
    private static boolean isRecyclerViewCanScroll(RecyclerView rv, int dy) {
        if (rv.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
            if (lm.getChildCount() > 0 && lm.getOrientation() == LinearLayoutManager.VERTICAL) {  //垂直布局时才需要判断
                if (dy > 0) {  //向上滑子控件优先
                    /*if (this.getScrollY() != 0) {
                        View view = lm.findViewByPosition(0);
                        if (view == null)
                            return true;
                        return view.getTop() < 0;
                    }*/
                } else {//向下滑
                    //return this.getScrollY() >= scrollHeight;
                }
            }
        }
        return false;
    }

}
