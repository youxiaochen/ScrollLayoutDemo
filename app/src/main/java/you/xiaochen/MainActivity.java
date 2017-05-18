package you.xiaochen;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import you.xiaochen.fragment.TabAdapter;

public class MainActivity extends AppCompatActivity {
    /**
     * toolbar
     */
    private Toolbar mToolbar;
    /**
     * 滑动内容第一部分模块
     */
    private NestedScrollView sv_first;

    /**
     * 模块管理
     */

    private ArgbAnimator argb;
    /**
     * toolbar title, 滑动界面中图片预览图
     */
    private View tv_title, iv_banner, iv_float;
    /**
     * 核心滑动控件
     */
    private ScrollLayout sl_root;

    private TabLayout pager_tabs;
    private ViewPager vp_tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConfigUtils.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        initActionbar();
        initView();
        argb = new ArgbAnimator(getResources().getColor(R.color.trans), getResources().getColor(R.color.colorPrimary));
        sv_first.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                setToolbarBg(scrollY);
            }
        });
        sl_root.setOnScrollChangeListener(new ScrollLayout.OnScrollChangeListener() {
            @Override
            public void onScrollChange(ScrollLayout v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                iv_float.setVisibility(scrollY>= v.getScrollHeight() / 2 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initActionbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        View actionView = getLayoutInflater().inflate(R.layout.actionbar_main, null);
        tv_title = actionView.findViewById(R.id.tv_title);
        actionView.findViewById(R.id.iv_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //这里在返回键中模拟键盘弹出时的情景
                ViewGroup.LayoutParams params = sl_root.getLayoutParams();
                params.height = sl_root.getHeight() * 2 / 3;
                sl_root.setLayoutParams(params);
            }
        });
        ActionBar.LayoutParams actionBarParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        bar.setCustomView(actionView, actionBarParams);
    }

    private void initView() {
        sv_first = (NestedScrollView) findViewById(R.id.sv_first);
        sl_root = (ScrollLayout) findViewById(R.id.sl_root);
        iv_float = findViewById(R.id.iv_float);
        iv_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sl_root.open();
            }
        });
        iv_banner = findViewById(R.id.iv_banner);
        pager_tabs = (TabLayout) findViewById(R.id.pager_tabs);
        vp_tabs = (ViewPager) findViewById(R.id.vp_tabs);
        vp_tabs.setAdapter(new TabAdapter(getSupportFragmentManager()));
        pager_tabs.setupWithViewPager(vp_tabs);
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * 根据偏移值计算toolbar的颜色过渡值
     * @param scrollY
     */
    private int lastColor = Color.TRANSPARENT;

    private void setToolbarBg(int scrollY) {
        float fraction = (float) scrollY / iv_banner.getHeight();
        int color = argb.getFractionColor(fraction);
        if (color != lastColor) {
            lastColor = color;
            mToolbar.setBackgroundColor(color);
        }
        tv_title.setVisibility(fraction < 1 ? View.INVISIBLE : View.VISIBLE);
    }

}
