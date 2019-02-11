---
title: RxJava2 + Retrofit2 完全指南 之 统一状态码/Exception处理
tags: Android之路
grammar_cjkRuby: true
---

## 前言
直接上数据结构：
```json
{
	"code": 200,
	"data": {
		"id": "1",
		"name": "name1",
		"stargazers_count": 1
	},
	"msg": "请求成功"
}
```
上面的数据结构是一般比较简单而常见的数据结构，在正确的情况下我们只关心`data`里面的数据，错误的情况下我们关心`code`和`msg`提示，而区分这两种情况又要不断的写大量的样板代码，这不是首选。所以就有了两种方法：
- 通过定义泛型T来处理
- 自定义ResponseBodyConverter实现统一处理

首先讲讲泛型T的处理方式，基本如下：
```java
public class BaseDataModel<T> {
    private int code;
    private T data;
    private String msg;

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public boolean isSuccessful() {
        return code == 200;
    }
}
```
使用也比较简单，只需要将泛型替换为相应的实体类就行。但是这也有一个不好的地方就是一旦项目中对接的数据结构变得繁杂的时候，就需要不断的顶部相应的实体了，还是比较麻烦，[下一篇]()会讲到,所以我们还是使用自定义ResponseBodyConverter实现统一处理。

## 实现
### 分析
我在使用Retrofit的时候，一般是使用的是`GsonConverterFactory`,而`GsonConverterFactory`则是负责将我们的提交的数据进行序列化和将返回的数据进行反序列化的，所以只要我们分析完成其中的代码，看明白他是怎么对数据进行反序列化的，那么我们就能做到统一的数据转换了。
####  源码目录
![GsonConverter源码目录](http://qiniu.fullscreendeveloper.cn/writestore/1549164772462.png)

从上图我们可以看到，其实只有三个类，分别是：GsonConverterFactory、GsonRequestBodyConverter和GsonResponseBodyConverter。
- GsonConverterFactory 主入口，负责联通GsonRequestBodyConverter和GsonResponseBodyConverter
- GsonRequestBodyConverter 负责将注解为`@Body`的对象序列化为Json字符串。
- GsonResponseBodyConverter 负责将返回的json字符串反序列化为对象。

其中GsonResponseBodyConverter的源码内容如下：
```java
final class GsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  private final Gson gson;
  private final TypeAdapter<T> adapter;

  GsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody value) throws IOException {
    // 这里就是对返回结果进行处理
    JsonReader jsonReader = gson.newJsonReader(value.charStream());
    try {
      T result = adapter.read(jsonReader);
      if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
        throw new JsonIOException("JSON document was not fully consumed.");
      }
      return result;
    } finally {
      value.close();
    }
  }
}

```
从上面的源码可以看到，convert方法中其实就是通过Gson进行看一次序列化而已，而ResponseBody提供了string方法，直接将返回结果转换为了字符串，那么我们就可以在中间加一道工序，将其中的数据进行相关的判断处理，再进行返回。

### 编码
首先将`GsonConverter`的源码copy过来，然后改个名字，如下：

![HandlerErrorGsonConvert](http://qiniu.fullscreendeveloper.cn/writestore/1549871328971.png)


为了模拟不同的数据接口，硬编码了以下三种数据结构。

- 数据结构·1
```json
{
	"code": 200,
	"message": "成功，但是没有数据",
	"data": []
}
```

- 数据结构·2
```json
{
	"code": -1,
	"message": "这里是接口返回的：错误的信息，抛出错误信息提示！",
	"data": []
}
```

- 数据结构·3
```json
{
	"code": 401,
	"message": "这里是接口返回的：权限不足，请重新登录！",
	"data": []
}
```
从上面三种数据结构分析，我们需要在`code`为`200`的情况下直接序列化`data`,其它情况下抛出`code`和`msg`,所以这里我们还需要定义一个`Exception`类来承载错误信息。

#### 创建Exception类
Exception类如下，主要是对一些和后端约定的状态码进行转义和包裹。
```java
public class NetErrorException extends IOException {
    private Throwable exception;
    private int mErrorType = NO_CONNECT_ERROR;
    private String mErrorMessage;
    /*无连接异常*/
    public static final int NoConnectError = 1;
    /**
     * 数据解析异常
     */
    public static final int PARSE_ERROR = 0;
    /**
     * 无连接异常
     */
    public static final int NO_CONNECT_ERROR = 1;
    /*网络连接超时*/
    public static final int SocketTimeoutError = 6;
    /**
     * 无法连接到服务
     */
    public static final int ConnectExceptionError = 7;
    /**
     * 服务器错误
     */
    public static final int HttpException = 8;
    /**
     * 登陆失效
     */
    public static final int LOGIN_OUT = 401;
    /**
     * 其他
     */
    public static final int OTHER = -99;
    /**
     * 没有网络
     */
    public static final int UNOKE = -1;
    /**
     * 无法找到
     */
    public static final int NOT_FOUND = 404;
    /*其他*/

    public NetErrorException(Throwable exception, int mErrorType) {
        this.exception = exception;
        this.mErrorType = mErrorType;
    }

    public NetErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetErrorException(String message, int mErrorType) {
        super(message);
        this.mErrorType = mErrorType;
        this.mErrorMessage = message;
    }

    @Override
    public String getMessage() {
        if (!TextUtils.isEmpty(mErrorMessage)) {
            return mErrorMessage;
        }
        switch (mErrorType) {
            case PARSE_ERROR:
                return "数据解析异常";
            case NO_CONNECT_ERROR:
                return "无连接异常";
            case OTHER:
                return mErrorMessage;
            case UNOKE:
                return "当前无网络连接";
            case ConnectExceptionError:
                return "无法连接到服务器，请检查网络连接后再试！";
            case HttpException:
                try {
                    if (exception.getMessage().equals("HTTP 500 Internal Server Error")) {
                        return "服务器发生错误！";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (exception.getMessage().contains("Not Found"))
                    return "无法连接到服务器，请检查网络连接后再试！";
                return "服务器发生错误";
        }


        try {
            return exception.getMessage();
        } catch (Exception e) {
            return "未知错误";
        }

    }
    /**
     * 获取错误类型
     */
    public int getErrorType() {
        return mErrorType;
    }
}
```
#### 统一订阅处理
由于这里使用的是`RxJava`,需要自定义一个`Subscriber`来对`Convert`抛出的`Exception`进行捕获，也需要对其它`Exception`进行捕获和包裹，防止发生错误后直接崩溃，代码不多，如下：
```java
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

}
```
#### 修改HandlerErrorGsonResponseBodyConverter
这一步其实没什么难度，只是在`convert`方法中提前将ResponseBody.string()取出来，通过最简单的`JSONObject`来判断`code`,为200则返回`data`,否则抛出自定义错误。详细代码如下：
```java
final class HandlerErrorGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final TypeAdapter<T> adapter;

    /**模拟的假数据*/
    private final List<String> mockResult;

    private final Random random;

    HandlerErrorGsonResponseBodyConverter(TypeAdapter<T> adapter) {
        this.random = new Random();
        this.adapter = adapter;
        mockResult = new ArrayList<>();
        mockResult.add("{\"code\":200,\"message\":\"成功，但是没有数据\",\"data\":[]}");
        mockResult.add("{\"code\":-1,\"message\":\"这里是接口返回的：错误的信息，抛出错误信息提示！\",\"data\":[]}");
        mockResult.add("{\"code\":401,\"message\":\"这里是接口返回的：权限不足，请重新登录！\",\"data\":[]}");
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        // 这里就是对返回结果进行处理
        String jsonString = value.string();
        try {
            // 这里为了模拟不同的网络请求，所以采用了本地字符串的格式然后进行随机选择判断结果。
            int resultIndex = random.nextInt(mockResult.size() + 1);
            if (resultIndex == mockResult.size()) {
                return adapter.fromJson(jsonString);

            } else {
                // 这里模拟不同的数据结构
                jsonString = mockResult.get(resultIndex);

                Log.e("TAG", "这里进行了返回结果的判断");
                // ------------------ JsonObject 只做了初略的判断，具体情况自定
                JSONObject object = new JSONObject(jsonString);
                int code = object.getInt("code");
                if (code != 200) {
                    throw new NetErrorException(object.getString("message"), code);
                }
                return adapter.fromJson(object.getString("data"));

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


#### 如何调用
主要是有以下两个地方：
1. 创建Retrofit的时候将`addConverterFactory`换成自定义的`HandlerErrorGsonConverterFactory.create()`
2. RxJava的subscribeWith换成自定义的ApiSubscriber<T>。

部分代码如下：

```java
......
retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(HandlerErrorGsonConverterFactory.create()) // 这里使用的是用自己自定义的转换器
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

service = retrofit.create(GitHubService.class);
......

mResultTv.setText("创建请求................\n");
disposable = service.listRxJava2FlowableRepos("aohanyao", "owner")
	.subscribeOn(Schedulers.io())
	.observeOn(AndroidSchedulers.mainThread())
	.subscribeWith(new ApiSubscriber<List<Repo>>() {
		@Override
		public void onNext(List<Repo> repos) {
			mResultTv.append("请求成功，repoCount:" + repos.size() + ":\n");
			for (Repo repo : repos) {
				mResultTv.append("repoName:" + repo.getName() + "    star:" + repo.getStargazers_count() + "\n");
			}
		}

		@Override
		protected void onFail(NetErrorException error) {
			mResultTv.append("请求失败" + error.getMessage() + "................\n");
		}

		@Override
		public void onComplete() {
			mResultTv.append("请求成功................\n");
		}
	});
```

####  演示

## 结束
这里只写了对返回状态码和错误的统一处理，并未对不同的数据结构进行处理，下一篇将对不同的数据结构进行统一处理。

[源码在这里](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/error/view/HandlerResponseErrorActivity.java)