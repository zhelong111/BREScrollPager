package com.etmay.brescrollpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.etmay.brescrollpager.ui.MyViewPager;


public class MainActivity extends AppCompatActivity {

    private MyViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mPager = (MyViewPager) findViewById(R.id.myPager);
        mPager.startSchedule(1400);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPager.stopSchecule();
    }
}
