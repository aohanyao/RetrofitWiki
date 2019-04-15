---
title: RxJava2 + Retrofit2 完全指南 之 对返回Response的统一处理
tags: Android之路
grammar_cjkRuby: true
---

# 前言
本章在上篇[统一状态码/Exception处理](统一状态码和Exception处理.md)的基础上进行拓展，请现有一个必要的概念。

在接口对接中，我们最理想的状态就是后端只返回一种数据结构，而且是规范的数据结构，但事实上大多数情况下都不会按照我们的理想状态下返回的，比方说有数据的情况下，返回的是一种结构，无数据报错又是另外一种数据结构，这就不可避免的需要我们对每个请求进行判断，虽然可以通过和()(P)谐(Y)交(J)谈(Y)来让后端返回相同数据结构。但也存在另外一种情况：一个APP接入不同公司的API，这种情况就是无法避免的了。
对此，我们通过Retrofit可以在进行转换之前进行统一的数据结构判断和转换，只将数据data返回，只关注数据结果就行。

# 实现
## 分析
为了模拟相关数据结构，我在原本的基础上增加了两种种不同的数据，然后通过随机数获取到其中的一个数据当做返回结果来操作，两种数据格式如下：
- 数据格式 1

```json
{
	"code": 200,
	"data": {
		"id": "1",
		"name": "数据格式 1",
		"stargazers_count": 1
	}
}
```

- 数据格式 2

```json
{
	"status": 200,
	"msg": "请求成功",
	"userInfo": {
		"id": "2",
		"name": "数据格式 2",
		"stargazers_count": 2
	}
}
```

分析两种数据，`data`和`userInfo`中的字段几乎一致，当然现实基本上不会这样的，这只是为了模拟一下这种情况，然后又是使用同一个实体类才这样做。
## 编码
先将上一个例子的三个类copy出来并重新命名。

![CustConvert](http://qiniu.fullscreendeveloper.cn/writestore/1550029852938.png)

对GsonResponseBodyConverter的convert方法进行修改，其实统一Resonse并没有什么难度，只是先一步将Resonse返回的数据使用原生的`JSONObject`先解析一遍，多加判断，多加try而已，具体代码如下：
```java
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
        mockResult.add("{\n\"status\": 200,\"msg\": \"请求成功\",\"userInfo\": {\"id\": \"2\",\"name\": \"数据格式 2\",\"stargazers_count\": 2}}");
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        // 这里就是对返回结果进行处理
        // 其实我根本就没使用真正返回的结果，都是用的自定义的结果
        String jsonString = value.string();
        try {
            // 这里为了模拟不同的网络请求，所以采用了本地字符串的格式然后进行随机选择判断结果。
            int resultIndex = random.nextInt(mockResult.size());

            // 这里模拟不同的数据结构
            jsonString = mockResult.get(resultIndex);

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
```
## 演示

![MultipleResponse](/image/MultipleResponse.gif)

# 结束
[源码](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/sample/view/activity/MultipleResponseActivity.java)

### 广告
如果对你有帮助，请给我一个star，感激不尽。
如果有什么错误，请尽量提出来，我会及时修改。