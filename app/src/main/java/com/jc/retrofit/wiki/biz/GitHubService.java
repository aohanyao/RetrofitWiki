package com.jc.retrofit.wiki.biz;

import com.jc.retrofit.wiki.annotation.JsonParam;
import com.jc.retrofit.wiki.bean.AuthorizationBean;
import com.jc.retrofit.wiki.bean.JsonQueryParametersBean;
import com.jc.retrofit.wiki.bean.Repo;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface GitHubService {
    @GET("users/{user}/repos")
    Call<ResponseBody> listResponseBodyRepos(@Path("user") String user,
                                             @Query("type") String type);

    @GET("users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user,
                               @Query("type") String type);

    @GET("users/{user}/repos")
    Observable<List<Repo>> listRxJava2Repos(@Path("user") String user,
                                            @Query("type") String type);

    @GET("users/{user}/repos")
    Flowable<List<Repo>> listRxJava2FlowableRepos(@Path("user") String user,
                                                  @Query("type") String type);

    @GET("authorizations")
    Observable<List<AuthorizationBean>> listAuthorizations();

    @GET("/filter")
    Call<ResponseBody> exampleJsonParam(@JsonParam @Query("value") JsonQueryParametersBean value);
}