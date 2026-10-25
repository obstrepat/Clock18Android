package az.ehval.weather;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import java.util.Locale;

public class MainActivity extends Activity {
  private TextView hello,phrase,hint,temp;
  private ImageView art;
  private Spinner languages;
  private String[] codes;
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
  @Override public void onCreate(Bundle b){super.onCreate(b);
    LinearLayout root=new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);
    GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(255,242,198),Color.rgb(255,207,177)});
    root.setBackground(bg);root.setPadding(dp(20),dp(26),dp(20),dp(20));setContentView(root);
    languages=new Spinner(this); codes=Words.choices(this);
    String[] names=new String[codes.length];
    for(int i=0;i<codes.length;i++)names[i]=Words.displayLanguage(codes[i]);
    ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names);
    languages.setAdapter(adapter);
    LinearLayout.LayoutParams select=new LinearLayout.LayoutParams(dp(230),dp(48));
    select.gravity=Gravity.END;root.addView(languages,select);
    String preferred=Words.p(this).getString("language",Words.language(this));
    for(int i=0;i<codes.length;i++)if(codes[i].equals(preferred))languages.setSelection(i);
    languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override public void onNothingSelected(AdapterView<?> parent){}
      @Override public void onItemSelected(AdapterView<?> parent,View v,int i,long l){
        Words.p(MainActivity.this).edit().putString("language",codes[i]).apply();
        redraw();WeatherWidget.updateAll(MainActivity.this);
      }
    });
    hello=new TextView(this);hello.setTextColor(Color.rgb(77,48,33));hello.setTextSize(25);hello.setGravity(Gravity.CENTER);hello.setTypeface(null,1);
    root.addView(hello,new LinearLayout.LayoutParams(-1,dp(54)));
    temp=new TextView(this);temp.setTextColor(Color.rgb(113,75,48));temp.setTextSize(18);temp.setGravity(Gravity.CENTER);
    root.addView(temp,new LinearLayout.LayoutParams(-1,dp(28)));
    art=new ImageView(this);
    LinearLayout.LayoutParams imageParams=new LinearLayout.LayoutParams(-1,0,1);root.addView(art,imageParams);
    art.setOnClickListener(v->{Words.next(this);redraw();WeatherWidget.updateAll(this);});
    phrase=new TextView(this);phrase.setTextColor(Color.rgb(75,49,32));phrase.setTextSize(20);phrase.setGravity(Gravity.CENTER);
    LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(-1,dp(154));root.addView(phrase,q);
    hint=new TextView(this);hint.setTextColor(Color.rgb(130,90,68));hint.setTextSize(15);hint.setGravity(Gravity.CENTER);
    root.addView(hint,new LinearLayout.LayoutParams(-1,dp(36)));
    TextView location=new TextView(this);location.setText("⌖");location.setTextSize(30);location.setTextColor(Color.rgb(85,60,44));location.setGravity(Gravity.CENTER);
    root.addView(location,new LinearLayout.LayoutParams(dp(56),dp(50)));
    location.setOnClickListener(v->locate());
    redraw();WeatherStore.refresh(this,true,this::redraw);
  }
  private void redraw(){
    if(hello==null)return;
    hello.setText(Words.greeting(this));phrase.setText(Words.phrase(this));
    String kind=WeatherStore.kind(this);hint.setText(Words.touch(this,kind));
    temp.setText(WeatherStore.temperature(this));
    art.setImageBitmap(CharacterArt.make(kind,580,500));
  }
  private void locate(){
    if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},101);return;
    }
    try{
      LocationManager manager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
      Location last=null;
      if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))last=manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
      if(last==null&&manager.isProviderEnabled(LocationManager.GPS_PROVIDER))last=manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if(last!=null){setLocation(last);return;}
      String prov=manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)?LocationManager.NETWORK_PROVIDER:LocationManager.GPS_PROVIDER;
      manager.requestSingleUpdate(prov,new LocationListener(){
        @Override public void onLocationChanged(Location l){setLocation(l);}
      },getMainLooper());
    }catch(Exception ignored){}
  }
  private void setLocation(Location l){
    Words.p(this).edit().putLong("lat",Double.doubleToLongBits(l.getLatitude()))
      .putLong("lon",Double.doubleToLongBits(l.getLongitude())).putLong("weatherFetch",0).apply();
    WeatherStore.refresh(this,true,this::redraw);
  }
  @Override public void onRequestPermissionsResult(int request,String[] p,int[] grants){
    super.onRequestPermissionsResult(request,p,grants);
    if(request==101&&grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)locate();
  }
  @Override protected void onResume(){super.onResume();if(hello!=null){redraw();WeatherStore.refresh(this,false,this::redraw);}}
}
