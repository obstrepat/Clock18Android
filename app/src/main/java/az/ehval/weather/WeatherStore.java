package az.ehval.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

final class WeatherStore {
 static String kind(Context c){return Words.p(c).getString("weatherKind","cloud");}
 static String temperature(Context c){
   SharedPreferences p=Words.p(c);
   return p.contains("temp")?String.format(Locale.US,"%.0f°", p.getFloat("temp",0)):"";
 }
 static void refresh(Context c,boolean force,Runnable callback){
   Context app=c.getApplicationContext();
   long last=Words.p(app).getLong("weatherFetch",0);
   if(!force && System.currentTimeMillis()-last<30*60*1000){if(callback!=null)callback.run();return;}
   new Thread(()->{
     HttpURLConnection con=null;
     try {
       SharedPreferences p=Words.p(app);
       double lat=Double.longBitsToDouble(p.getLong("lat",Double.doubleToLongBits(40.4093)));
       double lon=Double.longBitsToDouble(p.getLong("lon",Double.doubleToLongBits(49.8671)));
       URL url=new URL(String.format(Locale.US,"https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%.4f&lon=%.4f",lat,lon));
       con=(HttpURLConnection)url.openConnection();
       con.setRequestProperty("User-Agent","EhvalHavasiAndroid/0.1 (+https://github.com/obstrepat/Clock18Android)");
       con.setRequestProperty("Accept","application/json");
       con.setConnectTimeout(12000);con.setReadTimeout(12000);
       int code=con.getResponseCode();
       if(code==200){
         ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=con.getInputStream()){byte[] bytes=new byte[4096];int k;while((k=in.read(bytes))!=-1)out.write(bytes,0,k);}
         JSONObject root=new JSONObject(out.toString("UTF-8"));
         JSONArray time=root.getJSONObject("properties").getJSONArray("timeseries");
         JSONObject now=time.getJSONObject(0).getJSONObject("data");
         float temp=(float)now.getJSONObject("instant").getJSONObject("details").getDouble("air_temperature");
         JSONObject next=now.optJSONObject("next_1_hours");
         if(next==null)next=now.optJSONObject("next_6_hours");
         String sym=next==null?"cloudy":next.getJSONObject("summary").optString("symbol_code","cloudy");
         String kind=decode(sym);
         p.edit().putString("weatherKind",kind).putFloat("temp",temp).putLong("weatherFetch",System.currentTimeMillis()).apply();
       } else if(code==304){p.edit().putLong("weatherFetch",System.currentTimeMillis()).apply();}
     }catch(Exception ignored){}finally{if(con!=null)con.disconnect();}
     new Handler(Looper.getMainLooper()).post(()->{WeatherWidget.updateAll(app); if(callback!=null)callback.run();});
   },"MET-forecast").start();
 }
 static String decode(String code){
   String c=code.toLowerCase(Locale.ROOT);
   if(c.contains("thunder"))return "storm";
   if(c.contains("snow")||c.contains("sleet"))return "snow";
   if(c.contains("rain")||c.contains("drizzle"))return "rain";
   if(c.contains("fog"))return "fog";
   if(c.startsWith("partlycloudy"))return "partly";
   if(c.startsWith("cloudy"))return "cloud";
   if(c.contains("clearsky")||c.contains("fair"))return c.contains("_night")?"night":"sun";
   return "cloud";
 }
}
