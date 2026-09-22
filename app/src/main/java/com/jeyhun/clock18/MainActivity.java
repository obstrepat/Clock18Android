package com.jeyhun.clock18;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView customTime;
    private TextView exactTime;
    private TextView realTime;
    private TextView dateText;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            render();
            handler.postDelayed(this, 100);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setStatusBarColor(Color.rgb(12,15,20));
        w.setNavigationBarColor(Color.rgb(12,15,20));
        buildUi();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.post(ticker);
        ClockWidgetProvider.updateAll(this);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return t;
    }

    private void buildUi() {
        int bg = Color.rgb(12,15,20);
        int txt = Color.rgb(245,247,251);
        int muted = Color.rgb(144,153,168);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(24));
        root.setBackgroundColor(bg);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView brand = text("18 SAAT", 13, muted, true);
        brand.setLetterSpacing(0.18f);
        root.addView(brand, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(20), dp(42), dp(20), dp(42));
        card.setBackgroundResource(com.jeyhun.clock18.R.drawable.panel_bg);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, dp(26), 0, 0);
        root.addView(card, cardLp);

        TextView label = text("ŞƏXSİ VAXT", 11, muted, true);
        label.setLetterSpacing(0.16f);
        card.addView(label);

        customTime = text("00:00:00", 58, txt, true);
        customTime.setGravity(Gravity.CENTER);
        customTime.setIncludeFontPadding(false);
        LinearLayout.LayoutParams timeLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeLp.setMargins(0, dp(14), 0, 0);
        card.addView(customTime, timeLp);

        exactTime = text("", 13, muted, false);
        exactTime.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams exactLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        exactLp.setMargins(0, dp(14), 0, 0);
        card.addView(exactTime, exactLp);

        LinearLayout ordinary = new LinearLayout(this);
        ordinary.setOrientation(LinearLayout.VERTICAL);
        ordinary.setPadding(dp(20), dp(18), dp(20), dp(18));
        ordinary.setBackgroundResource(com.jeyhun.clock18.R.drawable.panel_bg);
        LinearLayout.LayoutParams ordLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ordLp.setMargins(0, dp(16), 0, 0);
        root.addView(ordinary, ordLp);

        TextView ordinaryLabel = text("ADİ VAXT", 11, muted, true);
        ordinaryLabel.setLetterSpacing(0.16f);
        ordinary.addView(ordinaryLabel);

        realTime = text("00:00:00", 30, txt, true);
        LinearLayout.LayoutParams rtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rtLp.setMargins(0, dp(6), 0, 0);
        ordinary.addView(realTime, rtLp);

        dateText = text("", 13, muted, false);
        ordinary.addView(dateText);

        TextView formula = text("1 dəqiqə = 69.282032302755… adi saniyə\n1 saat = 69.282032302755… şəxsi dəqiqə\n1 gün = 18 şəxsi saat", 13, muted, false);
        formula.setLineSpacing(0, 1.35f);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.setMargins(dp(2), dp(22), dp(2), 0);
        root.addView(formula, fLp);

        setContentView(root);
    }

    private void render() {
        Clock18.Parts p = Clock18.now();
        customTime.setText(Clock18.hhmmss(p));
        exactTime.setText(String.format(Locale.US, "Saat %d  •  Dəqiqə %.3f", p.hour, p.minuteFloat));

        Date now = new Date();
        realTime.setText(new SimpleDateFormat("HH:mm:ss", Locale.US).format(now));
        dateText.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(now));
    }
}
