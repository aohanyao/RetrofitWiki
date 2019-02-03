# 🔥RxJava2 + Retrofit2 完全指南🌰


# look
- [JsonQueryParameters](wiki/JsonQueryParameters.md)


# Doing
# 前言
1. 本文详细描述了retrofit在实战项目中使用到的每个细节点和一些特别的使用技巧和封装技巧，旨在能让人在本例子中找到所有需要的例子。而且不单单只是讲解例子，其中还会穿插源码分析，让你明白为什么要这么写？啊，原来还可以这么写？
2. 本文不是一个白话文，本文包括但不限于框架的使用，并且进行了关联拓展，在观看本文，默认你已经有Android编程基础，也对Retrofit有一定的了解（起码看过[官方文档]()）,我将不从`Hello World讲起`，不啰嗦，直接命中重点。

# 从HTTP开始
## 为什么要从HTTP开始呢？
在我看来，做Android应用开发， 做得最多的就是和网络打交道，而我们对其中涉及的细节却是不是很清楚，所以我们先从HTTP开始，一步一步的开始讲解，有一个大体的概念，以防后面在穿插源码分析的时候摸不着头脑。当然，HTTP的东西很多，不可能在这一个篇幅中讲解完成，所以也只是将一些重点和本文中使用到的部分。
### HTTP概念
**超文本传输协议**(HyperText Transfer Protocol)，是一种应用层协议，它与HTML(超文本标记语言一同诞生)。

### 什么是URL
URL(**U**niform **R**esource **I**dentifiers)，统一资源定位符。我们平常在发起请求的API地址就属于`URL`

URL主要由三部分组成：
> 协议类型、服务器地址(端口号)、路径(Path)

即：
> 协议类型://服务器地址:端口号/路径(Path)

如下 例子：
>https://api.github.com/orgs/octokit/repos

其中`https`是协议类型，`api.github.com`是服务器地址，后面默认的80端口，`orgs/octokit/repos`是路径(Path)。

### 发起网络请求都发生了什么？

### 报文
一个报文由四部分组成：
> 请求行
> 请求头
> 空行
> body

#### 请求报文 - Request
一个请求报文由如下内容组成：
>GET /user  HTTP/1.1
>Host: api.github.com
>Content-Type: application/x-www-form-urlencoded
>Content-Length：19 
>        <p>
>searchName=aohanyao

以上是访问GitHub的请求报文，内容进行了删减。
第一行`GET /user / HTTP/1.1`为`请求行`，其中`GET`是`请求方法(Request Method)`，`/user`是的是`访问路径`，`HTTP/1.1`之的是`协议及版本`。
第二行至第四行：
>Host: api.github.com
>Content-Type: application/x-www-form-urlencoded
>Content-Length：1024 

这部分为`请求头(Headers)`，请求头由多个`键值对`组成。

第五行为`空白行`，这行没有内容，主要是用于分割`请求头`和`Body`。

第六行`searchName=aohanyao`为`body`，除去`GET`请求方式，其他常用方式传递的数据都是 放在`Body`中进行提交的。

##### 请求方法 - Request method
###### GET
- 获取资源。
- 不对资源进行更改。
- 不包含body数据，数据在 `URL`中传递。

报文如下：
>GET  /users/1  HTTP/1.1 
>Host: api.github.com

Retrofit代码如下：
```java
@GET("/users/{id}") 
Call<User> getUser(@Path("id") String id, @Query("gender") String gender);
```

###### POST
- 新增或者修改资源。
- 提交给服务器的数据放在`Body`中提交。

报文如下：
>POST  /users  HTTP/1.1 
>Host: api.github.com
> Content-Type: application/x-www-form-urlencoded 
> Content-Length: 13
>        <p>
> name=raohanyao&gender=male

Retrofit代码如下：
```java
@FormUrlEncoded 
@POST("/users") 
Call<User> addUser(@Field("name") String name, @Field("gender") String gender);
```
###### PUT
- 修改资源
- 提交给服务器的数据放在`Body`中提交


报文如下：
> PUT  /users/1  HTTP/1.1 
> Host: api.github.com 
> Content-Type: application/x-www-form-urlencoded 
> Content-Length: 13
>                <p>
> gender=female

Retrofit代码如下：
```java
@FormUrlEncoded 
@PUT("/users/{id}") 
Call<User> updateGender(@Path("id") String id, @Field("gender") String gender)
```
###### DELETE
- 删除资源
- 不包含body数据，数据在 `URL`中传递。

报文如下：
> DELETE  /users/1  HTTP/1.1 
> Host: api.github.com

Retrofit代码如下：
```java
@DELETE("/users/{id}") 
Call<User> getUser(@Path("id") String id, @Query("gender") String gender);
```
###### HEAD
- 使用方式与GET完全相同。
- 区别在于`HEAD`的响应报文中无`Body`。

其它不常用略...
#### 响应报文 - Response









 

# 正文
## 每种注解简单使用
### GET
#### Get请求提交Json字符串
### POST
#### POST 提交Body对象
后端接收数据有两种方式：`RequestParamter`和`RequestBody`,RequestParamter接收方式的话需要我们通过key=value的方式进行上传，是一种非常普遍的传输方式。而RequestBody可以说是前端人员最喜欢的传值方式了，因为我们可以什么都不做，直接将对象序列化为Json字符串加入到body中就能进行提交了，省去一个参数一个参数的填充，下面就来看看如何使用@Body，而注解的参数对象又是如何序列化为Json字符串的？
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
