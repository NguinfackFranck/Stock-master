package com.example.stockmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockmanagement.activities.DashboardActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String PREF_NAME= "app_prefs";
    private static final String HAS_ENTERED_DASHBOARD = "has_entered_dashboard";

    private TextView tvGreeting;
    private TextView tvShopName;
    private MaterialCardView logoContainer;
    private TextView tvWelcome;
    private MaterialCardView featuresCard;
    private MaterialButton btnEnterDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if we should skip the landing screen
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        boolean hasEnteredBefore = prefs.getBoolean(HAS_ENTERED_DASHBOARD, false);

        if (hasEnteredBefore) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish(); // Close MainActivity so user doesn't go back to it
            return;
        }

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setGreeting();
        animateEntry();
        setupClickListeners();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        tvShopName = findViewById(R.id.tv_shop_name);
        logoContainer = findViewById(R.id.logo_container);
        tvWelcome = findViewById(R.id.tv_welcome);
        featuresCard = findViewById(R.id.features_card);
        btnEnterDashboard = findViewById(R.id.btn_enter_dashboard);
    }

    private void setGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (timeOfDay < 12) {
            greeting = getString(R.string.greeting_morning);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = getString(R.string.greeting_afternoon);
        } else {
            greeting = getString(R.string.greeting_evening);
        }
        tvGreeting.setText(greeting);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String shopName = prefs.getString("shop_name", getString(R.string.default_shop_name));
        tvShopName.setText(shopName);
    }

    private void animateEntry() {
        // Use ViewPropertyAnimator for smoother, more professional animations
        long duration = 800;
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

        tvGreeting.setTranslationY(-50f);
        tvGreeting.animate().alpha(1f).translationY(0f).setDuration(duration).setInterpolator(interpolator).start();

        tvShopName.setTranslationY(-30f);
        tvShopName.animate().alpha(1f).translationY(0f).setStartDelay(200).setDuration(duration).setInterpolator(interpolator).start();

        logoContainer.setScaleX(0.5f);
        logoContainer.setScaleY(0.5f);
        logoContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(400).setDuration(duration).setInterpolator(interpolator).start();

        tvWelcome.setTranslationY(30f);
        tvWelcome.animate().alpha(1f).translationY(0f).setStartDelay(600).setDuration(duration).setInterpolator(interpolator).start();

        featuresCard.setTranslationY(50f);
        featuresCard.animate().alpha(1f).translationY(0f).setStartDelay(800).setDuration(duration).setInterpolator(interpolator).start();

        btnEnterDashboard.setTranslationY(100f);
        btnEnterDashboard.animate().alpha(1f).translationY(0f).setStartDelay(1000).setDuration(duration).setInterpolator(interpolator).start();
    }

    private void setupClickListeners() {
        btnEnterDashboard.setOnClickListener(v -> {
            // Save preference to skip this screen next time
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(HAS_ENTERED_DASHBOARD, true).apply();

            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                    finish(); // Optional: finish if you never want them to come back to landing
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                })
                                .start();
                    })
                    .start();
        });
    }
}
