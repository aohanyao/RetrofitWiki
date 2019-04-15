package com.jc.retrofit.wiki.advanced.error.subscriber;

import com.google.gson.JsonParseException;
import com.jc.retrofit.wiki.advanced.error.exception.NetErrorException;
import io.reactivex.subscribers.ResourceSubscriber;
import org.json.JSONException;

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

    @Override
    public void onError(Throwable e) {
        NetErrorException error = null;
        if (e != null) {
            // 对不是自定义抛出的错误进行解析
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

        // 回调抽象方法
        onFail(error);

    }

    /**
     * 回调错误
     */
    protected abstract void onFail(NetErrorException error);

    @Override
    public void onComplete() {

    }
}
