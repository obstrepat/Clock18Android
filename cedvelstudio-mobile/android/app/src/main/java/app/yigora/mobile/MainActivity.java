package app.yigora.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.text.InputType;
import java.util.Locale;

/** Native Android client for the user's Windows Yigora server.
 * All financial data and print authorization are controlled by the Windows backend.
 */
public class MainActivity extends Activity {
    private static final int NAVY=Color.rgb(18,18,18), BG=Color.rgb(248,248,248);
    private String server="";
    private WebView web;
    private TextView status;
    private ProgressBar progress;
    private int dp(int n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
    private TextView text(String s,int pt,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(pt);v.setTextColor(color);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        getWindow().setStatusBarColor(NAVY);
        server=getPreferences(MODE_PRIVATE).getString("server","");
        if(server.isEmpty())settings();else workspace();
    }
    private boolean local(String s){
        String h=s.toLowerCase(Locale.ROOT);
        if(h.equals("localhost")||h.endsWith(".local")||h.endsWith(".ts.net"))return true;
        String[] p=h.split("\\.");if(p.length!=4)return false;
        try {int[] x=new int[4];for(int i=0;i<4;i++){x[i]=Integer.parseInt(p[i]);if(x[i]<0||x[i]>255)return false;}
            return x[0]==10 || x[0]==127 || (x[0]==192&&x[1]==168) || (x[0]==172&&x[1]>=16&&x[1]<=31) || (x[0]==100&&x[1]>=64&&x[1]<=127);
        }catch(NumberFormatException e){return false;}
    }
    private String valid(String input){
        String s=input.trim();Uri u=Uri.parse(s);String scheme=u.getScheme(),host=u.getHost();
        if(scheme==null||host==null||(!scheme.equalsIgnoreCase("http")&&!scheme.equalsIgnoreCase("https")))return null;
        if((u.getPath()!=null&&!u.getPath().isEmpty()&&!u.getPath().equals("/"))||u.getQuery()!=null||u.getFragment()!=null||u.getPort()==0)return null;
        if(scheme.equalsIgnoreCase("http")&&!local(host))return null;
        return s.replaceAll("/+$","");
    }
    private void dispose(){if(web!=null){web.destroy();web=null;}}
    private void settings(){dispose();LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(24),dp(42),dp(24),dp(20));c.setBackgroundColor(BG);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.app_icon);c.addView(logo,new LinearLayout.LayoutParams(dp(72),dp(72)));
        TextView h=text("Yigora",27,NAVY);h.setPadding(0,0,0,dp(12));c.addView(h);
        TextView sub=text("MOBİL TƏTBİQ  •  SMART PRINT",12,Color.rgb(42,42,42));sub.setPadding(0,0,0,dp(30));c.addView(sub);
        TextView desc=text("Telefon və kompüteri eyni Wi-Fi şəbəkəsinə qoşun. VPN və bulud xidməti lazım deyil.",16,NAVY);
        desc.setPadding(0,0,0,dp(22));c.addView(desc);
        c.addView(text("Kompüterin ünvanı",15,NAVY));
        EditText url=new EditText(this);url.setText(server);url.setSingleLine(true);url.setHint("http://192.168.1.100:8765");url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);c.addView(url);
        TextView note=text("Kompüterinizin Wi-Fi IP ünvanını yazın. Məsələn: http://192.168.1.100:8765",12,Color.rgb(84,100,116));note.setPadding(0,dp(12),0,dp(22));c.addView(note);
        Button go=button("Qoşul →");c.addView(go);go.setOnClickListener(v->{String u=valid(url.getText().toString());if(u==null){url.setError("Düzgün Wi-Fi IP ünvanı daxil edin");return;}server=u;getPreferences(MODE_PRIVATE).edit().putString("server",u).apply();workspace();});
        TextView help=text("İlk giriş: kompüterdə «Cihazlar və icazələr» → «Telefon üçün kod yarat». Sonra telefonda «Telefonu qoş» seçərək kodu daxil edin. İdarəçi girişi yalnız Windows kompüterində mümkündür.",13,Color.rgb(84,100,116));help.setPadding(0,dp(24),0,0);c.addView(help);setContentView(c);
    }
    private boolean allowed(Uri u){Uri base=Uri.parse(server);return base.getScheme().equalsIgnoreCase(u.getScheme())&&base.getHost().equalsIgnoreCase(u.getHost())&&base.getPort()==u.getPort();}
    private void workspace(){dispose();LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setBackgroundColor(Color.WHITE);
        LinearLayout head=new LinearLayout(this);head.setPadding(dp(10),dp(5),dp(8),dp(5));head.setBackgroundColor(NAVY);
        TextView title=text("Yigora",18,Color.WHITE);title.setGravity(android.view.Gravity.CENTER_VERTICAL);head.addView(title,new LinearLayout.LayoutParams(0,dp(46),1));
        Button reload=button("Yenilə");head.addView(reload,new LinearLayout.LayoutParams(dp(82),dp(46)));
        Button edit=button("Ünvan");head.addView(edit,new LinearLayout.LayoutParams(dp(80),dp(46)));c.addView(head);
        status=text("Qoşulur...",12,Color.rgb(92,105,119));status.setPadding(dp(10),dp(6),dp(10),dp(6));c.addView(status);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setIndeterminate(true);c.addView(progress,new LinearLayout.LayoutParams(-1,dp(3)));
        web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView w,int p){if(progress!=null)progress.setVisibility(p<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest req){return !allowed(req.getUrl());}
            @Override public void onPageFinished(WebView w,String u){if(status!=null){status.setText("Bağlıdır • "+server);status.setTextColor(Color.rgb(23,122,84));}}
            @Override public void onReceivedError(WebView w,WebResourceRequest req,WebResourceError e){if(req.isForMainFrame()&&status!=null){status.setText("Bağlantı yoxdur — eyni Wi-Fi və kompüteri yoxlayın");status.setTextColor(Color.RED);}}
        });
        c.addView(web,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));setContentView(c);
        reload.setOnClickListener(v->web.reload());
        edit.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Server ünvanı").setMessage("Başqa kompüterə qoşulmaq istəyirsiniz?").setNegativeButton("İmtina",(d,x)->{}).setPositiveButton("Dəyiş",(d,x)->settings()).show());
        web.loadUrl(server+"/");
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onDestroy(){dispose();super.onDestroy();}
}