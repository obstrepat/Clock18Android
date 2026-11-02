package com.focusguard.local;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FocusService extends AccessibilityService {
    private RuleStore store;
    private List<RuleStore.Trigger> triggers;
    private long triggerVersion=-1;
    private WindowManager windowManager;
    private View overlay;
    private final Map<Long,Long> lastShown=new HashMap<>();

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        store=new RuleStore(this);
        windowManager=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        triggerVersion=-1;
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event==null || event.getEventType()!=AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || overlay!=null)return;
        if(event.getPackageName()==null || getPackageName().contentEquals(event.getPackageName()))return;
        if(!getSharedPreferences("state",MODE_PRIVATE).getBoolean("accepted",false))return;
        if(getSharedPreferences("state",MODE_PRIVATE).getBoolean("paused",false))return;
        AccessibilityNodeInfo node=event.getSource();
        if(node==null)return;
        try {
            if(node.isPassword())return;
            int input=node.getInputType();
            int variation=input&InputType.TYPE_MASK_VARIATION;
            if(variation==InputType.TYPE_TEXT_VARIATION_PASSWORD ||
               variation==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
               variation==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
               (input&InputType.TYPE_MASK_CLASS)==InputType.TYPE_CLASS_NUMBER &&
               variation==InputType.TYPE_NUMBER_VARIATION_PASSWORD)return;
            CharSequence raw=node.getText();
            if(raw==null || raw.length()==0)return;
            if(store==null)store=new RuleStore(this);
            long ver=store.version();
            if(ver!=triggerVersion || triggers==null) {
                triggers=store.active();
                triggerVersion=ver;
            }
            String text=RuleStore.normal(raw.toString());
            if(text.isEmpty())return;
            long now=SystemClock.elapsedRealtime();
            for(RuleStore.Trigger t:triggers) {
                if(t.phrase.length()>0 && text.contains(t.phrase)) {
                    long prev=lastShown.containsKey(t.ruleId)?lastShown.get(t.ruleId):0;
                    if(now-prev>20000 || prev==0) {
                        lastShown.put(t.ruleId,now);
                        showWarning(t.message);
                    }
                    break;
                }
            }
        } catch(Exception ignored) {
            // Some applications restrict accessibility access to their input fields.
        } finally {node.recycle();}
    }
    private int dp(int d){return (int)(d*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable background(int color,int radius) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);g.setCornerRadius(dp(radius));return g;
    }
    private TextView txt(String text,int size,int color,boolean bold) {
        TextView t=new TextView(this);
        t.setText(text);t.setTextColor(color);t.setTextSize(size);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private void showWarning(String message) {
        if(windowManager==null)windowManager=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout outer=new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(24),dp(45),dp(24),dp(45));
        outer.setBackgroundColor(Color.rgb(12,18,28));

        TextView mark=txt("●  FOKUS XƏBƏRDARLIĞI",14,Color.rgb(250,99,103),true);
        mark.setGravity(Gravity.CENTER);
        outer.addView(mark,new LinearLayout.LayoutParams(-1,dp(60)));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(false);
        LinearLayout inner=new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        inner.setPadding(dp(16),dp(24),dp(16),dp(24));
        TextView title=txt("DAYAN!",38,Color.WHITE,true);
        title.setGravity(Gravity.CENTER);
        inner.addView(title);
        TextView msg=txt(message,22,Color.rgb(230,235,242),false);
        msg.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);
        mp.topMargin=dp(24);
        inner.addView(msg,mp);
        scroll.addView(inner);
        outer.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        Button close=new Button(this);
        close.setAllCaps(false);close.setText("Bağla və davam et");
        close.setTextColor(Color.WHITE);close.setTextSize(17);
        close.setBackground(background(Color.rgb(224,66,82),18));
        close.setOnClickListener(v->hideWarning());
        outer.addView(close,new LinearLayout.LayoutParams(-1,dp(62)));

        WindowManager.LayoutParams params=new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        try {windowManager.addView(outer,params);overlay=outer;}
        catch(Exception ignored) {overlay=null;}
    }
    private void hideWarning() {
        if(overlay!=null && windowManager!=null) {
            try {windowManager.removeView(overlay);}catch(Exception ignored){}
            overlay=null;
        }
    }
    @Override public void onInterrupt() {hideWarning();}
    @Override public void onDestroy() {
        hideWarning();if(store!=null)store.close();super.onDestroy();
    }
}
