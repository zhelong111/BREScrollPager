package com.etmay.brescrollpager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.etmay.brescrollpager.ui.BREScrollPager;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class MainActivity extends AppCompatActivity {

    private BREScrollPager mPager;
    private View root;
    private View root2;
    private int currColor;
    private int preColor;
    private int[] colors = new int[] {
            0xff693377,0xffe9b377,0xffe9b3f7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        ViewPager
    }

    private void initView() {
        root = findViewById(R.id.root);
        root2 = findViewById(R.id.root2);
        currColor = preColor = colors[0];
        root.setBackgroundColor(currColor);
        root2.setBackgroundColor(preColor);

        mPager = (BREScrollPager) findViewById(R.id.myPager);
//        mPager.startSchedule(1600);

        for (int i = 0; i < 10; i++) {
            View child = View.inflate(this, R.layout.item_img, null);
            child.setTag(i);
            mPager.addView(child);
        }
//        mPager.setCurrItem(1);
        // 设置Listener之后才有自动
        mPager.setLoadMoreListener(new BREScrollPager.LoadMoreListener() {
            @Override
            public void onStart() {
                Toast.makeText(getApplicationContext(), "onStart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoading() {

            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "onFinish", Toast.LENGTH_SHORT).show();
            }
        });


        final int color = colors[0];
        final int dc = colors[1] - color;
        mPager.setPagerTransformer(new BREScrollPager.PageTransformer() {
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

        mPager.setOnItemClickListener(new BREScrollPager.OnItemClickListener() {
            @Override
            public void onItemClicked(View itemView, int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }
        });

        mPager.setOnPageChangeListener(new BREScrollPager.OnPageChangeListener() {
            @Override
            public void onPageChanged(int position) {
                ViewHelper.setAlpha(root2, 0.3f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(root2, "alpha", 0.3f, 0).setDuration(500);
                alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (!animation.isRunning()) {
                            root2.setBackgroundColor(currColor);
                        }
                    }
                });
                alpha.start();

                currColor = colors[position % colors.length];
                root.setBackgroundColor(currColor);
                ViewHelper.setAlpha(root, 0.3f);
                ObjectAnimator.ofFloat(root, "alpha", 0.3f, 1).setDuration(500).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPager.stopSchecule();
    }

}
