package com.cybernetxsystems.bluetooth.le;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class Start extends Activity{
    public static final long DEFAULT_ANIMATION_DURATION = 2500;
    protected View intro;
    protected View mFrameLayout;
    protected float mScreenHeight;
    protected View titulo;
    protected View titulo2;
    protected float mScreenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        titulo = findViewById(R.id.titulo);
        titulo2 = findViewById(R.id.titulo2);
        intro = findViewById(R.id.logotipo);
        mFrameLayout = findViewById(R.id.contenedor);
        final Intent intent = new Intent(this, DeviceScanActivity.class);
        mFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });
    }


    protected void onStartAnimation() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(-mScreenHeight/2, 0);
        ValueAnimator valueAnimator2 = ValueAnimator.ofFloat(mScreenHeight/2, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                intro.setTranslationX(value);
            }
        });
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                titulo.setTranslationX(value);
            }
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator(2f));
        valueAnimator.setDuration(DEFAULT_ANIMATION_DURATION);
        valueAnimator.start();
        valueAnimator2.setInterpolator(new AccelerateInterpolator(2f));
        valueAnimator2.setDuration(DEFAULT_ANIMATION_DURATION);
        valueAnimator2.start();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(titulo2, "alpha", 0f, 1f);
        fadeIn.setDuration(2500);
        fadeIn.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenHeight = displaymetrics.heightPixels;
        mScreenWidth = displaymetrics.widthPixels;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            onStartAnimation();
        }
    }
}
