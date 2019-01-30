package com.jc.retrofit.wiki.advanced.sample.view.fragment;

import com.jc.retrofit.wiki.advanced.error.view.HandlerResponseErrorActivity;
import com.jc.retrofit.wiki.advanced.sample.view.activity.*;
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

        datas.add(new TargetDummyItem("RxJavaObserveOnMainThread",
                "不使用RxAndroid做线程切换，自己通过集成CallAdapter.Factory实现线程切换",
                RxJavaObserveOnMainThreadActivity.class));
        datas.add(new TargetDummyItem("多返回数据格式处理及code提前处理",
                "应对在APP中请求接口有多种数据格式返回，需要做适配。还有就是需要对错误码进行统一的预处理。",
                MultipleResponseActivity.class));
        datas.add(new TargetDummyItem("统一状态码/错误处理",
                "1.统一对返回结果中的状态码做统一的处理，开发者只关心拿到的data。\n2.对Exaction进行统一的抓取和处理。",
                HandlerResponseErrorActivity.class));
        datas.add(new TargetDummyItem("动态BaseUrl",
                "通过自定义 Interceptor 的方式来完成对 url 的动态替换",
                DynamicBaseUrlActivity.class));
        datas.add(new TargetDummyItem("Authenticator token 重试实现方法1",
                "使用okhttp提供的authenticator接口完成token实现的拦截和获取，会进行多次重试，直到达到最大重试次数。",
                Authenticator1Activity.class));
        datas.add(new TargetDummyItem("Authenticator token 重试实现方法2",
                "使用自定义 Interceptor 来实现，只会调用一次刷新接口，完全有开发者控制。",
                Authenticator2Activity.class));
        return datas;
    }

    public AdvancedMainFragment() {
    }
}
