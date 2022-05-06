package com.honkasoft.zhuanfalun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Title text interaction
        findViewById(R.id.titleText).setOnClickListener(view -> openLinkIntent(getString(R.string.zhuanFalunBookLink)));

        // 9 sessions interaction
        findViewById(R.id.nineSessionsButton).setOnClickListener(v -> openLinkIntent(getString(R.string.nineSessionsLink)));

        // Articles interaction
        findViewById(R.id.latestArticlesButton).setOnClickListener(v -> openLinkIntent(getString(R.string.latestArticlesLink)));

        // Exercises interaction
        findViewById(R.id.exercisesButton).setOnClickListener(v -> openLinkIntent(getString(R.string.exercisesLink)));

        // Preferences interaction
        findViewById(R.id.setGongNotification).setOnClickListener(v -> openPreferencesInterface());

        // Github interaction
        findViewById(R.id.githubButton).setOnClickListener(v -> openLinkIntent(getString(R.string.githubLink)));
    }

    private void openLinkIntent(String link)
    {
        if(link == null) return;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private void openPreferencesInterface()
    {
        PreferencesFragment preferences = new PreferencesFragment(MainActivity.this);
        preferences.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        preferences.show();
    }
}