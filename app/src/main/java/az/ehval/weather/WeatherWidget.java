package az.ehval.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WeatherWidget extends AppWidgetProvider {
  private static final String NEXT="az.ehval.weather.NEXT";
  @Override public void onReceive(Context c,Intent intent){
    super.onReceive(c,intent);
    if(NEXT.equals(intent.getAction())){Words.next(c);updateAll(c);}
  }
  @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids){
    for(int id:ids)update(c,manager,id);
    WeatherStore.refresh(c,false,null);
  }
  static void updateAll(Context c){
    AppWidgetManager am=AppWidgetManager.getInstance(c);
    ComponentName widget=new ComponentName(c,WeatherWidget.class);
    for(int id:am.getAppWidgetIds(widget))update(c,am,id);
  }
  static void update(Context c,AppWidgetManager m,int id){
    RemoteViews view=new RemoteViews(c.getPackageName(),R.layout.widget);
    String kind=WeatherStore.kind(c);
    view.setTextViewText(R.id.greeting,Words.greeting(c));
    view.setTextViewText(R.id.message,Words.phrase(c));
    view.setTextViewText(R.id.hint,Words.touch(c,kind));
    view.setImageViewBitmap(R.id.weather_image,CharacterArt.make(kind,280,220));
    Intent intent=new Intent(c,WeatherWidget.class).setAction(NEXT);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
    PendingIntent tap=PendingIntent.getBroadcast(c,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    view.setOnClickPendingIntent(R.id.weather_image,tap);
    m.updateAppWidget(id,view);
  }
}
