package com.jc.retrofit.wiki.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jc.retrofit.wiki.R;
import com.jc.retrofit.wiki.bean.TargetDummyItem;

import java.util.List;

/**
 * 基类fragment 不重要
 * <p/>
 * interface.
 */
public abstract class BaseListFragment extends Fragment {


    protected List<TargetDummyItem> mTargets = initTargets();

    protected abstract List<TargetDummyItem> initTargets();

    public BaseListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_targat_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new TargetBeanRecyclerViewAdapter(getActivity(), mTargets));
        }
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


}
