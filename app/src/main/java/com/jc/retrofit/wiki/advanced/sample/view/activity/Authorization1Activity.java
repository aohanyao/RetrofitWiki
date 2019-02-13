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
 * token重试机制1
 */
public class Authorization1Activity extends BaseActivity {

    private Disposable disposable;

    private AuthenticatorHandler mAuthenticatorHandler;


    private Authenticator authorization = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            // 我这里为了显示错误信息，所以填充到TextView上，真实项目肯定不是这样的。
            sendMessage("得到错误：" + response.message() + "，应该是：" + response.code() + " \n");
            sendMessage("准备模拟发起请求刷新token...\n");


            //-----------核心代码-------
            // ------------------- 这里应该调用自己的刷新token的接口
            // 这里抛出的错误会直接回调 onError
            // 这里发起的请求是同步的，刷新完成token后再增加到header中
            // String token = refreshToken();
            String token = Credentials.basic("userName", "password", Charset.forName("UTF-8"));
            //-----------核心代码-------


            sendMessage("刷新token成功...\n");
            sendMessage("重新调起上一次请求，并增加 Authorization\n");

            //-----------核心代码-------
            return response.request()
                    .newBuilder()
                    .header("Authorization", token)
                    .build();
            //-----------核心代码-------
        }
    };
    //   这里只是为了回调消息
    private void sendMessage(String msg) {
        Logger.e(msg);
        Message message = mAuthenticatorHandler.obtainMessage();
        message.obj = msg;
        mAuthenticatorHandler.sendMessage(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();
        mAuthenticatorHandler = new AuthenticatorHandler();

        mDescTv.setText("本例子说明：\n" +
                "通过 okhttp 提供的 authenticator 接口来实现对 401、token 静默刷新。使用这种方式有个问题就是会一直进行重试，\" +\n" +
                "\"直到重试次数大于设置的次数（默认为21次）才会断开连接。");
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
                        .authenticator(authorization)// 增加重试
                        .addInterceptor(getHttpLoggingInterceptor())
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
