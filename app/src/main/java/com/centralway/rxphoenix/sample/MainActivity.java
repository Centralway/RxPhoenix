package com.centralway.rxphoenix.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.launchConfigChangeTestActivityButton)
    void launchConfigChangeTestActivity() {
        startActivity(new Intent(this, ConfigurationChangeTestActivity.class));
    }

    @OnClick(R.id.launchExampleUsageActivityButton)
    void launchExampleUsageActivity() {
        startActivity(new Intent(this, DemoActivity.class));
    }

}
