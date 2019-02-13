package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import com.jc.retrofit.wiki.advanced.sample.view.convert.CustGsonConverterFactory;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.Repo;
import com.jc.retrofit.wiki.biz.GitHubService;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 多种数据格式处理
 */
public class MultipleResponseActivity extends BaseActivity {

    private Disposable disposable;
    Scheduler observeOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observeOn = Schedulers.computation();
        initRetrofitAndService();


        mDescTv.setText("本例子说明：\n" +
                "应对在APP中请求接口有多种数据格式返回，需要做适配。还有就是需要对错误码进行统一的预处理。\n" +
                "主要有两种方式：\n" +
                "①直接复制converter-gson的源码，在GsonResponseBodyConverter.convert()方法中做处理。\n" +
                "②自行实现Converter.Factory的相关方法，源码中就是这么做的。\n" +
                "在本实例中，是直接复制了源码，在GsonResponseBodyConverter中进行了操作.");
    }


    @Override
    protected void sendRequest() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        mResultTv.setText("创建请求................\n");
        service.singleRxJava2Repos("aohanyao", "owner")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Repo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        mResultTv.append("开始请求................\n");
                    }

                    @Override
                    public void onNext(Repo repo) {
                        mResultTv.append("repoName:" + repo.getName() + "    star:" + repo.getStargazers_count() + "\n");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mResultTv.append("请求失败：" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        mResultTv.append("请求结束................\n");
                    }
                });


    }

    private void initRetrofitAndService() {
        retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder()
                        .addInterceptor(getHttpLoggingInterceptor())
                        .build())
                .baseUrl("https://api.github.com/")
                .addConverterFactory(CustGsonConverterFactory.create()) // 这里使用的是用自己智自定义的转换器
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

}
