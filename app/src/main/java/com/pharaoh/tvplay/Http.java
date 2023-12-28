package com.pharaoh.tvplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Http {
    static public String Get(String url,HttpCallback cb) {
        try {
            return Get(new URL(url),cb);
        } catch (IOException ex) {
            return null;
        }
    }
    static public String Get(URL u,HttpCallback cb) {
        try {
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if(responseCode ==200){
                //请求成功 获得返回的流
                InputStream is = connection.getInputStream();
                String txt = getStringByStream(is);
                if(cb!=null) {
                    cb.result(txt);
                }
                return txt;
            }else {
                if(cb!=null) {
                    cb.result(null);
                }
                //请求失败
                return null;
            }
        } catch (Exception ex) {
            if(cb!=null) {
                cb.result(null);
            }
            return null;
        }

    }

    static private String getStringByStream(InputStream inputStream){
        Reader reader;
        try {
            reader=new InputStreamReader(inputStream,"UTF-8");
            char[] rawBuffer=new char[512];
            StringBuffer buffer=new StringBuffer();
            int length;
            while ((length=reader.read(rawBuffer))!=-1){
                buffer.append(rawBuffer,0,length);
            }
            return buffer.toString();
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
