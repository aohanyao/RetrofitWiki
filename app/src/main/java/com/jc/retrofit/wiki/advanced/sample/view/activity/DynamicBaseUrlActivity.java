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
 * 动态Url/Path/Parameter/Header
 */
public class DynamicBaseUrlActivity extends BaseActivity {

    /**
     * 拦截器
     */
    private HostSelectionInterceptor hostSelectionInterceptor = new HostSelectionInterceptor();

    private HostSelectionHandler mHostSelectionHandler;

    public interface RobotsService {
        // 这里为了测试，所以加了一个前缀 test，要做的就是在拦截器中去除这个前缀
        @GET("test/robots.txt")
        retrofit2.Call<ResponseBody> robots();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRetrofitAndService();
        mHostSelectionHandler = new HostSelectionHandler();


        mDescTv.setText("本例子说明：\n" +
                "通过增加自定义 Interceptor 的方式。\n" +
                "完成对 动态Url/Path/Parameter/Header 的动态替换和新增。\n");
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

                    RobotsService robots = retrofit.create(RobotsService.class);
                    sendMessage("开始请求................\n");
                    retrofit2.Response<ResponseBody> response = robots.robots().execute();
                    sendMessage("Response from: \n" + response.raw().request().url());
                    sendMessage("\n返回结果：\n" + response.body().string());

                } catch (IOException e) {
                    sendMessage("发生错误：" + e.getMessage());
                    e.printStackTrace();
                }
            }


        });


    }

    private void sendMessage(String msg) {
        Logger.e(msg);
        Message message = mHostSelectionHandler.obtainMessage();
        message.obj = msg;
        mHostSelectionHandler.sendMessage(message);
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


    /**
     * 自定义的拦截器
     * 1. 实现baseUrl的动态替换
     * 2. path的替换
     * 3. 增加parameter
     * 3. 增加header
     */
    final class HostSelectionInterceptor implements Interceptor {


        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            // 拿到请求
            Request request = chain.request();

            HttpUrl httpUrl = request.url();
            HttpUrl.Builder newUrlBuilder = httpUrl.newBuilder();

            // 替换host
            String host = httpUrl.host();
            // 这里是判断 当然真实情况不会这么简单
            if (httpUrl.host().equals("api.github.com")) {
                // 只是为了在demo中显示消息提示
                sendMessage("\n\n替换url:www.baidu.com\n");
                host = "www.baidu.com";
            }
            // 重新设置新的host
            newUrlBuilder.host(host);


            // 替换path
            //List<String> pathSegments = httpUrl.pathSegments();
            // 这里是我已经知道了我是要移除第一个路径，所以我直接就移除了
            // 真实项目中，判断条件更加复杂
            newUrlBuilder.removePathSegment(0);
            // 将index的segment替换为传入的值
            //newUrlBuilder.setPathSegment(index,segment);

            // 添加参数
            newUrlBuilder.addQueryParameter("version", "v1.3.1");

            // 创建新的请求
            request = request.newBuilder()
                    .url(newUrlBuilder.build())
                    .header("NewHeader", "NewHeaderValue")
                    .build();
            // 只是为了在demo中显示消息提示
            sendMessage("\n\n新请求地址和参数：" + request.url().toString() + "\n\n");
            return chain.proceed(request);
        }
    }


    final class HostSelectionHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mResultTv.append((String) msg.obj);
        }
    }

}
