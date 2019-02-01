package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import com.jc.retrofit.wiki.annotation.JsonParam;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.AuthorizationBean;
import com.jc.retrofit.wiki.biz.GitHubService;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 自定义序列化，实现get请求提交json格式的参数。
 */
public class JsonQueryParametersActivity extends BaseActivity {

    private Disposable disposable;


    static class JsonStringConverterFactory extends Converter.Factory {
        private final Converter.Factory delegateFactory;

        JsonStringConverterFactory(Converter.Factory delegateFactory) {
            this.delegateFactory = delegateFactory;
        }

        @Override
        public @Nullable
        Converter<?, String> stringConverter(
                Type type, Annotation[] annotations, Retrofit retrofit) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof JsonParam) {
                    // NOTE: If you also have a JSON converter factory installed in addition to this factory,
                    // you can call retrofit.requestBodyConverter(type, annotations) instead of having a
                    // reference to it explicitly as a field.
                    Converter<?, RequestBody> delegate = delegateFactory.requestBodyConverter(type, annotations, new Annotation[0], retrofit);
                    return new DelegateToStringConverter<>(delegate);
                }
            }
            return null;
        }

        static class DelegateToStringConverter<T> implements Converter<T, String> {

            private final Converter<T, RequestBody> delegate;

            DelegateToStringConverter(Converter<T, RequestBody> delegate) {
                this.delegate = delegate;
            }

            @Override
            public String convert(T value) throws IOException {
                Buffer buffer = new Buffer();
                delegate.convert(value).writeTo(buffer);
                return buffer.readUtf8();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();

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
                        .build())
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
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
