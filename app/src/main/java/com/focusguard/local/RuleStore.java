package com.focusguard.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class RuleStore extends SQLiteOpenHelper {
    public static class Rule {
        public long id;
        public String title, message;
        public boolean enabled;
        public List<String> phrases = new ArrayList<>();
    }
    public static class Trigger {
        public long ruleId;
        public String phrase, message;
        Trigger(long id, String p, String m) { ruleId=id; phrase=p; message=m; }
    }

    private final Context app;
    public RuleStore(Context c) {
        super(c.getApplicationContext(), "fokus_rules.db", null, 1);
        app=c.getApplicationContext();
    }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE rules (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, message TEXT NOT NULL, enabled INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE phrases (id INTEGER PRIMARY KEY AUTOINCREMENT, rule_id INTEGER NOT NULL, phrase TEXT NOT NULL)");
        db.execSQL("CREATE INDEX phrases_rule ON phrases(rule_id)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int o,int n) { }

    public synchronized List<Rule> all() {
        ArrayList<Rule> list=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        try (Cursor c=db.rawQuery("SELECT id,title,message,enabled FROM rules ORDER BY id DESC",null)) {
            while (c.moveToNext()) {
                Rule r=new Rule();
                r.id=c.getLong(0); r.title=c.getString(1); r.message=c.getString(2); r.enabled=c.getInt(3)==1;
                try (Cursor p=db.rawQuery("SELECT phrase FROM phrases WHERE rule_id=? ORDER BY id",new String[]{Long.toString(r.id)})) {
                    while(p.moveToNext())r.phrases.add(p.getString(0));
                }
                list.add(r);
            }
        }
        return list;
    }
    public synchronized void save(long id,String title,String message,String phrases) {
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v=new ContentValues();
            v.put("title",title.trim()); v.put("message",message.trim());
            if(id<=0) {
                v.put("enabled",1);
                id=db.insertOrThrow("rules",null,v);
            } else {
                db.update("rules",v,"id=?",new String[]{Long.toString(id)});
                db.delete("phrases","rule_id=?",new String[]{Long.toString(id)});
            }
            LinkedHashSet<String> unique=new LinkedHashSet<>();
            for(String p:phrases.split("\\r?\\n")) {
                String q=p.trim();
                if(!q.isEmpty())unique.add(q);
            }
            for(String p:unique) {
                ContentValues value=new ContentValues();
                value.put("rule_id",id);value.put("phrase",p);
                db.insertOrThrow("phrases",null,value);
            }
            db.setTransactionSuccessful();
        } finally {db.endTransaction();}
        bump();
    }
    public synchronized void delete(long id) {
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try {
            String[] args={Long.toString(id)};
            db.delete("phrases","rule_id=?",args);
            db.delete("rules","id=?",args);
            db.setTransactionSuccessful();
        } finally {db.endTransaction();}
        bump();
    }
    public synchronized void setEnabled(long id,boolean enabled) {
        ContentValues v=new ContentValues();v.put("enabled",enabled?1:0);
        getWritableDatabase().update("rules",v,"id=?",new String[]{Long.toString(id)});
        bump();
    }
    public synchronized List<Trigger> active() {
        ArrayList<Trigger> result=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT r.id,p.phrase,r.message FROM phrases p JOIN rules r ON r.id=p.rule_id WHERE r.enabled=1 ORDER BY r.id DESC,p.id",null)) {
            while(c.moveToNext()) result.add(new Trigger(c.getLong(0),normal(c.getString(1)),c.getString(2)));
        }
        return result;
    }
    public static String normal(String str) {
        return str.toLowerCase(Locale.ROOT).replace('İ','i').trim();
    }
    public long version() { return app.getSharedPreferences("state",Context.MODE_PRIVATE).getLong("db_version",0); }
    private void bump() {
        SharedPreferences p=app.getSharedPreferences("state",Context.MODE_PRIVATE);
        p.edit().putLong("db_version",p.getLong("db_version",0)+1).apply();
    }
}
