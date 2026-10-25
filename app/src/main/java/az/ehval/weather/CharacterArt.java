package az.ehval.weather;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

final class CharacterArt {
  static int color(String c){return Color.parseColor(c);}
  static Bitmap make(String kind,int width,int height){
    Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
    Canvas raw=new Canvas(b);
    float sc=Math.min(width,height)/240f;
    raw.save();
    raw.translate((width-240*sc)/2f,(height-240*sc)/2f);
    raw.scale(sc,sc);
    Paint p=new Paint(3);
    if(kind.equals("sun")||kind.equals("partly")){
      p.setColor(color("#F4A83B"));p.setStrokeWidth(7);p.setStrokeCap(Paint.Cap.ROUND);
      for(int i=0;i<12;i++){double a=i*Math.PI/6;p.setColor(color("#F4A83B"));raw.drawLine(120+(float)Math.cos(a)*78,110+(float)Math.sin(a)*78,120+(float)Math.cos(a)*99,110+(float)Math.sin(a)*99,p);}
      p.setColor(color("#FFD24B"));raw.drawCircle(120,110,62,p);
      if(kind.equals("sun"))face(raw,120,110);
      else { cloud(raw,128,144); }
    } else if(kind.equals("night")){
      p.setColor(color("#FFE4A0"));raw.drawCircle(120,112,65,p);
      p.setColor(color("#FFF1C9"));raw.drawCircle(146,95,56,p);
      p.setColor(color("#FFE4A0"));raw.drawCircle(120,112,55,p);face(raw,120,112);
    }else{
      cloud(raw,120,120);
      if(kind.equals("rain")){ p.setColor(color("#6FA9E6"));p.setStrokeWidth(8);p.setStrokeCap(Paint.Cap.ROUND);
        for(int i=0;i<3;i++)raw.drawLine(82+i*37,179,73+i*37,197,p);
      }
      if(kind.equals("snow")){p.setColor(color("#79B4E5"));for(int i=0;i<3;i++)raw.drawCircle(78+i*42,190+(i%2)*9,6,p);}
      if(kind.equals("storm")){p.setColor(color("#FFD14D"));Path lightning=new Path();lightning.moveTo(124,166);lightning.lineTo(103,192);lightning.lineTo(124,192);lightning.lineTo(115,218);lightning.lineTo(153,183);lightning.lineTo(132,183);lightning.close();raw.drawPath(lightning,p);}
      if(kind.equals("fog")){p.setColor(color("#9BADBE"));p.setStrokeWidth(5);raw.drawLine(70,192,179,192,p);raw.drawLine(90,208,156,208,p);}
    }
    raw.restore();return b;
  }
  static void cloud(Canvas c,int x,int y){
    Paint p=new Paint(3); p.setColor(color("#E5EEF9"));
    c.drawCircle(x-49,y+15,37,p);c.drawCircle(x-15,y-15,49,p);c.drawCircle(x+34,y-5,44,p);
    c.drawRoundRect(new RectF(x-76,y,x+78,y+53),27,27,p);
    face(c,x,y+14);
  }
  static void face(Canvas c,int x,int y){
    Paint p=new Paint(3);p.setColor(color("#58432F"));
    c.drawOval(new RectF(x-25,y-12,x-17,y+3),p);c.drawOval(new RectF(x+17,y-12,x+25,y+3),p);
    p.setColor(color("#F99791"));c.drawCircle(x-37,y+12,9,p);c.drawCircle(x+37,y+12,9,p);
    p.setColor(color("#58432F"));p.setStrokeWidth(5);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
    Path smile=new Path();smile.moveTo(x-18,y+13);smile.quadTo(x,y+36,x+18,y+13);c.drawPath(smile,p);
  }
}
