package az.ehval.weather;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class Words {
  private static JSONObject json;
  private static final Random RANDOM = new Random();
  static SharedPreferences p(Context c){ return c.getSharedPreferences("ehval", Context.MODE_PRIVATE); }
  static synchronized JSONObject data(Context c) {
    if(json==null) try(InputStream in=c.getAssets().open("messages.json")){
      byte[] b=new byte[in.available()]; int offset=0; int count;
      while(offset<b.length && (count=in.read(b,offset,b.length-offset))!=-1)offset+=count;
      json=new JSONObject(new String(b,StandardCharsets.UTF_8));
    } catch(Exception e){ json=new JSONObject(); }
    return json;
  }
  static String language(Context c) {
    String lang=p(c).getString("language","");
    if(lang.isEmpty())lang=Locale.getDefault().getLanguage();
    if(!data(c).optJSONObject("phrases").has(lang))lang="en";
    return lang;
  }
  static String[] choices(Context c) {
    JSONArray a=data(c).optJSONArray("languages");
    if(a==null)return new String[]{"az","en","ru","tr","es","fr","de"};
    String[] arr=new String[a.length()];
    for(int i=0;i<arr.length;i++)arr[i]=a.optString(i);
    return arr;
  }
  static String phrase(Context c) {
    JSONObject p=data(c).optJSONObject("phrases"); if(p==null)return "";
    JSONArray a=p.optJSONArray(language(c)); if(a==null)return "";
    int i=Math.max(0,Math.min(a.length()-1,Words.p(c).getInt("messageIndex",0)));
    return a.optString(i);
  }
  static void next(Context c) {
    int old=p(c).getInt("messageIndex",-1), x;
    do{x=RANDOM.nextInt(18);}while(x==old);
    p(c).edit().putInt("messageIndex",x).apply();
  }
  static String greeting(Context c) {
    int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int i=h<5||h>=22?0:h<12?1:h<17?2:3;
    JSONObject label=data(c).optJSONObject("labels").optJSONObject(language(c));
    return label==null?"Good morning":label.optJSONArray("g").optString(i);
  }
  static String touch(Context c, String kind) {
    String[] kinds={"sun","partly","cloud","rain","snow","night","storm","fog"};
    int i=0;for(int j=0;j<kinds.length;j++)if(kinds[j].equals(kind))i=j;
    JSONObject label=data(c).optJSONObject("labels").optJSONObject(language(c));
    return label==null?"Tap":label.optJSONArray("tap").optString(i);
  }
  static String displayLanguage(String code) {
    return new Locale(code).getDisplayLanguage(new Locale(code))+" · "+code;
  }
}
