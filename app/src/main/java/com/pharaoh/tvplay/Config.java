package com.pharaoh.tvplay;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    Context context;

    public void setJS_HOST(String JS_HOST) {
        this.JS_HOST = JS_HOST;
        setKey("JS_HOST",JS_HOST);
    }

    public String JS_HOST;

    public int getCurrentCCTV() {
        return currentCCTV;
    }

    public void setCurrentCCTV(int currentCCTV) {
        if(this.currentCCTV == currentCCTV) return;
        this.currentCCTV = currentCCTV;
        setInt("currentCCTV",currentCCTV);
    }

    private int currentCCTV=1;

    public Config(Context c) {
        this.context = c;
        JS_HOST = getKey("JS_HOST","https://tvbox-config.s3.bitiful.net/host/");
        currentCCTV = getInt("currentCCTV",1);
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences("main", context.MODE_PRIVATE);
    }
    private String getKey(String key, String def) {
        return getSharedPreferences().getString(key, def);
    }

    private void setKey(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private Integer getInt(String key, Integer def) {
        return getSharedPreferences().getInt(key, def);
    }

    private void setInt(String key, Integer value) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }




}
