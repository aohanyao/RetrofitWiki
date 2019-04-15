---
title: RxJava2 + Retrofit2 完全指南 之 Authenticator处理与Token静默刷新
tags: Android之路
grammar_cjkRuby: true
---
# 前言
今年是9102年了，应该没有还在用`userId`来鉴权了吧，也应该很少人使用`cookie`来保持会话了吧？而现在更常用的是`Authorization`,

### 关于Authorization
简略的讲一讲Authorization，如果要深入了解的话请看底部的参考文章链接。Authorization的认证方式在我接触中有两种
- Basic
- Bearer

#### Basic
HTTP基本认证，在请求的时候加上以下请求头：
> Authorization : basic base64encode(username+":"+password)）

将用户名和密码用英文冒号(:)拼接起来，并进行一次Base64编码。服务端拿到basic码，然后自己查询相关信息再按照`base64encode(username+":"+password)）`的方式得出当前用户的basic进行对比。

#### Bearer
授权完成后会返回类似下面的数据结构：
```json
{
	"token_type": "Bearer",
	"access_token": "xxxxx",
	"refresh_token": "xxxxx"
}
```
而其中的`refresh_token`的作用是在`access_token`失效的时候进行重新刷新传入的参数，具体怎么传要看各自项目的实现方式。
`access_token`就是我们的认证令牌。token_type是令牌的类型，而我现在使用到的只有`bearer`，其它类型未碰到，希望各位看官能补充一下。
在使用的时候需要加上以下请求头：
> Authorization : token_type access_token

也就是这样：
> Authorization: Bearer xxxxx



# 实现
## 方式1 ：authenticator
`authenticator`是在创建`OkHttpClient`的时候能够设置的一个方法，接收的是一个`okhttp3.Authenticator`的interface，默认不设置的话是一个`NONE`的空实现，而回调的地方是在`okhttp3.internal.http.RetryAndFollowUpInterceptor.followUpRequest()`

![Authenticator](http://qiniu.fullscreendeveloper.cn/writestore/1550038526681.png)

![followUpRequest](http://qiniu.fullscreendeveloper.cn/writestore/1550038568539.png)

### 编码
相关代码也比较简单，在`okhttp3.Authenticator`的注释上面也写有简单的例子，核心代码就以下几行：

```java
private Authenticator authorization = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {

            //-----------核心代码-------
            // 这里抛出的错误会直接回调 onError
            // 这里发起的请求是同步的，刷新完成token后再增加到header中
            // String token = refreshToken();
            String token = Credentials.basic("userName", "password", Charset.forName("UTF-8"));
            return response.request()
                    .newBuilder()
                    .header("Authorization", token)
                    .build();
            //-----------核心代码-------
        }
    };
```
以上就是主要代码，其中演示的是`basic`方式的认证模式，`bearer`方式的没实现，其实也只是`refreshToken()`中发起一个`同步请求`去刷新一下`token`并保存，后面的步骤都是一样的。
### 如何使用
创建OkHttpClient调用，当然，也可以直接写匿名内部类的实现，都是可以的。
```java
retrofit = new Retrofit.Builder()
                .client(new OkHttpClient.Builder()
                        .authenticator(authorization)// 增加重试
                        .addInterceptor(getHttpLoggingInterceptor())
                        .build())
                .baseUrl("https://api.github.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
```
### 演示
为了演示方便看到结果，我在`Authenticator`的实现类中增加了一些回调主线程的方法，具体看一下源码即可，对于主要结果没什么影响。

![Authenticator](http://qiniu.fullscreendeveloper.cn/writestore/Authorization1Activity-2019213143240-min.gif)

### 总结
使用官方提供的`Authenticator`有一个很明显的问题，那就是会占用`重试`，像示例中，我并没有传入一个正确的`token`,就导致一直在回调`Authenticator`,直到达到了最大重试次数为止。而往往需求是token失效以后选择重试一次，成功了继续请求，再次失败则提示登录，所以这个方法使用得不多。
## 方式2 ：Interceptor
上面`okhttp3.Authenticator`的实现方式其实是在`RetryAndFollowUpInterceptor`中判断和回调的，由此，可以自定义一个`Interceptor`，由开发者来自行判断和跳转。

### 编码
详细代码如下：
```java
Interceptor mAuthenticatorInterceptor = new Interceptor() {
	@Override
	public Response intercept(Chain chain) throws IOException {
		// 获取请求
		Request request = chain.request();
		// 获取响应
		Response response = chain.proceed(request);
		// 在这里判断是不是是token失效
		// 当然，判断条件不会这么简单，会有更多的判断条件的
		if (response.code() == 401) {
			// 这里应该调用自己的刷新token的接口
			// 这里发起的请求是同步的，刷新完成token后再增加到header中
			// 这里抛出的错误会直接回调 onError
//                String token = refreshToken();
			String token = Credentials.basic("userName", "password", Charset.forName("UTF-8"));
			// 创建新的请求，并增加header
			Request retryRequest = chain.request()
					.newBuilder()
					.header("Authorization", token)
					.build();

			// 再次发起请求
			return chain.proceed(retryRequest);
		}

		return response;
	}
}
```
### 使用
和方法一相同，在创建`HttpClient`的时候`addInterceptor(mAuthenticatorInterceptor)`,将我们自己的拦截器加入进行即可。

### 演示

![AuthenticatorInterceptor](/image/Authorization2Activity-2019213143241.gif)

### 总结
从演示中可以看出，在第一次返回`401`的时候，进行了一次token的获取，并且再次进行了请求，圆满符合我们的预期，只重试一次。
# 最后
## 分析
>可能会有疑问：为什么使用`Interceptor`就能达到我们预期的效果？`Interceptor`到底是如何工作的？



首先`Interceptor`添加是有先后顺序的，首先添加的是我们设置的`Interceptor`,然后添加的才是`okhttp`的`Interceptor`。如源码中：
![Add Interceptor](http://qiniu.fullscreendeveloper.cn/writestore/1550051977827.png)


总的来说，`okhttp`的实现方式就是通过`Interceptor`来组成一个一个的`chian`来实现的。每个`Interceptor`里面的`intercept()`方法内部都会调用`Chain.proceed()`方法，将请求交给下一个`Interceptor`,由此类推，一直到最后一个`Interceptor`请求完成。
需要注意的是`proceed`是同步的，也就是调用`proceed`方法之后需要等等下一个`Interceptor`进行处理，当最后一个`Interceptor`请求到数据，经过自己的处理之后，再往上返回`Response`,直到第一个`Interceptor`为止，返回数据。主要关系如下图：


![Interceptor ex](http://qiniu.fullscreendeveloper.cn/writestore/1550052695796.png)

这些所有的`Interceptor`里面的`proceed`都是调用了一次，那么我们增加一个`Interceptor`，等到`proceed`返回了`Response`之后，对`Response`进行判断，如果是认证失败，我们则刷新一下token，重新创建Request，再调用一次`proceed`方法。如果再失败了，就不会再回调到当前的`Interceptor`,如下图：

![AuthenticatorInterceptor](http://qiniu.fullscreendeveloper.cn/writestore/1550055345106.png)

## 源码
- [authenticator](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/sample/view/activity/Authorization1Activity.java)
- [Interceptor](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/sample/view/activity/Authorization2Activity.java)

# 参考文章
- [OAuth2.0协议原理与实现：协议原理](https://my.oschina.net/wangzhenchao/blog/851773)
- [OAuth2.0协议原理与实现：TOKEN生成算法](https://my.oschina.net/wangzhenchao/blog/856964)
- [OAuth2.0协议原理与实现：协议实现](https://my.oschina.net/wangzhenchao/blog/862094)