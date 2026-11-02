package com.focusguard.local;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class MainActivity extends Activity {
    private RuleStore store;
    private LinearLayout body;
    private final int BASE=Color.rgb(11,17,27);
    private final int CARD=Color.rgb(24,33,47);
    private final int WHITE=Color.rgb(245,247,251);
    private final int MUTE=Color.rgb(169,184,202);
    private final int ACCENT=Color.rgb(93,211,164);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BASE);
        getWindow().setNavigationBarColor(BASE);
        store=new RuleStore(this);
        render();
    }
    @Override protected void onResume(){super.onResume();if(store!=null)render();}
    private int dp(int px){return (int)(px*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable bg(int color,int radius) {
        GradientDrawable gd=new GradientDrawable();
        gd.setColor(color);gd.setCornerRadius(dp(radius));return gd;
    }
    private TextView text(String s,int size,int color,boolean bold) {
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private LinearLayout column(){
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;
    }
    private Button button(String s,int color) {
        Button b=new Button(this);
        b.setAllCaps(false);b.setText(s);b.setTextSize(15);b.setTextColor(color==ACCENT?BASE:WHITE);
        b.setBackground(bg(color,15));return b;
    }
    private void gap(LinearLayout container,int size) {
        container.addView(new View(this),new LinearLayout.LayoutParams(1,dp(size)));
    }
    private void render() {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BASE);
        body=column();body.setPadding(dp(20),dp(30),dp(20),dp(36));
        scroll.addView(body);setContentView(scroll);

        TextView tag=text("ŞƏXSİ MƏHSULDARLIQ",12,ACCENT,true);
        body.addView(tag);
        gap(body,9);
        body.addView(text("Fokus Qalxanı",31,WHITE,true));
        gap(body,6);
        body.addView(text("Sözlərini seç. Özünə xatırlatma yarat. Klaviaturanı dəyişmə.",15,MUTE,false));
        gap(body,23);

        LinearLayout explain=column();explain.setPadding(dp(18),dp(18),dp(18),dp(18));
        explain.setBackground(bg(CARD,20));
        explain.addView(text("NECƏ İŞLƏYİR?",12,ACCENT,true));
        gap(explain,9);
        explain.addView(text("Seçdiyin söz və ya ifadə başqa tətbiqdə yazılanda ekranı xəbərdarlıq örtəcək. Bəzi tətbiqlərdə aşkarlama Android məhdudiyyətlərinə görə işləməyə bilər.",14,WHITE,false));
        gap(explain,12);
        explain.addView(text("Məxfilik: yazdıqların internetə ötürülmür və saxlanılmır; yalnız sənin yaratdığın ifadələrlə telefonda müqayisə olunur. Parol sahələri yoxlanılmır.",12,MUTE,false));
        body.addView(explain);
        gap(body,17);

        boolean enabled=isServiceEnabled();
        body.addView(text(enabled?"● Xidmət aktivdir":"○ Xidmət aktiv deyil",15,enabled?ACCENT:MUTE,true));
        gap(body,10);
        Button activate=button(enabled?"Android əlçatanlıq parametrləri":"Xidməti aktivləşdir",ACCENT);
        activate.setOnClickListener(v->requestAccess());
        body.addView(activate,new LinearLayout.LayoutParams(-1,dp(54)));
        gap(body,10);

        boolean paused=getSharedPreferences("state",MODE_PRIVATE).getBoolean("paused",false);
        Button pause=button(paused?"Xəbərdarlıqları davam etdir":"Xəbərdarlıqları dayandır",CARD);
        pause.setOnClickListener(v->{
            getSharedPreferences("state",MODE_PRIVATE).edit().putBoolean("paused",!paused).apply();
            render();
        });
        body.addView(pause,new LinearLayout.LayoutParams(-1,dp(52)));
        gap(body,22);

        TextView subtitle=text("Xəbərdarlıqlar",22,WHITE,true);
        body.addView(subtitle);
        gap(body,12);
        Button add=button("+ Yeni xəbərdarlıq yarat",ACCENT);
        add.setOnClickListener(v->edit(null));
        body.addView(add,new LinearLayout.LayoutParams(-1,dp(55)));
        gap(body,16);
        List<RuleStore.Rule> rules=store.all();
        if(rules.isEmpty()) {
            TextView empty=text("Hələ xəbərdarlıq yoxdur. İlk xəbərdarlığını yarat və açar ifadələri yeni sətirlərdə yaz.",14,MUTE,false);
            empty.setPadding(dp(12),dp(14),dp(12),dp(14));
            body.addView(empty);
        }
        for(RuleStore.Rule r:rules){
            LinearLayout card=column();
            card.setPadding(dp(16),dp(16),dp(16),dp(16));
            card.setBackground(bg(CARD,17));
            card.addView(text(r.title,19,WHITE,true));
            gap(card,8);
            card.addView(text(r.message,14,MUTE,false));
            gap(card,10);
            card.addView(text(r.phrases.size()+" açar ifadə",12,ACCENT,true));
            gap(card,8);
            CheckBox check=new CheckBox(this);
            check.setText("Aktiv");check.setTextColor(WHITE);
            check.setChecked(r.enabled);
            check.setOnCheckedChangeListener((v,isChecked)->store.setEnabled(r.id,isChecked));
            card.addView(check);
            LinearLayout actions=new LinearLayout(this);
            Button edit=button("Dəyiş",Color.rgb(48,64,85));
            Button del=button("Sil",Color.rgb(103,51,61));
            edit.setOnClickListener(v->edit(r));
            del.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("Xəbərdarlıq silinsin?")
                .setMessage(r.title)
                .setNegativeButton("İmtina",null)
                .setPositiveButton("Sil",(d,w)->{store.delete(r.id);render();}).show());
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);
            p.rightMargin=dp(6);actions.addView(edit,p);
            LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(0,dp(46),1);
            p2.leftMargin=dp(6);actions.addView(del,p2);
            card.addView(actions);
            body.addView(card);
            gap(body,13);
        }
        body.addView(text("Fokus Qalxanı 1.0 · Bütün qaydalar telefonda saxlanılır.",12,MUTE,false));
    }
    private boolean isServiceEnabled() {
        String services=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return services!=null && services.contains(getPackageName()) && services.contains("FocusService");
    }
    private void requestAccess() {
        if(!getSharedPreferences("state",MODE_PRIVATE).getBoolean("accepted",false)) {
            new AlertDialog.Builder(this)
                .setTitle("Xüsusi giriş icazəsi")
                .setMessage("Fokus Qalxanı sənin seçdiyin ifadələri müəyyən etmək üçün digər tətbiqlərin mətn sahələrindəki dəyişiklikləri oxuya bilər. Bu, şəxsi yazışmalardakı mətnləri də əhatə edə bilər. Mətn yalnız telefonda seçdiyin açar ifadələrlə müqayisə edilir, saxlanılmır və heç yerə göndərilmir. Parol sahələri istisnadır. Android bəzi tətbiqlərdə oxumağa icazə verməyə bilər. Davam etmək üçün bu məlumatların işlənməsinə razılıq verməlisən.")
                .setNegativeButton("Razı deyiləm",null)
                .setPositiveButton("Razıyam",(d,w)->{
                    getSharedPreferences("state",MODE_PRIVATE).edit().putBoolean("accepted",true).apply();
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                }).show();
        } else startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }
    private EditText input(String hint,String value,int minLines) {
        EditText e=new EditText(this);
        e.setHint(hint);e.setHintTextColor(Color.rgb(125,140,160));
        e.setTextColor(Color.BLACK);e.setTextSize(16);
        e.setText(value);
        e.setSingleLine(minLines==1);
        if(minLines>1) {
            e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            e.setMinLines(minLines);e.setGravity(Gravity.TOP|Gravity.START);
        }
        return e;
    }
    private void edit(RuleStore.Rule rule) {
        boolean creating=rule==null;
        LinearLayout box=column();box.setPadding(dp(20),dp(8),dp(20),dp(10));
        box.addView(text("Ad",14,Color.DKGRAY,true));
        EditText name=input("Məsələn: Vaxt itkisi",creating?"":rule.title,1);
        box.addView(name);
        gap(box,14);
        box.addView(text("Açar söz və ifadələr (hər sətirdə bir ifadə)",14,Color.DKGRAY,true));
        EditText keys=input("Darıxıram\nNə edirsən?\nBoş vaxtım var",
                creating?"":android.text.TextUtils.join("\n",rule.phrases),4);
        box.addView(keys);
        gap(box,14);
        box.addView(text("Tam ekranda göstəriləcək xəbərdarlıq",14,Color.DKGRAY,true));
        EditText message=input("Vaxtını dəyərli istifadə et!",creating?"":rule.message,4);
        box.addView(message);
        ScrollView sc=new ScrollView(this);
        sc.addView(box);
        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle(creating?"Yeni xəbərdarlıq":"Xəbərdarlığı dəyiş")
                .setView(sc)
                .setNegativeButton("İmtina",null)
                .setPositiveButton("Yadda saxla",null)
                .create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String n=name.getText().toString().trim();
            String m=message.getText().toString().trim();
            String k=keys.getText().toString();
            boolean hasKey=false;
            for(String line:k.split("\\r?\\n"))if(!line.trim().isEmpty()){hasKey=true;break;}
            if(n.isEmpty()||m.isEmpty()||!hasKey) {
                Toast.makeText(this,"Ad, açar ifadə və xəbərdarlıq mətni tələb olunur.",Toast.LENGTH_LONG).show();
                return;
            }
            store.save(creating?0:rule.id,n,m,k);
            dialog.dismiss();render();
        }));
        dialog.show();
    }
    @Override protected void onDestroy(){if(store!=null)store.close();super.onDestroy();}
}
