package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.biz.GitHubService;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * 提交参数不进行URL编码
 */
@Deprecated
public class UrlEncodeActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();


        mDescTv.setText("本例子说明：\n" +
                "提交参数不进行URL编码\n" +
                "提交数据的时候，不进行URL编码，提交原始数据。例如：a=<b>默认情况下会编码成：a=%3cb%3e。往往" +
                "后端不会进行处理，所以需要我们在传递的时候就进行处理。\n\n" +
                "处理模式也很简单，@Field都提供了一个叫encoded的属性，默认是false。为false的时候，Retrofit解析会调用" +
                "encode的方式增加参数，为true则直接添加，不会进行一次编码。");
    }


    @Override
    protected void sendRequest() {


        mResultTv.setText("创建请求:https://api.github.com/\n");
        service.robotsByEncode("<a>")
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            mResultTv.append("返回结果：" + response.body().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        mResultTv.append("请求失败" + t.getMessage());
                    }
                });


    }


    private void initRetrofitAndService() {
        retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder()
                        .addInterceptor(getHttpLoggingInterceptor())
                        .build())
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }


}
