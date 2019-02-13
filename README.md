# 🔥RxJava2 + Retrofit2 完全指南🌰


# Look here
- [JsonQueryParameters](wiki/JsonQueryParameters.md)
- [统一状态码和Exception处理](wiki/统一状态码和Exception处理.md)
- [动态Url/Path/Parameter/Header](wiki/DynamicBaseUrl.md)
- [对返回Response的统一处理](wiki/对返回Response的统一处理.md)

# Doing

## 每种注解简单使用
### GET
#### Get请求提交Json字符串
### POST
#### POST 提交Body对象
后端接收数据有两种方式：`RequestParameter`和`RequestBody`,RequestParameter接收方式的话需要我们通过key=value的方式进行上传，是一种非常普遍的传输方式。而RequestBody可以说是前端人员最喜欢的传值方式了，因为我们可以什么都不做，直接将对象序列化为Json字符串加入到body中就能进行提交了，省去一个参数一个参数的填充，下面就来看看如何使用@Body，而注解的参数对象又是如何序列化为Json字符串的？
### 自定义请求方式
Retrofit为我们提供了默认实现的几种请求,基本情况下是完全够用了，在特殊情况下我们可以进行请求方式的自定义来满足我们的需求，这里我们创建一个`PP`请求。
## 提交不编码的特殊字符(UrlEncode)
特殊情况下，我们会提交`<>`这样的字符，而在不做特殊处理的情况下，Retrofit会把相关字符进行UrlEncode编码，变成这样`%DSE`，这样是不行的，我们需要传递的就是原始字符，该如何做？跟着例子一步一步来，首先我们创建一个请求。



## GsonConvertFactory
## 统一错误处理，
## 多数据格式的处理及统一状态码处理
### 自定义CallAdapter.Factory
### 自定义Convert
## 线程切换
### 请求的时候
### 自定义CallAdapter.Factory
## 动态BaseUrl
## 自定义注解
## 反序列化
## 上传文件
### 单文件上传
### 多文件上传
### 文件+参数上传
## 上传数组和集合
## POST对象
## 特殊数组的处理
## 接口调用日志
## 请求日志打印
## 多请求并行嵌套
## 多请求队列嵌套
## Token(Authorization)认证
### Token认证简单讲解
### 静默重试

# 源码解析
