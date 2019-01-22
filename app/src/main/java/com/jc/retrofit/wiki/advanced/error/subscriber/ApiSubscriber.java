package com.jc.retrofit.wiki.advanced.error.subscriber;

import com.google.gson.JsonParseException;
import com.jc.retrofit.wiki.advanced.error.exception.NetErrorException;
import com.jc.retrofit.wiki.advanced.error.inf.HandlerBaseView;
import io.reactivex.subscribers.ResourceSubscriber;
import org.json.JSONException;
import org.reactivestreams.Subscriber;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by 江俊超 on 2019/1/22.
 * Version:1.0
 * Description: RxJava 统一订阅
 * ChangeLog:
 */
public abstract class ApiSubscriber<T> extends ResourceSubscriber<T> {

    private HandlerBaseView v;

    private ApiSubscriber() {
    }

    /**
     * 使用这个构造函数时候 将错误提示交给 v来处理
     *
     * @param v
     */
    public ApiSubscriber(HandlerBaseView v) {
        this.v = v;
    }


    public ApiSubscriber(Subscriber<?> subscriber, HandlerBaseView v) {
        this.v = v;
    }


    @Override
    public void onError(Throwable e) {
        NetErrorException error = null;
        if (e != null) {


            if (!(e instanceof NetErrorException)) {
                if (e instanceof UnknownHostException) {
                    error = new NetErrorException(e, NetErrorException.NoConnectError);
                } else if (e instanceof JSONException || e instanceof JsonParseException) {
                    error = new NetErrorException(e, NetErrorException.PARSE_ERROR);
                } else if (e instanceof SocketTimeoutException) {
                    error = new NetErrorException(e, NetErrorException.SocketTimeoutError);
                } else if (e instanceof ConnectException) {
                    error = new NetErrorException(e, NetErrorException.ConnectExceptionError);
                } else {
                    error = new NetErrorException(e, NetErrorException.OTHER);
                }
            } else {
                error = new NetErrorException(e.getMessage(), NetErrorException.OTHER);
            }
        }


        if (v != null) {
            v.onFail(error);
        }

    }

    @Override
    public void onComplete() {

    }
}
