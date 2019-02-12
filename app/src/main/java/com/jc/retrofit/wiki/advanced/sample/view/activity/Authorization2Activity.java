package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.AuthorizationBean;
import com.jc.retrofit.wiki.biz.GitHubService;
import com.orhanobut.logger.Logger;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * token重试机制2，使用拦截器的机制
 */
public class Authorization2Activity extends BaseActivity {

    private Disposable disposable;

    private AuthenticatorHandler mAuthenticatorHandler;


    private Interceptor mAuthenticatorInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            // 获取请求
            Request request = chain.request();
            // 获取响应
            Response response = chain.proceed(request);
            // 在这里判断是不是是token失效
            // 当然，判断条件不会这么简单，会有更多的判断条件的
            if (response.code() == 401) {
                //我这里为了显示错误信息，所以填充到TextView上，真实项目肯定不是这样的。

                sendMessage("得到错误：" + response.message() + " -> " + response.code() + " \n");
                sendMessage("准备模拟发起请求刷新token...\n");
                //这里应该调用自己的刷新token的接口
//                String token = refreshToken();
                String token = Credentials.basic("userName", "password", Charset.forName("UTF-8"));
                // 这里发起的请求是同步的，刷新完成token后再增加到header中
                // 这里抛出的错误会直接回调 onError
                sendMessage("刷新token成功...\n");
                sendMessage("重新调起上一次请求，并增加 Authorization\n");

                Request retryRequest = chain.request()
                        .newBuilder()
                        .header("Authorization", token)
                        .build();

                // 再次发起请求
                return chain.proceed(retryRequest);
            }

            return response;
        }

        //   这里只是为了回调消息
        private void sendMessage(String msg) {
            Logger.e(msg);
            Message message = mAuthenticatorHandler.obtainMessage();
            message.obj = msg;
            mAuthenticatorHandler.sendMessage(message);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();
        mAuthenticatorHandler = new AuthenticatorHandler();

        mDescTv.setText("本例子说明：\n" +
                "通过 Interceptor 实现对 401、token 静默刷新。用这种方式的话，重试只会调用一次接口。");
    }


    @Override
    protected void sendRequest() {


        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        mResultTv.setText("创建没有增加Authorization的请求................\n");
        service.listAuthorizations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AuthorizationBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        mResultTv.append("开始请求................\n");
                    }

                    @Override
                    public void onNext(List<AuthorizationBean> repos) {
                        mResultTv.append(repos.size() + "");
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
                        .addInterceptor(mAuthenticatorInterceptor)// 重试拦截器
                        .build())
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);
    }

    /**
     * 只是为了在主线程回调消息而已
     */
    private final class AuthenticatorHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mResultTv.append((String) msg.obj);
        }
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
