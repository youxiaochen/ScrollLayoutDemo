package you.xiaochen.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import you.xiaochen.R;

/**
 * Created by you on 2017/5/4.
 */

public class GoodsFragment extends Fragment {

    public static GoodsFragment newInstance() {
        return new GoodsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goods, container, false);
    }
}
