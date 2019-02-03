package com.jc.retrofit.wiki.advanced.sample.view.activity;

import android.os.Bundle;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import com.jc.retrofit.wiki.annotation.JsonParam;
import com.jc.retrofit.wiki.base.BaseActivity;
import com.jc.retrofit.wiki.bean.AuthorizationBean;
import com.jc.retrofit.wiki.bean.JsonQueryParametersBean;
import com.jc.retrofit.wiki.biz.GitHubService;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 自定义序列化，实现get请求提交json格式的参数。
 */
public class JsonQueryParametersActivity extends BaseActivity {

    private Disposable disposable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();

        mDescTv.setText("本例子说明：\n" +
                "通过 Converter.Factory 实现对象序列化为Json字符串，通过GET提交。");
    }


    @Override
    protected void sendRequest() {


        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        JsonQueryParametersBean parametersBean = new JsonQueryParametersBean("35");
        mResultTv.setText("创建请求................\n");
        service.exampleJsonParam(parametersBean)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AuthorizationBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        mResultTv.append("开始请求................\n");
                    }

                    @Override
                    public void onNext(AuthorizationBean repos) {
//                        mResultTv.append(repos.size() + "");
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
                .addConverterFactory(new JsonStringConverterFactory())
                .build();

        service = retrofit.create(GitHubService.class);
    }


    class JsonStringConverterFactory extends Converter.Factory {
        private Gson gson;

        /*public 使用的内部类*/ JsonStringConverterFactory() {
            this.gson = new Gson();
        }

        @Override
        public @Nullable
        Converter<?, String> stringConverter(
                Type type, Annotation[] annotations, Retrofit retrofit) {
            // 遍历所有的注解
            for (Annotation annotation : annotations) {
                // 判断是我们自己的注解，使用我们自己的方式来进行序列化
                if (annotation instanceof JsonParam) {
                    // type adapter
                    TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
                    return new JsonStringConverter<>(gson, adapter);
                }
            }
            return null;
        }

        class JsonStringConverter<T> implements Converter<T, String> {

            private final Gson gson;
            private final TypeAdapter<T> adapter;

            JsonStringConverter(Gson gson, TypeAdapter<T> adapter) {
                this.gson = gson;
                this.adapter = adapter;
            }

            @Override
            public String convert(T value) throws IOException {
                // 通过Gson将对象序列化为json字符串

                Buffer buffer = new Buffer();
                Writer writer = new OutputStreamWriter(buffer.outputStream(), Util.UTF_8);
                JsonWriter jsonWriter = gson.newJsonWriter(writer);
                adapter.write(jsonWriter, value);
                jsonWriter.close();
                return buffer.readUtf8();
            }
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
