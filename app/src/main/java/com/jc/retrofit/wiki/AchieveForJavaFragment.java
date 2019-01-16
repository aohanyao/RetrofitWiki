package com.jc.retrofit.wiki;

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

        for (int i = 0; i < 16; i++) {
            datas.add(new TargetDummyItem(String.valueOf(i),String.valueOf(i),""));
        }

        return datas;
    }

    public AchieveForJavaFragment() {
    }
}
