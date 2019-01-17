package com.jc.retrofit.wiki.sample.view.activity;

import android.os.Bundle;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.Repo;
import com.jc.retrofit.wiki.biz.GitHubService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class SampleGetRequestActivity extends BaseActivity {

    private Call<List<Repo>> repos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRetrofitAndService();


        mDescTv.setText("原生实现的请求，没有加RxJava，只加上了GsonConverterFactory.create(),将Json字符串序列化为实体");
    }



    @Override
    protected void sendRequest() {
        if (repos != null && !repos.isCanceled() && repos.isExecuted()) {
            repos.cancel();
        }
        mResultTv.setText("创建请求................\n");
        repos = service.listRepos("aohanyao", "owner");
        mResultTv.append("开始请求................\n");

        repos.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                mResultTv.append("请求成功,仓库数量:\n" + response.body().size() + "\n");
                for (Repo repo : response.body()) {
                    mResultTv.append("repoName:" + repo.getName() + "    star:" + repo.getStargazers_count() + "\n");
                }

            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {
                mResultTv.append("请求失败：" + t.getLocalizedMessage());
                t.printStackTrace();
            }
        });
    }

    private void initRetrofitAndService() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }

}
