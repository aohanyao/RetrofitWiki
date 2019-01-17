package com.jc.retrofit.wiki.sample.view.fragment;

import com.jc.retrofit.wiki.base.BaseListFragment;
import com.jc.retrofit.wiki.bean.TargetDummyItem;
import com.jc.retrofit.wiki.sample.view.activity.SampleGetRequestActivity;
import com.jc.retrofit.wiki.sample.view.activity.SampleRxJavaGetRequestActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 江俊超 on 2019/1/16.
 * Version:1.0
 * Description:
 * ChangeLog:
 */
public class SampleRequestMainFragment extends BaseListFragment {
    @Override
    protected List<TargetDummyItem> initTargets() {
        List<TargetDummyItem> datas = new ArrayList<>();

        datas.add(new TargetDummyItem("Sample GET  ",
                "原生的@GET请求，包含@Path和@Query，使用GsonConverterFactory进行序列化",
                SampleGetRequestActivity.class));
        datas.add(new TargetDummyItem("Sample RxJava2 GET  ",
                "使用RxJava2进行配合请求，RxAndroid做线程切换,RxJava2CallAdapterFactory做数据适配",
                SampleRxJavaGetRequestActivity.class));

        return datas;
    }

    public SampleRequestMainFragment() {
    }
}
