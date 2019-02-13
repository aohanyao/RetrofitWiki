---
title: RxJava2 + Retrofit2 完全指南 之 动态Url/Path/Parameter/Header
tags: Android之路
grammar_cjkRuby: true
---
# 前言
因为有需求，才会有解决方案。本篇文章就是为了解决以下类似问题：

- 统一为所有接口加上一个参数，如`appType`或则`version`
- 统一为请求加上一个`header`
- 请求`path`变更了，需要按照一定规则将`path`进行替换


# 实现
实现思路也是比较简单的，只需要自己实现一个`Interceptor`，然后加在其它`Interceptor`之前，具体代码如下：
```java
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
		sendMessage("\n\n新请求地址和参数：" + request.url().toString() + "\n");
		return chain.proceed(request);
	}
}
```
使用：
```java
retrofit = new Retrofit.Builder()
			.client(new OkHttpClient.Builder()
					.addInterceptor(getHttpLoggingInterceptor())
					.addInterceptor(hostSelectionInterceptor)//加入自定义的拦截器
					.build())
			.baseUrl("https://api.github.com/")
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build();
```

演示：

![演示](/image/动态Url-Path-Parameter-Header.gif)
# 源码
[源码地址](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/sample/view/activity/DynamicBaseUrlActivity.java)