package com.find.doc.activities;

import com.airbnb.lottie.LottieAnimationView;
import com.find.doc.BaseActivity;
import com.find.doc.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView lottie = findViewById(R.id.lottieSplash);

        lottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkNetworkAndProceed();
                handleLaunchFlow();
            }
        });
         new Handler().postDelayed(this::checkNetworkAndProceed, SPLASH_DELAY);
    }

    private void checkNetworkAndProceed() {
        if (isNetworkAvailable()) {
            handleLaunchFlow();
        } else {
            Intent intent = new Intent(this, NoNetworkActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean handleLaunchFlow() {
        SharedPreferences prefs = getSharedPreferences("login_session", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        boolean everLoggedIn = prefs.getBoolean("everLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            intent = new Intent(this, HomeActivity.class);
        } else if (!everLoggedIn) {
            // First launch
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
        return true;
    }
}
