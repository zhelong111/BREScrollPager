package com.etmay.brescrollpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
//        mPager.startSchedule(1400);

        for (int i = 0; i < 100; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.mipmap.g3);
            iv.setTag(i);
            mPager.addView(iv);
        }

        mPager.setPagerTransformer(new MyViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
//                ViewHelper.setAlpha(page, 1 - Math.abs(position) * (1f/4));
//                ViewHelper.setScaleX(page, 1 - 0.1f * Math.abs(position));
//                ViewHelper.setScaleY(page, 1 - 0.1f * Math.abs(position));
//
//                if (position < 0) { // 左移
//                    View rightView = mPager.getRightView();
//                    if (rightView != null) {
//                        ViewHelper.setAlpha(rightView, 0.25f + Math.abs(position) * (3/4f));
//                        ViewHelper.setScaleX(rightView, 0.9f + 0.1f * Math.abs(position));
//                        ViewHelper.setScaleY(rightView, 0.9f + 0.1f * Math.abs(position));
//                    }
//                } else if (position > 0) { // 右移
//                    View leftView = mPager.getLeftView();
//                    if (leftView != null) {
//                        ViewHelper.setAlpha(leftView, 0.25f + Math.abs(position) * (3/4f));
//                        ViewHelper.setScaleX(leftView, 0.9f + 0.1f * Math.abs(position));
//                        ViewHelper.setScaleY(leftView, 0.9f + 0.1f * Math.abs(position));
//                    }
//                }

            }
        });

        mPager.setOnItemClickListener(new MyViewPager.OnItemClickListener() {
            @Override
            public void onItemClicked(View itemView, int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPager.stopSchecule();
    }

}
