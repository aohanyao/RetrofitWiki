---
title: RxJava2 + Retrofit2 完全指南 之 JsonQueryParameters
tags: Android之路
grammar_cjkRuby: true
---


## 前言
一般情况下，我们提交数据都是放在`body`中进行提交的，而`GET`却是放到`path`中进行提交的，有的时候
需要用`GET`提交`Json`格式的数据，所以本篇就是来实现`GET`请求提交`Json`参数。
## 实现
实现方式有两种：第一种是我们自己使用序列化库对参数进行json序列化，很明显，这种方式是需要我们每次都
去手动序列化一次，不太符合编程的思想。而第二种则是通过自定义`ConverterFactory`和自定义`Annotation
(注解)`的方式来进行，只要是标注上我们json注解的参数，我就可以通过自定义序列化过程来达到我们的目的。

### 自定义Annotation(注解)
```java
@Retention(RUNTIME)
@interface Json {
}
```
以上注解主要是为了标识我们需要控制序列化的参数，就像`@Query`一样使用，如下：
```java
interface Service {
    @GET("/filter")
    Observable<AuthorizationBean> exampleJsonParam(@JsonParam @Query("value") Filter value);
  }
```
当然，像这样直接使用肯定是不行的，我们虽然定义了注解，但是却没有地方对注解进行解析，所以接下来就是对注解进行解析了。
### 自定义Convert
Retrofit底层其实并没有对参数的序列化和对返回结果的反序列化并没有做处理，只有一个默认的`BuiltInConverters`也只是做了简单的`toString`而已，但是提供了`Converter`接口和`Converter.Factory`抽象类提供我们使用。我们常用的`GsonConverterFactory`就是对他们的实现，具体可以看看[这里，对返回结果的通过]()
#### 继承Converter.Factory和Convert并实现相关接口
代码量不多，也就下面的几行，其中最主要的是`annotation instanceof JsonParam`判断是否是我们的自定义的注解，然后通过`Gson`将对象序列化为`Json`字符串即可。

```java
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
```
#### 查看结果
运行起来后通过log拦截器拦截到以下信息，

> GET https://api.github.com/filter?value=%7B%22userId%22%3A%2235%22%7D http/1.1

其中`%7B%22userId%22%3A%2235%22%7D`是`{"userId":"35"} `经过URL编码之后的结果，所以我们是转换成功了。如下图：

![请求结果](http://qiniu.fullscreendeveloper.cn/writestore/1549161924717.png)

## 总结
Retrofit源码解析，虽然已经完成了我们的需求，但是对于为什么能这么做就不是很清楚，所以接下来就是进行部分源码的解析，了解为什么我们可以通过自定义Convert的方式来完成自定义解析。

当然，Retrofit的源码较多，在这里是不会一一解答的，只讲本例子相关的一部分。

首先来看关于`@Query`的解析部分。代码在`RequestFactory.Builder.parseParameterAnnotation()`中。如下图：
![parseParameter](http://qiniu.fullscreendeveloper.cn/writestore/1549163058466.png)
从源码中可以看到，是调用`retrofit.stringConverter`方法来进行转换的。

而`retrofit.stringConverter`中又是将我们在创建`retrofit` addConverterFactory 的增加的convert进行循环遍历调用，只要不为null，就代表转换成功。
![stringConvert](http://qiniu.fullscreendeveloper.cn/writestore/1549163160816.png)

最终会调用到我们的`stringConvert`方法，返回我们需要的值。

![cust stringConvert](http://qiniu.fullscreendeveloper.cn/writestore/1549163461921.png)

### 例子源码
详细代码请看[这里](https://github.com/aohanyao/RetrofitWiki/blob/master/app/src/main/java/com/jc/retrofit/wiki/advanced/sample/view/activity/JsonQueryParametersActivity.java)，都是在一个类中完成的。
