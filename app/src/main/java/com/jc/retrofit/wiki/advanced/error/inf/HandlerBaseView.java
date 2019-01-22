package com.jc.retrofit.wiki.advanced.error.inf;

import com.jc.retrofit.wiki.advanced.error.exception.NetErrorException;

/**
 * Created by 江俊超 on 2019/1/22.
 * Version:1.0
 * Description:
 * ChangeLog: 处理返回结果的信息
 */
public interface HandlerBaseView {
    /**
     * 发生错误
     *
     * @param error
     */
    void onFail(NetErrorException error);

    /**
     * 完成
     *
     * @param msg
     */
    void complete(String msg);

}
