# JsonQueryParameters

## 前言
一般情况下，我们提交数据都是放在`body`中进行提交的，而`GET`却是放到`path`中进行提交的，有的时候
需要用`GET`提交`Json`格式的数据，所以本篇就是来实现`GET`请求提交`Json`参数。
## 实现
实现方式有两种：第一种是我们自己使用序列化库对参数进行json序列化，很明显，这种方式是需要我们每次都
去手动序列化一次，不太符合编程的思想。而第二种则是通过自定义`ConverterFactory`和自定义`Annotation
(注解)`的方式来进行，只要是标注上我们json注解的参数，我就可以通过自定义序列化过程来达到我们的目的。

### 自定义注解
```java
@Retention(RUNTIME)
  @interface Json {
  }
```