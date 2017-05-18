package you.xiaochen.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import you.xiaochen.R;

/**
 * Created by you on 2017/5/4.
 */

public class RecommendFragment extends Fragment {

    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;

    private RecommendAdapter adapter;

    public static RecommendFragment newInstance() {
        return new RecommendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recommend, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new RecommendAdapter();
        recyclerView.setAdapter(adapter);
    }

    private class RecommendAdapter extends RecyclerView.Adapter<RecommendHolder> {
        @Override
        public int getItemCount() {
            return 30;
        }

        @Override
        public RecommendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return new RecommendHolder(inflater.inflate(R.layout.item_fragment_recommend, parent, false));
        }

        @Override
        public void onBindViewHolder(RecommendHolder holder, int position) {

        }
    }

    private class RecommendHolder extends RecyclerView.ViewHolder {
        public RecommendHolder(View itemView) {
            super(itemView);
        }
    }

}
