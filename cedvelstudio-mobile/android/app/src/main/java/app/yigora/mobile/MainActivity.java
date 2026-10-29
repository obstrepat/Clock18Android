package app.yigora.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;
import java.util.Locale;

/** Yigora Cloud launcher.
 * Google OAuth is completed by a supported external browser, not embedded WebView.
 * Firestore security rules and the desktop agent enforce data and print permissions.
 */
public class MainActivity extends Activity {
  private final int DARK=Color.rgb(20,20,20), BG=Color.rgb(250,250,250);
  private int dp(int x){return (int)(getResources().getDisplayMetrics().density*x+.5f);}
  private TextView text(String s,int sz){TextView v=new TextView(this);v.setText(s);v.setTextSize(sz);v.setTextColor(DARK);return v;}
  private String valid(String input){
    try{
      Uri u=Uri.parse(input.trim());String h=u.getHost();
      if(!"https".equalsIgnoreCase(u.getScheme())||h==null||!h.contains(".")||
        u.getUserInfo()!=null||u.getPort()!=-1||u.getQuery()!=null||
        u.getFragment()!=null||(!u.getPath().isEmpty()&&!u.getPath().equals("/")))return null;
      return "https://"+h.toLowerCase(Locale.ROOT);
    }catch(Exception e){return null;}
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);getWindow().setStatusBarColor(DARK);
    LinearLayout screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);
    screen.setPadding(dp(24),dp(36),dp(24),dp(18));screen.setBackgroundColor(BG);
    ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.app_icon);
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(68),dp(68));lp.bottomMargin=dp(14);screen.addView(logo,lp);
    TextView title=text("Yigora",32);title.setTypeface(null,1);screen.addView(title);
    TextView sub=text("CLOUD  •  1.1.0",12);sub.setPadding(0,dp(2),0,dp(28));screen.addView(sub);
    TextView info=text("Google hesabı ilə giriş, borcluların çapı, mağaza və icarələrin idarəsi — bütün icazələr Windows kompüterindən verilir.",16);
    info.setPadding(0,0,0,dp(18));screen.addView(info);
    screen.addView(text("Yigora Cloud ünvanı",14));
    EditText url=new EditText(this);url.setSingleLine(true);
    url.setHint("https://PROJECT_ID.web.app");
    url.setText(getPreferences(MODE_PRIVATE).getString("cloud_url",""));
    url.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);
    screen.addView(url,new LinearLayout.LayoutParams(-1,dp(62)));
    Button start=new Button(this);start.setAllCaps(false);start.setText("Yigora Cloud-a daxil ol");
    screen.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
    TextView note=text("Giriş təhlükəsiz Google autentifikasiyası üçün telefonun brauzerində açılır. VPN lazım deyil. Windows proqramı aktiv olduqda printerdə çap əmri icra olunur.",13);
    note.setPadding(0,dp(18),0,0);screen.addView(note);
    start.setOnClickListener(v->{
      String good=valid(url.getText().toString());
      if(good==null){url.setError("HTTPS ünvanı daxil edin");return;}
      getPreferences(MODE_PRIVATE).edit().putString("cloud_url",good).apply();
      try{
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(good));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        startActivity(intent);
      }catch(Exception e){new AlertDialog.Builder(this).setTitle("Brauzer açılmadı").setMessage(e.getMessage()).setPositiveButton("Bağla",null).show();}
    });
    setContentView(screen);
  }
}