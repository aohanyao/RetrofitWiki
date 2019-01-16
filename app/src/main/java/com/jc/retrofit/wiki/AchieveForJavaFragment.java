package com.jc.retrofit.wiki;

import com.jc.retrofit.wiki.annotation.SampleRequestMainActivity;
import com.jc.retrofit.wiki.base.BaseListFragment;
import com.jc.retrofit.wiki.bean.TargetDummyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 江俊超 on 2019/1/16.
 * Version:1.0
 * Description:
 * ChangeLog:
 */
public class AchieveForJavaFragment extends BaseListFragment {
    @Override
    protected List<TargetDummyItem> initTargets() {
        List<TargetDummyItem> datas = new ArrayList<>();

        datas.add(new TargetDummyItem("简单使用",
                "包含所有的请求方式的简单使用",
                SampleRequestMainActivity.class));

        return datas;
    }

    public AchieveForJavaFragment() {
    }
}
