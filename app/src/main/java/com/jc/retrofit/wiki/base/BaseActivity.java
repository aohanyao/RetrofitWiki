package com.jc.retrofit.wiki.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.jc.retrofit.wiki.R;
import com.jc.retrofit.wiki.biz.GitHubService;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created by 江俊超 on 2019/1/16.
 * Version:1.0
 * Description:
 * ChangeLog:
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected TextView mResultTv;
    protected TextView mDescTv;
    protected Retrofit retrofit;
    protected GitHubService service;

    protected String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_get_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        initToolbar(toolbar);
        initView();
    }

    private void initView() {
        findViewById(R.id.mSendRequestBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        mResultTv = findViewById(R.id.mResultTv);
        mDescTv = findViewById(R.id.mDescTv);
    }

    protected abstract void sendRequest();

    protected void initToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected HttpLoggingInterceptor getHttpLoggingInterceptor(){
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        return logInterceptor;
    }

}
