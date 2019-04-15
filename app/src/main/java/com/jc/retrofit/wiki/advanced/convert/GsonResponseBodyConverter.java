/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jc.retrofit.wiki.advanced.convert;

import android.util.Log;
import com.google.gson.TypeAdapter;
import com.jc.retrofit.wiki.advanced.error.exception.NetErrorException;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;

    /**
     * 模拟的假数据
     */
    private final List<String> mockResult;

    private final Random random;

    GsonResponseBodyConverter(TypeAdapter<T> adapter) {
        this.random = new Random();
        this.adapter = adapter;
        mockResult = new ArrayList<>();
        mockResult.add("{\"code\":200,\"message\":\"成功，但是没有数据\",\"data\":[]}");
        mockResult.add("{\"code\":-1,\"message\":\"这里是接口返回的：错误的信息，抛出错误信息提示！\",\"data\":[]}");
        mockResult.add("{\"code\":401,\"message\":\"这里是接口返回的：权限不足，请重新登录！\",\"data\":[]}");
        mockResult.add("{\"code\": 200,\"data\": {\"id\": \"1\",\"name\": \"数据格式 1\",\"stargazers_count\": 1}}");
        mockResult.add(
                "{\n\"status\": 200,\"msg\": \"请求成功\",\"userInfo\": {\"id\": \"2\",\"name\": \"数据格式 2\",\"stargazers_count\": 2}}");
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        // 这里就是对返回结果进行处理
        // 其实我根本就没使用真正返回的结果，都是用的自定义的结果
        String jsonString = value.string();
        try {
            value.string();
            Log.e("TAG", "这里进行了返回结果的判断");

            JSONObject jsonObject = new JSONObject(jsonString);

            try {
                // 如果这里能取出数据，而且没有问题，那就代表这是 code data msg 数据格式的
                int code = jsonObject.getInt("code");
                if (code != 200) {
                    throw new NetErrorException(jsonObject.getString("message"), code);
                }
                try {
                    return adapter.fromJson(jsonObject.getString("data"));
                } catch (Exception e) {
                    throw new NetErrorException("数据解析异常", NetErrorException.PARSE_ERROR);
                }
            } catch (JSONException ignored) {

            }

            try {
                // 如果这里能取出数据，而且没有问题，那就代表这是 code data msg 数据格式的
                int status = jsonObject.getInt("status");
                if (status != 200) {
                    throw new NetErrorException(jsonObject.getString("msg"), status);
                }
                return adapter.fromJson(jsonObject.getString("userInfo"));
            } catch (JSONException e) {
                throw new NetErrorException("数据解析异常", NetErrorException.PARSE_ERROR);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            throw new NetErrorException("数据解析异常", NetErrorException.PARSE_ERROR);
        } finally {
            value.close();
        }
    }
}
