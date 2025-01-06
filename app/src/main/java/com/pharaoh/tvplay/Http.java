package com.pharaoh.tvplay;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.content.Context;

public class Http {
    private static File cacheDir; // 缓存目录

    // 初始化缓存目录
    public static void init(Context context) {
        cacheDir = new File(context.getCacheDir(), "http_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    static public String Get(String url, HttpCallback cb) {
        try {
            String ret = Get(new URL(url),null);
            if (cb != null) {
                cb.result(ret);
            }
            return ret;
        } catch (IOException ex) {
            return null;
        }
    }

    static private String Get(URL u, HttpCallback cb) {
        String cacheKey = generateCacheKey(u.toString());
        String cachedContent = readFromCache(cacheKey);
        
        // 如果有缓存，先返回缓存内容
        if (cachedContent != null) {
            // 在后台更新缓存
            new Thread(() -> {
                String newContent = fetchFromNetwork(u, cb);
                if (newContent != null) {
                    saveToCache(cacheKey, newContent);
                }
            }).start();
            
            return cachedContent;
        }
        
        // 如果没有缓存，从网络获取并保存到缓存
        String content = fetchFromNetwork(u, cb);
        if (content != null) {
            saveToCache(cacheKey, content);
        }
        return content;
    }

    // 从网络获取内容的方法
    private static String fetchFromNetwork(URL u, HttpCallback cb) {
        try {
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(50000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                String txt = getStringByStream(is);
                if (cb != null) {
                    cb.result(txt);
                }
                return txt;
            } else {
                if (cb != null) {
                    cb.result(null);
                }
                return null;
            }
        } catch (Exception ex) {
            if (cb != null) {
                cb.result(null);
            }
            return null;
        }
    }

    // 生成缓存键
    private static String generateCacheKey(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(url.hashCode());
        }
    }

    // 从缓存读取内容
    private static String readFromCache(String cacheKey) {
        if (cacheDir == null) {
            return null;
        }
        File cacheFile = new File(cacheDir, cacheKey);
        if (!cacheFile.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            return null;
        }
    }

    // 保存内容到缓存
    private static void saveToCache(String cacheKey, String content) {
        if (cacheDir == null) {
            return;
        }
        File cacheFile = new File(cacheDir, cacheKey);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 原有的getStringByStream方法保持不变
    static private String getStringByStream(InputStream inputStream) {
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
