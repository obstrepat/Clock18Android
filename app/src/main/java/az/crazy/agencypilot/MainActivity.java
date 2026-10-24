package az.crazy.agencypilot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(247,248,253), WHITE = Color.WHITE, INK = Color.rgb(28,34,57);
    private static final int MUTED = Color.rgb(112,124,153), PURPLE = Color.rgb(103,82,244), LIGHT = Color.rgb(237,234,255);
    private static final int GREEN = Color.rgb(1,154,137), BORDER = Color.rgb(230,234,244), RED = Color.rgb(219,76,87);
    private SharedPreferences prefs;
    private JSONObject store;
    private LinearLayout root, page;
    private String screen="home", selectedId="", plannerType="Strategiya";
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs=getSharedPreferences("agency_pilot_v1", MODE_PRIVATE);
        try { store=new JSONObject(prefs.getString("store", "{}")); } catch(Exception e) { store=new JSONObject(); }
        if(store.optJSONArray("clients")==null) put("clients",new JSONArray());
        if(store.optJSONArray("campaigns")==null) put("campaigns",new JSONArray());
        if(store.optJSONArray("outputs")==null) put("outputs",new JSONArray());
        selectedId=store.optString("selectedId","");
        render();
    }
    private void put(String k,Object v){try{store.put(k,v);}catch(Exception ignored){}}
    private void persist(){put("selectedId",selectedId);prefs.edit().putString("store",store.toString()).apply();}
    private int d(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,int r){GradientDrawable gd=new GradientDrawable();gd.setColor(c);gd.setCornerRadius(d(r));return gd;}
    private GradientDrawable stroke(int c,int r,int line){GradientDrawable gd=bg(c,r);gd.setStroke(d(1),line);return gd;}
    private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void pad(View v,int x,int y){v.setPadding(d(x),d(y),d(x),d(y));}
    private TextView text(String s,int sp,int c,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextColor(c);t.setTextSize(sp);t.setLineSpacing(d(2),1.08f);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private Button button(String label,boolean primary){
        Button b=new Button(this);b.setText(label);b.setTextSize(13);b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setMinHeight(d(45));b.setMinimumHeight(d(45));
        b.setTextColor(primary?WHITE:INK);b.setBackground(bg(primary?PURPLE:LIGHT,13));pad(b,14,4);return b;
    }
    private LinearLayout card(){LinearLayout c=vertical();pad(c,16,16);c.setBackground(stroke(WHITE,20,BORDER));return c;}
    private void space(LinearLayout host,int h){View v=new View(this);host.addView(v,new LinearLayout.LayoutParams(1,d(h)));}
    private void addCard(LinearLayout c){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=d(12);page.addView(c,p);}
    private void label(LinearLayout v,String title,String caption){
        v.addView(text(title,16,INK,true));
        if(caption!=null){space(v,5);v.addView(text(caption,12,MUTED,false));}
    }
    private void section(String title){TextView t=text(title,19,INK,true);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=d(12);p.bottomMargin=d(12);page.addView(t,p);}
    private void render(){
        root=vertical();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=vertical();pad(header,22,17);header.setBackgroundColor(WHITE);
        TextView brand=text("✦  AGENCY PILOT",13,PURPLE,true);header.addView(brand);
        space(header,6);String current=screen.equals("home")?"İdarəetmə paneli":screen.equals("clients")?"Müştərilər":screen.equals("planner")?"AI Studio":screen.equals("campaigns")?"Kampaniyalar":"Parametrlər";
        header.addView(text(current,25,INK,true));root.addView(header);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=vertical();page.setPadding(d(16),d(16),d(16),d(24));scroll.addView(page);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        switch(screen){case "clients":clientsPage();break;case "planner":plannerPage();break;case "campaigns":campaignsPage();break;case "settings":settingsPage();break;default:homePage();}
        navbar();
    }
    private void navbar(){
        LinearLayout nav=horizontal();nav.setBackground(stroke(WHITE,0,BORDER));nav.setPadding(d(3),d(6),d(3),d(8));
        String[] ids={"home","clients","planner","campaigns","settings"};
        String[] symbols={"▦","♙","✦","◎","⚙"};
        String[] names={"Panel","Müştəri","AI Studio","Reklam","Ayarlar"};
        for(int i=0;i<ids.length;i++){
            final String id=ids[i];
            LinearLayout item=vertical();item.setGravity(Gravity.CENTER);item.setPadding(0,d(5),0,d(5));
            TextView icon=text(symbols[i],22,id.equals(screen)?PURPLE:MUTED,true);icon.setGravity(Gravity.CENTER);item.addView(icon);
            TextView caption=text(names[i],10,id.equals(screen)?PURPLE:MUTED,id.equals(screen));caption.setGravity(Gravity.CENTER);item.addView(caption);
            item.setOnClickListener(v->{screen=id;render();});
            nav.addView(item,new LinearLayout.LayoutParams(0,d(57),1));
        }
        root.addView(nav);
    }
    private JSONArray arr(String key){JSONArray a=store.optJSONArray(key);return a==null?new JSONArray():a;}
    private JSONObject selected(){JSONArray a=arr("clients");for(int i=0;i<a.length();i++){JSONObject c=a.optJSONObject(i);if(c!=null&&selectedId.equals(c.optString("id")))return c;}return null;}
    private String field(JSONObject j,String key){return j==null?"":j.optString(key,"");}
    private String date(){return new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault()).format(new Date());}
    private void metric(LinearLayout host,String value,String name,int accent){
        LinearLayout box=vertical();pad(box,13,15);box.setBackground(stroke(WHITE,16,BORDER));
        box.addView(text(value,23,accent,true));space(box,7);box.addView(text(name,12,MUTED,false));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(d(3),0,d(3),0);host.addView(box,p);
    }
    private void homePage(){
        LinearLayout hero=card();hero.setBackground(bg(PURPLE,20));
        TextView k=text("MÜŞTƏRİLƏRİNİZ. BİR PANEL.",12,Color.rgb(224,220,255),true);hero.addView(k);space(hero,10);
        TextView h=text("Marketinq işlərini daha ağıllı idarə edin.",24,WHITE,true);hero.addView(h);space(hero,9);
        hero.addView(text("Müştərilər, reklam planları və kontent — hamısı bir yerdə.",13,Color.rgb(242,240,255),false));
        space(hero,14);Button b=button("Yeni plan hazırla  →",false);b.setOnClickListener(v->{screen="planner";render();});hero.addView(b);addCard(hero);
        LinearLayout r=horizontal();metric(r,""+arr("clients").length(),"Müştəri",PURPLE);metric(r,""+arr("campaigns").length(),"Kampaniya",GREEN);page.addView(r);space(page,10);
        LinearLayout r2=horizontal();metric(r2,""+arr("outputs").length(),"Hazır sənəd",PURPLE);
        double sum=0;JSONArray a=arr("campaigns");for(int i=0;i<a.length();i++)sum+=a.optJSONObject(i).optDouble("budget",0);
        metric(r2,String.format(Locale.US,"%.0f",sum),"Plan büdcəsi",GREEN);page.addView(r2);
        section("Sürətli əməliyyatlar");
        LinearLayout quick=card();Button add=button("+ Müştəri əlavə et",true);add.setOnClickListener(v->clientDialog());quick.addView(add);
        space(quick,10);Button plan=button("✦ Strategiya hazırla",false);plan.setOnClickListener(v->{screen="planner";render();});quick.addView(plan);
        space(quick,10);Button camp=button("+ Kampaniya qeyd et",false);camp.setOnClickListener(v->campaignDialog());quick.addView(camp);addCard(quick);
        section("Son planlar");JSONArray outs=arr("outputs");
        if(outs.length()==0){empty("Hələ plan yoxdur","AI Studio bölməsində ilk kontent və strategiya sənədinizi yaradın.");}
        for(int i=outs.length()-1;i>=0&&i>=outs.length()-3;i--){JSONObject x=outs.optJSONObject(i);if(x!=null)outputCard(x);}
        LinearLayout notice=card();label(notice,"İş rejimi: lokal","Məlumatlar bu telefonda saxlanılır. Canlı AI cavabları üçün Ayarlar bölməsində uyğun server qoşun.");addCard(notice);
    }
    private void empty(String title,String sub){LinearLayout e=card();label(e,title,sub);addCard(e);}
    private void clientsPage(){
        Button b=button("+ Yeni müştəri",true);b.setOnClickListener(v->clientDialog());page.addView(b);space(page,16);
        JSONArray cs=arr("clients");if(cs.length()==0){empty("Müştəri bazası boşdur","Yeni müştəri əlavə edin və reklam strategiyasını hazırlamağa başlayın.");return;}
        for(int i=0;i<cs.length();i++){
            JSONObject c=cs.optJSONObject(i);if(c==null)continue;String id=field(c,"id");
            LinearLayout cr=card();if(selectedId.equals(id))cr.setBackground(stroke(Color.rgb(250,249,255),18,PURPLE));
            TextView name=text((selectedId.equals(id)?"● ":"○ ")+field(c,"name"),18,INK,true);cr.addView(name);
            space(cr,7);cr.addView(text(field(c,"sector")+" · "+field(c,"region"),13,MUTED,false));
            space(cr,5);cr.addView(text("Aylıq büdcə: "+field(c,"budget"),13,MUTED,false));
            space(cr,12);LinearLayout actions=horizontal();
            Button select=button(selectedId.equals(id)?"Seçilib":"Bu müştərini seç",false);
            select.setOnClickListener(v->{selectedId=id;persist();render();});actions.addView(select,new LinearLayout.LayoutParams(0,d(48),1));
            spaceWidth(actions,8);
            Button del=button("Sil",false);del.setTextColor(RED);del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Müştəri silinsin?")
                .setMessage(field(c,"name")+" siyahıdan silinəcək.").setNegativeButton("Ləğv et",null)
                .setPositiveButton("Sil",(di,w)->{removeById("clients",id);if(id.equals(selectedId))selectedId="";persist();render();}).show());
            actions.addView(del);cr.addView(actions);addCard(cr);
        }
    }
    private void spaceWidth(LinearLayout l,int w){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(d(w),1));}
    private EditText input(String hint,String value){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setHint(hint);e.setText(value);e.setPadding(d(10),d(14),d(10),d(14));return e;}
    private void dialogField(LinearLayout body,String label,EditText e){body.addView(text(label,12,MUTED,true));body.addView(e);space(body,6);}
    private void clientDialog(){
        LinearLayout form=vertical();pad(form,20,10);
        EditText name=input("Məsələn: Caspian Coffee",""),sector=input("Restoran, klinika, mağaza...",""),region=input("Bakı / Azərbaycan","Bakı");
        EditText budget=input("Məsələn: 1000 AZN",""),aud=input("Məqsədli auditoriya","18–45 yaş");
        dialogField(form,"Müştərinin adı",name);dialogField(form,"Sektor",sector);dialogField(form,"Ərazi",region);
        dialogField(form,"Aylıq reklam büdcəsi",budget);dialogField(form,"Auditoriya",aud);
        ScrollView sc=new ScrollView(this);sc.addView(form);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Yeni müştəri").setView(sc).setNegativeButton("Ləğv et",null)
            .setPositiveButton("Yadda saxla",null).create();
        dlg.setOnShowListener(v->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
            if(name.getText().toString().trim().isEmpty()){name.setError("Adı daxil edin");return;}
            JSONObject c=new JSONObject();try{
                c.put("id",UUID.randomUUID().toString());c.put("name",name.getText().toString().trim());
                c.put("sector",sector.getText().toString().trim());c.put("region",region.getText().toString().trim());
                c.put("budget",budget.getText().toString().trim());c.put("audience",aud.getText().toString().trim());
            }catch(Exception ignored){}
            arr("clients").put(c);selectedId=c.optString("id");persist();dlg.dismiss();screen="clients";render();
        }));dlg.show();
    }
    private void plannerPage(){
        JSONObject c=selected();
        LinearLayout chosen=card();label(chosen,c==null?"Müştəri seçilməyib":field(c,"name"),c==null?"Əvvəlcə müştəri əlavə edin.":field(c,"sector")+" · "+field(c,"region"));
        space(chosen,12);Button change=button("Müştərilər →",false);change.setOnClickListener(v->{screen="clients";render();});chosen.addView(change);addCard(chosen);
        section("Nə hazırlayaq?");
        HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips=horizontal();String[] types={"Strategiya","Kontent planı","Reklam mətni","SEO planı","Rəqib təhlili","Hesabat"};
        for(String type:types){
            Button chip=button(type,type.equals(plannerType));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,d(44));p.rightMargin=d(8);chips.addView(chip,p);
            chip.setOnClickListener(v->{plannerType=type;render();});
        }
        scroll.addView(chips);page.addView(scroll);space(page,16);
        LinearLayout box=card();label(box,"✦  "+plannerType,"Müştəri məlumatları əsasında ilkin sənəd yaradın.");
        space(box,16);Button local=button("Lokal şablon yarat",true);local.setOnClickListener(v->{if(selected()==null){toast("Əvvəlcə müştəri əlavə edin");return;}
            saveOutput(localOutput(selected(),plannerType),"Lokal şablon");render();});box.addView(local);
        space(box,10);Button ai=button("Canlı AI ilə hazırla",false);ai.setOnClickListener(v->generateRemote());
        box.addView(ai);space(box,10);box.addView(text("Canlı AI üçün Ayarlar bölməsində HTTPS API endpoint, model və açar lazımdır. Lokal şablon real bazar məlumatı toplamır.",12,MUTED,false));
        addCard(box);
        section("Hazırlanmış sənədlər");
        JSONArray outs=arr("outputs");boolean any=false;
        for(int i=outs.length()-1;i>=0;i--){JSONObject o=outs.optJSONObject(i);if(o!=null&&selectedId.equals(o.optString("clientId"))){outputCard(o);any=true;}}
        if(!any)empty("Sənəd yoxdur","Yuxarıdakı düymələrdən biri ilə ilk sənədi yaradın.");
    }
    private void saveOutput(String s,String source){
        JSONObject c=selected();JSONObject o=new JSONObject();try{o.put("id",UUID.randomUUID().toString());o.put("clientId",selectedId);
            o.put("client",field(c,"name"));o.put("type",plannerType);o.put("text",s);o.put("date",date());o.put("source",source);
        }catch(Exception ignored){}arr("outputs").put(o);persist();toast("Sənəd saxlanıldı");
    }
    private void outputCard(JSONObject x){
        LinearLayout box=card();box.addView(text(x.optString("type")+"  ·  "+x.optString("client"),15,INK,true));space(box,5);
        box.addView(text(x.optString("date")+"  ·  "+x.optString("source"),11,MUTED,false));space(box,8);
        String raw=x.optString("text");box.addView(text(raw.length()>210?raw.substring(0,210)+"…":raw,13,MUTED,false));space(box,10);
        LinearLayout actions=horizontal();Button open=button("Oxu",false);open.setOnClickListener(v->openOutput(x));actions.addView(open,new LinearLayout.LayoutParams(0,d(46),1));
        spaceWidth(actions,8);Button share=button("Paylaş",true);share.setOnClickListener(v->share(x.optString("text")));actions.addView(share,new LinearLayout.LayoutParams(0,d(46),1));box.addView(actions);addCard(box);
    }
    private void openOutput(JSONObject o){
        ScrollView sc=new ScrollView(this);TextView t=text(o.optString("text"),15,INK,false);pad(t,22,18);t.setTextIsSelectable(true);sc.addView(t);
        new AlertDialog.Builder(this).setTitle(o.optString("type")).setView(sc)
            .setNegativeButton("Bağla",null).setPositiveButton("Paylaş",(d,w)->share(o.optString("text"))).show();
    }
    private void share(String s){Intent it=new Intent(Intent.ACTION_SEND);it.setType("text/plain");it.putExtra(Intent.EXTRA_TEXT,s);startActivity(Intent.createChooser(it,"Paylaş"));}
    private String localOutput(JSONObject c,String mode){
        String n=field(c,"name"), sector=field(c,"sector"), region=field(c,"region"), bud=field(c,"budget"),aud=field(c,"audience");
        String header="AGENCY PILOT — "+mode.toUpperCase(Locale.getDefault())+"\nMüştəri: "+n+"\nSahə: "+sector+"\nRegion: "+region+"\nAuditoriya: "+aud+"\nBüdcə: "+bud+"\nTarix: "+date()+"\n\n";
        if(mode.equals("Strategiya"))return header+
            "MƏQSƏD\nPotensial müştərilərin tanınmasını və müraciət sayını artırmaq.\n\n"+
            "KANAL SEÇİMİ\n• Instagram/Facebook: lokal auditoriya və təkrar hədəfləmə.\n• Google Search: satınalma niyyətli sorğular.\n• Veb-sayt: sorğular və konversiyaların ölçülməsi.\n\n"+
            "30 GÜNLÜK PLAN\n1-ci həftə: təklif, auditoriya və izləmə hadisələri.\n2-ci həftə: 3 kreativ və 2 reklam mətni üçün test.\n3-cü həftə: nəticələrin müqayisəsi, zəif kreativlərin yenilənməsi.\n4-cü həftə: nəticə hesabatı və növbəti ay planı.\n\n"+
            "BÜDCƏ NÜMUNƏSİ\n50% əsas kampaniya, 30% testlər, 20% remarketinq. Bu bölgünü faktiki nəticələrə görə dəyişin.\n\n"+
            "ÖLÇÜLMƏ\nCTR, CPC, lead sayı, CPL və satışa dönüşmə.\n\nQeyd: Bu plan avtomatik lokal şablondur, real bazar araşdırması deyil.";
        if(mode.equals("Kontent planı"))return header+
            "1-ci həftə\nB.e.: Brendin təqdimatı\nÇ.a.: Müştərinin problemi və həll yolu\nC.a.: Məhsul/xidmət nümayişi\nŞ.: Story sorğusu\n\n"+
            "2-ci həftə\nB.e.: Faydalı məsləhət\nÇ.a.: Komanda və proseslər\nC.a.: 15 saniyəlik Reels ssenarisi\nŞ.: Suallar və cavablar\n\n"+
            "3-cü həftə\nB.e.: Müştəri rəyi (yalnız icazəli real rəy)\nÇ.a.: Ən çox verilən suallar\nC.a.: Müqayisəli karusel\nŞ.: Story çağırışı\n\n"+
            "4-cü həftə\nB.e.: Yeni təklif\nÇ.a.: Xidmətin üstünlükləri\nC.a.: Sifariş addımları\nŞ.: Ayın yekunu\n\nHər post üçün CTA: əlaqə, mesaj və ya sayt.";
        if(mode.equals("Reklam mətni"))return header+
            "VERSİYA A — TANITIM\n"+n+" ilə "+sector+" sahəsində yeni imkanları kəşf edin. Təkliflərimiz barədə məlumat alın.\nCTA: Ətraflı məlumat\n\n"+
            "VERSİYA B — MÜRACİƏT\n"+region+" bölgəsində sizə uyğun həll axtarırsınız? "+n+" ilə əlaqə saxlayın.\nCTA: Mesaj göndər\n\n"+
            "VERSİYA C — DƏYƏR\nVaxtınıza və ehtiyaclarınıza uyğun seçimlər. "+n+" haqqında daha çox öyrənin.\nCTA: İndi bax\n\n"+
            "Qeyd: Endirim, zəmanət və ya müştəri rəyi təsdiqlənmədən reklama əlavə edilməməlidir.";
        if(mode.equals("SEO planı"))return header+
            "TEXNİKİ AUDİT\n• İndekslənmə, sitemap.xml və robots.txt yoxlanışı.\n• Mobile-first istifadə rahatlığı və Core Web Vitals.\n• HTTPS, canonical və 404 səhifələri.\n\n"+
            "KONTENT\n• "+sector+" üzrə açar söz qrupları.\n• "+region+" üzrə lokal axtarış səhifələri.\n• Suallara cavab verən məqalələr və FAQ.\n\n"+
            "LOKAL SEO\n• Google Business Profile optimallaşdırılması.\n• Əlaqə məlumatlarının ardıcıllığı.\n• Müştəri rəylərinin etik toplanması.\n\n"+
            "KPI: orqanik kliklər, indekslənən səhifələr, form müraciətləri. Bu, canlı sayt auditi deyil.";
        if(mode.equals("Rəqib təhlili"))return header+
            "RƏQİB ARAŞDIRMASI ÜÇÜN ÇƏRÇİVƏ\n1. "+region+" ərazisində eyni xidmətə malik 5 real rəqib seçin.\n2. Təklif, qiymət kommunikasiyası və CTA-ları müqayisə edin.\n3. Orqanik postların tezliyini və formatını qeyd edin.\n4. Axtarış nəticələrində mövcudluğu qiymətləndirin.\n5. Fərqləndirici mesaj və sınaq hipotezləri qurun.\n\n"+
            "MÜQAYİSƏ SÜTUNLARI\nRəqib | Təklif | Güclü tərəf | Zəif tərəf | Fürsət\n\n"+
            "Qeyd: Bu sənəd real rəqib məlumatları toplamır və rəqib adları uydurulmur.";
        return header+"AYLIQ HESABAT ŞABLONU\n\nİcra olunan işlər: [daxil edin]\nReklam xərci: [daxil edin]\nBaxışlar: [daxil edin]\nKliklər: [daxil edin]\nMüraciətlər: [daxil edin]\nSatış: [daxil edin]\n\n"+
            "HESABLANACAQ GÖSTƏRİCİLƏR\nCTR = klik / baxış × 100\nCPC = xərc / klik\nCPL = xərc / müraciət\nROAS = reklamdan gəlir / xərc\n\n"+
            "Növbəti ayın hipotezləri və prioritetləri: [daxil edin]\n\nQeyd: Təsdiqlənməmiş statistik məlumat daxil edilməyib.";
    }
    private void generateRemote(){
        JSONObject c=selected();if(c==null){toast("Əvvəlcə müştəri əlavə edin");return;}
        String endpoint=store.optString("apiUrl","").trim(),model=store.optString("apiModel","").trim();
        if(!endpoint.startsWith("https://")||model.isEmpty()){new AlertDialog.Builder(this).setTitle("AI qoşulmayıb")
            .setMessage("Ayarlar bölməsində HTTPS API ünvanı və model adı daxil edin. API açarını yalnız etibar etdiyiniz server üçün istifadə edin.")
            .setPositiveButton("Ayarları aç",(d,w)->{screen="settings";render();}).setNegativeButton("Bağla",null).show();return;}
        final String type=plannerType;final String id=selectedId;toast("AI sorğusu göndərilir…");
        final String instructions="Sən peşəkar marketinq strategisən. Azərbaycan dilində konkret və praktik "+type+" hazırla. Faktiki araşdırma aparmadan bazar statistikası və rəqib adları uydurma. Reklam hesabına girişin olmadığını nəzərə al.";
        final String user="Müştəri: "+field(c,"name")+"; sektor: "+field(c,"sector")+"; region: "+field(c,"region")+"; auditoriya: "+field(c,"audience")+"; büdcə: "+field(c,"budget")+".";
        new Thread(()->{
            HttpURLConnection con=null;String result=null,err=null;
            try{
                URL url=new URL(endpoint);con=(HttpURLConnection)url.openConnection();con.setConnectTimeout(18000);con.setReadTimeout(60000);
                con.setRequestMethod("POST");con.setDoOutput(true);con.setRequestProperty("Content-Type","application/json; charset=utf-8");
                String key=store.optString("apiKey","").trim();if(!key.isEmpty())con.setRequestProperty("Authorization","Bearer "+key);
                JSONObject body=new JSONObject();body.put("model",model);body.put("temperature",0.5);
                JSONArray messages=new JSONArray();messages.put(new JSONObject().put("role","system").put("content",instructions));
                messages.put(new JSONObject().put("role","user").put("content",user));body.put("messages",messages);
                byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream os=con.getOutputStream()){os.write(bytes);}
                int code=con.getResponseCode();InputStream is=(code>=200&&code<300)?con.getInputStream():con.getErrorStream();
                StringBuilder response=new StringBuilder();try(BufferedReader reader=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){
                    String line;while((line=reader.readLine())!=null){response.append(line);if(response.length()>100000)break;}
                }
                if(code<200||code>=300)throw new Exception("HTTP "+code+": "+response.substring(0,Math.min(160,response.length())));
                JSONObject data=new JSONObject(response.toString());
                result=data.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                if(result==null||result.trim().isEmpty())throw new Exception("AI cavabı boşdur");
            }catch(Exception ex){err=ex.getMessage()==null?"Bilinməyən xəta":ex.getMessage();}
            finally{if(con!=null)con.disconnect();}
            final String good=result,bad=err;
            main.post(()->{
                if(bad!=null){new AlertDialog.Builder(this).setTitle("AI sorğusu alınmadı").setMessage(bad).setPositiveButton("Bağla",null).show();return;}
                if(!id.equals(selectedId))return;plannerType=type;saveOutput(good,"Canlı AI");render();
            });
        }).start();
    }
    private void campaignsPage(){
        Button b=button("+ Kampaniya əlavə et",true);b.setOnClickListener(v->campaignDialog());page.addView(b);space(page,16);
        JSONArray a=arr("campaigns");if(a.length()==0){empty("Kampaniya yoxdur","Google və Meta hesablarına avtomatik qoşulma hələ mövcud deyil. Manual kampaniya məlumatlarını qeyd edə bilərsiniz.");return;}
        for(int i=a.length()-1;i>=0;i--){
            JSONObject camp=a.optJSONObject(i);if(camp==null)continue;LinearLayout cr=card();
            label(cr,camp.optString("name"),camp.optString("channel")+" · "+camp.optString("client"));
            space(cr,10);double budget=camp.optDouble("budget",0),spent=camp.optDouble("spent",0),leads=camp.optDouble("leads",0),clicks=camp.optDouble("clicks",0);
            cr.addView(text("Büdcə: "+money(budget)+"  |  Xərc: "+money(spent),13,MUTED,false));space(cr,6);
            cr.addView(text("Klik: "+(int)clicks+"  |  Müraciət: "+(int)leads,13,MUTED,false));space(cr,6);
            cr.addView(text("CPC: "+(clicks>0?money(spent/clicks):"—")+"   CPL: "+(leads>0?money(spent/leads):"—"),13,GREEN,true));space(cr,10);
            Button del=button("Qeydi sil",false);String cid=camp.optString("id");del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Kampaniya silinsin?")
                .setNegativeButton("Ləğv et",null).setPositiveButton("Sil",(di,w)->{removeById("campaigns",cid);persist();render();}).show());cr.addView(del);addCard(cr);
        }
        empty("Məlumat mənbəyi","Göstəricilər əl ilə daxil edilib, reklam hesablarından avtomatik gətirilməyib.");
    }
    private String money(double x){return String.format(Locale.US,"%.2f",x);}
    private double num(EditText e){try{return Math.max(0,Double.parseDouble(e.getText().toString().trim().replace(',','.')));}catch(Exception ex){return 0;}}
    private void campaignDialog(){
        if(arr("clients").length()==0){toast("Əvvəlcə müştəri yaradın");screen="clients";render();return;}
        JSONObject c=selected();if(c==null){selectedId=arr("clients").optJSONObject(0).optString("id");c=selected();}
        final JSONObject client=c;LinearLayout form=vertical();pad(form,20,10);
        EditText name=input("Kampaniya adı",""),channel=input("Meta / Google / TikTok","Meta");
        EditText budget=input("Büdcə","0"),spent=input("Xərc","0"),clicks=input("Klik","0"),leads=input("Müraciət","0");
        dialogField(form,"Ad",name);dialogField(form,"Kanal",channel);dialogField(form,"Büdcə",budget);
        dialogField(form,"Xərc",spent);dialogField(form,"Klik sayı",clicks);dialogField(form,"Müraciət sayı",leads);
        ScrollView sc=new ScrollView(this);sc.addView(form);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Kampaniya: "+field(client,"name")).setView(sc)
            .setNegativeButton("Ləğv et",null).setPositiveButton("Yadda saxla",null).create();
        dlg.setOnShowListener(v->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
            if(name.getText().toString().trim().isEmpty()){name.setError("Adı daxil edin");return;}
            JSONObject item=new JSONObject();try{
                item.put("id",UUID.randomUUID().toString());item.put("clientId",selectedId);item.put("client",field(client,"name"));
                item.put("name",name.getText().toString().trim());item.put("channel",channel.getText().toString().trim());
                item.put("budget",num(budget));item.put("spent",num(spent));item.put("clicks",num(clicks));item.put("leads",num(leads));
            }catch(Exception ignored){}arr("campaigns").put(item);persist();dlg.dismiss();screen="campaigns";render();
        }));dlg.show();
    }
    private void settingsPage(){
        LinearLayout info=card();label(info,"AI serveri","OpenAI uyğun /chat/completions protokolu ilə işləyən öz HTTPS serverinizdən istifadə edin.");addCard(info);
        LinearLayout form=card();
        EditText url=input("https://server.example/v1/chat/completions",store.optString("apiUrl",""));
        EditText model=input("Model adı (məsələn: qwen3)",store.optString("apiModel",""));
        EditText key=input("API açarı (istəyə bağlı)",store.optString("apiKey",""));key.setInputType(129);
        dialogField(form,"API endpoint (HTTPS)",url);dialogField(form,"Model",model);dialogField(form,"API açarı",key);
        Button save=button("Bağlantını yadda saxla",true);
        save.setOnClickListener(v->{String end=url.getText().toString().trim();
            if(!end.isEmpty()&&!end.startsWith("https://")){url.setError("HTTPS ünvanı istifadə edin");return;}
            put("apiUrl",end);put("apiModel",model.getText().toString().trim());put("apiKey",key.getText().toString().trim());persist();toast("Ayarlar saxlanıldı");});
        form.addView(save);addCard(form);
        LinearLayout policy=card();label(policy,"Məlumat və məxfilik","Müştəri və kampaniya qeydləri lokal olaraq cihazda saxlanılır. AI sorğusu zamanı müştərinin profili konfiqurasiya edilmiş xarici serverə göndərilir. API açarı cihaz yaddaşında saxlanılır; həssas hesab üçün ayrıca təhlükəsiz serverdən istifadə edin.");
        space(policy,10);policy.addView(text("Versiya 1.0 · Android 8+",12,MUTED,false));addCard(policy);
    }
    private void removeById(String key,String id){JSONArray src=arr(key),dst=new JSONArray();for(int i=0;i<src.length();i++){
        JSONObject x=src.optJSONObject(i);if(x!=null&&!id.equals(x.optString("id")))dst.put(x);
    }put(key,dst);}
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
}
