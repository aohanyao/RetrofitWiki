package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.biz.GitHubService;
import com.orhanobut.logger.Logger;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.GET;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * 动态BaseUrl
 */
public class DynamicBaseUrlActivity extends BaseActivity {

    /**
     * 拦截器
     */
    private HostSelectionInterceptor hostSelectionInterceptor = new HostSelectionInterceptor();

    private HostSelectionHandler mHostSelectionHandler;

    public interface Pop {
        @GET("robots.txt")
        retrofit2.Call<ResponseBody> robots();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();
        mHostSelectionHandler = new HostSelectionHandler();


        mDescTv.setText("本例子说明：\n" +
                "通过增加自定义 Interceptor 的方式，来完成对 BaseUrl的动态替换。");
    }


    @Override
    protected void sendRequest() {


        // 为了演示才这么写在 主线程中的，实际情况下是不可能
        // 直接写在主线程中的
        mResultTv.setText("创建请求:https://api.github.com/\n");


        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {

                    Pop pop = retrofit.create(Pop.class);
                    sendMessage("开始请求................\n");
                    retrofit2.Response<ResponseBody> response1 = pop.robots().execute();
                    sendMessage("Response from: \n" + response1.raw().request().url());
                    sendMessage("\n返回结果：\n" + response1.body().string());


                    sendMessage("\n\n替换url:www.baidu.com\n");
                    hostSelectionInterceptor.setHost("www.baidu.com");
                    sendMessage("开始请求................\n");
                    retrofit2.Response<ResponseBody> response2 = pop.robots().execute();
                    sendMessage("Response from: " + response2.raw().request().url());
                    sendMessage("\n返回结果：\n" + response2.body().string());
                } catch (IOException e) {
                    sendMessage("发生错误：" + e.getMessage());
                    e.printStackTrace();
                }
            }

            private void sendMessage(String msg) {
                Logger.e(msg);
                Message message = mHostSelectionHandler.obtainMessage();
                message.obj = msg;
                mHostSelectionHandler.sendMessage(message);
            }

        });


    }

    private void initRetrofitAndService() {
        retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder()
                        .addInterceptor(getHttpLoggingInterceptor())
                        .addInterceptor(hostSelectionInterceptor)//加入自定义的拦截器
                        .build())
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }


    final class HostSelectionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mResultTv.append((String) msg.obj);
        }
    }

    /**
     * 自定义的拦截器，实现baseUrl的动态替换
     */
    static final class HostSelectionInterceptor implements Interceptor {
        // 这里主要是做线程懂不，保证host的值是唯一的
        // 具体请看：https://zh.wikipedia.org/wiki/Volatile%E5%8F%98%E9%87%8F
        private volatile String host;

        public void setHost(String host) {
            this.host = host;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String host = this.host;
            if (host != null) {
                HttpUrl newUrl = request.url().newBuilder()
                        .host(host)
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
            }
            return chain.proceed(request);
        }
    }

}
