package com.jc.retrofit.wiki.advanced;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import com.jc.retrofit.wiki.R;
import com.jc.retrofit.wiki.base.BaseActivity;

public class AdvancedMainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_request_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        initToolbar(toolbar);

    }

    @Override
    protected void sendRequest() {
        // None
    }

}
