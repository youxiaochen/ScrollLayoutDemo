package you.xiaochen.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by you on 2017/5/4.
 */

public class TabAdapter extends FragmentPagerAdapter {
    static final String[] titles = {"图文详情", "商品参数", "热卖推荐"};
    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return RecommendFragment.newInstance();
            case 1:
                return GoodsFragment.newInstance();
            case 2:
                return RecommendFragment.newInstance();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
