package com.pharaoh.tvplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {
    LinearLayout mWebContainer;
    WebView mWebView;

    Config config;
    long lastBackTime = 0;
    //private GestureDetector mGestureDetector;
    String[] PlayUrls = null;
    //int currentCCTV = 1;

    private float[] lastTouchDownXY = new float[2];

    private  String getUrl() {
        int length = PlayUrls.length;
        int currentCCTV = config.getCurrentCCTV();
        if(currentCCTV>length) currentCCTV=1;
        if(currentCCTV < 1) currentCCTV = length;
        config.setCurrentCCTV(currentCCTV);
        toast("播放"+ currentCCTV +"频道",Toast.LENGTH_LONG);
        return PlayUrls[currentCCTV-1].trim();
    }
    private void Play() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(getUrl());
            }
        });
    }

    private void LoadConfig() {
        new Thread() {
            @Override
            public void run() {
                String text = Http.Get(config.JS_HOST + "config.txt", new HttpCallback() {
                    @Override
                    public void result(String text) {
                        if(text !=null && text.length()>0) {
                            PlayUrls = text.split("\\n");
                            Play();
                        }
                        else {
                            toast("获取频道错误!请检查网络是否正常!",Toast.LENGTH_LONG);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //mWebView.loadData("Null","text/plain","utf8");
                                    mWebView.loadUrl("about:blank");
                                }
                            });

                        }
                    }
                });

            }
        }.start();
    }

    private void PlayNext() {
        config.setCurrentCCTV(config.getCurrentCCTV()+1);
        Play();
    }
    private void PlayPre() {
        config.setCurrentCCTV(config.getCurrentCCTV()-1);
        Play();
    }
    private void Play(int i) {
        config.setCurrentCCTV(i);
        mWebView.loadUrl(getUrl());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initGesture();
        config = new Config(this);

        mWebContainer = (LinearLayout)findViewById(R.id.webViewParent);

        mWebView = (WebView) findViewById(R.id.webview);
        initWebSettings();

        initJsInterface();

        initWebChromeClient();

        initWebViewClient();

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // save the X,Y coordinates
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.getX();
                    lastTouchDownXY[1] = event.getY();
                }
                return false;
            }
        });
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                //PlayNext();
                int h = mWebView.getHeight()/2;
                if(lastTouchDownXY[1]<h) {
                    showConfigDialog();
                }
                else {
                    int w = mWebView.getWidth()/2;
                    if(lastTouchDownXY[0]<w) {
                        PlayPre();
                    } else {
                        PlayNext();
                    }
                }
                //showConfigDialog();
                return true;
            }
        });

        LoadConfig();

    }

    @Override
    protected void onDestroy() {
        destroyWebView();
        super.onDestroy();
    }

    private void destroyWebView() {
        if(mWebView != null) {

            //mWebContainer.removeView(mWebView);
            mWebView.removeAllViews();
            //mWebView.loadData("Null","text/plain","utf8");
            mWebView.loadUrl("about:blank");
            mWebView.onPause();
            mWebView.removeAllViews();
            mWebView.pauseTimers();
            mWebView.destroy();
            mWebView = null;
        }
    }

    private void initWebSettings() {
        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        //webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        WebView.setWebContentsDebuggingEnabled(true);
    }

    private void initWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view,String url) {
                try {
                    URL u = new URL(url);
                    String  host = u.getHost();
                    String ujs = config.JS_HOST+host+".js";
                    //Toast.makeText(MainActivity.this,"JS:"+ujs,Toast.LENGTH_LONG).show();
                    new Thread() {
                        @Override
                        public void run() {
                            String text = Http.Get(ujs, new HttpCallback() {
                                @Override
                                public void result(String text) {
                                    if(text !=null && text.length()>0) {
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mWebView.evaluateJavascript(text,null);
                                            }
                                        });

                                    }
                                }
                            });

                        }
                    }.start();
                } catch (MalformedURLException e) {
                    //throw new RuntimeException(e);
                }
            }
        });
    }

    private void initWebChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // 此处的 view 就是全屏的视频播放界面，需要把它添加到我们的界面上
                //Toast.makeText(MainActivity.this,"full screen",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onHideCustomView() {
                // 退出全屏播放，我们要把之前添加到界面上的视频播放界面移除
                Toast.makeText(MainActivity.this,"Exit full screen",Toast.LENGTH_LONG).show();
            }

            @Override
            public  boolean onJsPrompt(WebView view, String url, String message,String defaultValue, JsPromptResult result) {
                if(message.equals("httpget")) {
                    new Thread() {
                        @Override
                        public void run() {
                            String text = Http.Get(defaultValue, new HttpCallback() {
                                @Override
                                public void result(String txt) {
                                    result.confirm(txt);
                                }
                            });

                        }
                    }.start();
                }
                return true;
            }
        });
    }

    private void initJsInterface() {
        mWebView.addJavascriptInterface(new Object() {

            @JavascriptInterface
            public void toast(final String msg) {
                MainActivity.this.toast(msg);
            }

            @JavascriptInterface
            public void close() {
                MainActivity.this.finish();
            }

            @JavascriptInterface
            public void playM3u8(final String u,final String packName,final String className,final String type) {
                Uri uri = Uri.parse(u);
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                i.setPackage(packName);
                i.setClassName(packName,className);
                i.setDataAndType(uri,type);
                //MainActivity.this.toast(i.toUri(0),Toast.LENGTH_LONG);
                MainActivity.this.setClipText(i.toUri(0));

                MainActivity.this.startActivity(i);
            }

            @JavascriptInterface
            public void playIntent(final String intent) {
                try {
                    Intent i = Intent.parseUri(intent,0);
                    MainActivity.this.startActivity(i);
                } catch (Exception e) {
                    MainActivity.this.toast("Error:"+e.getMessage());
                }
            }


        }, "AndroidJs");
    }
    public void toast(final String msg, final int time) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, time).show();
            }
        });
    }

    public void toast(final String msg) {
        toast(msg,Toast.LENGTH_SHORT);
    }

    public void setClipText(String txt) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, txt));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            return DoKey(keyCode);
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean DoKey(int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - lastBackTime > 2000) {
                lastBackTime = System.currentTimeMillis();
                toast("再按一次返回键关闭");
            }
            else {
                this.finish();
            }
            return true;
        }
        Toast.makeText(this,"KeyCode:"+keyCode,Toast.LENGTH_SHORT).show();
        switch (keyCode) {
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                PlayPre();
                return true;
            case KeyEvent.KEYCODE_CHANNEL_UP:
                PlayNext();
                return true;
            case KeyEvent.KEYCODE_MENU:
                showConfigDialog();
                return true;
            default:
                //Toast.makeText(this,"keyCode:"+keyCode,Toast.LENGTH_LONG).show();
                if(keyCode > KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    Play(keyCode-KeyEvent.KEYCODE_0);
                    return true;
                } else if(keyCode > KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
                    Play(keyCode-KeyEvent.KEYCODE_NUMPAD_0);
                    return true;
                }
                break;
        }

        return false;
    }

    private void showConfigDialog() {
        // 获取EditText
        final EditText editText = new EditText(this);
        editText.setSingleLine();
        editText.setHint("设置地址");
        editText.requestFocus();
        editText.setFocusable(true);
        editText.setText(config.JS_HOST);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this)
                .setTitle("请输入设置地址：")
                .setView(editText).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content = editText.getText().toString().trim();
                                if (content.length() == 0) {
                                    toast("网址不能为空!");
                                    return;
                                }
                                config.setJS_HOST(content);
                                LoadConfig();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        inputDialog.create().show();
    }

}