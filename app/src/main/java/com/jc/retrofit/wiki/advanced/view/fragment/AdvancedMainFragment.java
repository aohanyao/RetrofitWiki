package com.jc.retrofit.wiki.advanced.view.fragment;

import com.jc.retrofit.wiki.advanced.view.activity.MultipleResponseActivity;
import com.jc.retrofit.wiki.base.BaseListFragment;
import com.jc.retrofit.wiki.bean.TargetDummyItem;
import com.jc.retrofit.wiki.advanced.view.activity.RxJavaObserveOnMainThreadActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 江俊超 on 2019/1/16.
 * Version:1.0
 * Description:
 * ChangeLog:
 */
public class AdvancedMainFragment extends BaseListFragment {
    @Override
    protected List<TargetDummyItem> initTargets() {
        List<TargetDummyItem> datas = new ArrayList<>();

        datas.add(new TargetDummyItem("RxJavaObserveOnMainThread",
                "不使用RxAndroid做线程切换，自己通过集成CallAdapter.Factory实现线程切换",
                RxJavaObserveOnMainThreadActivity.class));
        datas.add(new TargetDummyItem("多返回数据格式处理及code提前处理",
                "应对在APP中请求接口有多种数据格式返回，需要做适配。还有就是需要对错误码进行统一的预处理。",
                MultipleResponseActivity.class));
        return datas;
    }

    public AdvancedMainFragment() {
    }
}
