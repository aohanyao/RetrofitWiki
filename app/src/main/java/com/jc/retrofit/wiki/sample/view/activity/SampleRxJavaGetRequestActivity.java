package com.jc.retrofit.wiki.sample.view.activity;

import android.os.Bundle;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.Repo;
import com.jc.retrofit.wiki.biz.GitHubService;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class SampleRxJavaGetRequestActivity extends BaseActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRetrofitAndService();


        mDescTv.setText("本例子说明：\n" +
                "使用RxJava配合请求\n" +
                "增加RxJava2CallAdapterFactory做数据适配，主要作用是将数据适配器RxJava2需要的格式\n" +
                "增加RxAndroid做线程切换：\n" +
                "1、subscribeOn(Schedulers.io())：运行在io线程；\n" +
                "2、observeOn(AndroidSchedulers.mainThread())：返回结果切换到主线程");
    }


    @Override
    protected void sendRequest() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }

        mResultTv.setText("创建请求................\n");
        service.listRxJava2Repos("aohanyao", "owner")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Repo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        mResultTv.append("开始请求................\n");
                    }

                    @Override
                    public void onNext(List<Repo> repos) {
                        for (Repo repo : repos) {
                            mResultTv.append("repoName:" + repo.getName() + "    star:" + repo.getStargazers_count() + "\n");
                        }
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
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
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
