package org.ms.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.ms.module.base.view.BaseActivity;

public class SplashActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void initView() {
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected boolean isFullScreen() {
        return true;
    }
}
