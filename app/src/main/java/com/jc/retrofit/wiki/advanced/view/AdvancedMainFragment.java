package com.jc.retrofit.wiki.advanced.view;

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
public class AdvancedMainFragment extends BaseListFragment {
    @Override
    protected List<TargetDummyItem> initTargets() {
        List<TargetDummyItem> datas = new ArrayList<>();

//        datas.add(new TargetDummyItem("Sample GET  ",
//                "原生的@GET请求，包含@Path和@Query，使用GsonConverterFactory进行序列化",
//                SampleGetRequestActivity.class));


        return datas;
    }

    public AdvancedMainFragment() {
    }
}
