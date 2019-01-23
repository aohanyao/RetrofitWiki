package com.jc.retrofit.wiki.advanced.error.view;

import android.os.Bundle;
import com.jc.retrofit.wiki.advanced.error.convert.HandlerErrorGsonConverterFactory;
import com.jc.retrofit.wiki.advanced.error.exception.NetErrorException;
import com.jc.retrofit.wiki.advanced.error.inf.HandlerBaseView;
import com.jc.retrofit.wiki.advanced.error.subscriber.ApiSubscriber;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.Repo;
import com.jc.retrofit.wiki.biz.GitHubService;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;

/**
 * 统一处理错误
 */
public class HandlerResponseErrorActivity extends BaseActivity implements HandlerBaseView {

    private Disposable disposable;
    Scheduler observeOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observeOn = Schedulers.computation();
        initRetrofitAndService();


        mDescTv.setText("本例子说明：\n" +
                "1.统一对返回结果中的状态码做统一的处理，开发者只关心拿到的data。\n" +
                "2.对Exaction进行统一的抓取和处理。\n" +
                "实现思路：\n" +
                "① 创建自定义Exception类，所有错误统一处理\n" +
                "① 自定义BodyConverter，在这里对正常返回结果做第一次判断，抛出Exception\n" +
                "② 创建interface接口，专门用于回调错误\n" +
                "③ 自定义Subscriber，在onError中处理错误\n" +
                "④ View层实现interface接口，对结果进行处理，到底是弹窗还是toast等");
    }


    @Override
    protected void sendRequest() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        mResultTv.setText("创建请求................\n");
        disposable = service.listRxJava2FlowableRepos("aohanyao", "owner")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ApiSubscriber<List<Repo>>(this) {
                    @Override
                    public void onNext(List<Repo> repos) {
                        mResultTv.append("请求成功，repoCount:" + repos.size() + ":\n");
                        for (Repo repo : repos) {
                            mResultTv.append("repoName:" + repo.getName() + "    star:" + repo.getStargazers_count() + "\n");
                        }
                    }

                });


    }

    private void initRetrofitAndService() {
        retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder()
                        .addInterceptor(getHttpLoggingInterceptor())
                        .build())
                .baseUrl("https://api.github.com/")
                .addConverterFactory(HandlerErrorGsonConverterFactory.create()) // 这里使用的是用自己智自定义的转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            disposable.dispose();
        } catch (Exception e) {
        }
    }

    @Override
    public void onFail(NetErrorException error) {
        mResultTv.append("请求失败" + error.getMessage() + "................\n");
    }

    @Override
    public void complete(String msg) {
        mResultTv.append("请求成功................\n");
    }

}
