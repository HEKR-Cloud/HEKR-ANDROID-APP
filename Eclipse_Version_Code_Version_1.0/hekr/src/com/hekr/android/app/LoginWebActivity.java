package com.hekr.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;

import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.util.AssetsDatabaseManager;
import com.hekr.android.app.util.MySettingsHelper;

import android.text.TextUtils;

import com.hekr.android.app.util.ThreadPool;

import java.util.HashMap;

/**
 * Created by xubukan on 2015/3/20.
 */
public class LoginWebActivity extends Activity {

    private WebView mWeb;
    private CustomProgress loginWebProgressBar;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_web);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        mWeb = (WebView)findViewById(R.id.id_webview);
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        loginWebProgressBar = CustomProgress.show(LoginWebActivity.this, getResources().getString(R.string.login_loadding).toString(), false, null);

        mWeb.setWebViewClient(new WebViewClient()
        {
            //对网页中超链接按钮的响应。当按下某个连接时WebViewClient会调用这个方法，并传递参数
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.contains("success.htm"))
                {
                    //mWeb.setVisibility(View.INVISIBLE);
                }
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                if (loginWebProgressBar.isShowing()) {
                    loginWebProgressBar.dismiss();
                }

                if(url.contains("success.htm"))
                {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookiestr = cookieManager.getCookie(url);                   
                    HashMap<String,String> cookieMap = new HashMap<String, String>();
                    if(cookiestr!=null)
                    {
                        String cookieParams[] = cookiestr.split(";");
                        if(cookieParams.length>0)
                        {
                            for(int i=0;i<cookieParams.length;i++)
                            {
                                String kvParam[] = cookieParams[i].split("=");                              
                                if(kvParam.length==2)
                                {
                                    cookieMap.put(kvParam[0].toString().trim(),kvParam[1].toString().trim());
                                }
                            }
                        }
                        if(cookieMap.containsKey("u"))
                        {
                            AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
                            SQLiteDatabase db = mg.getDatabase("db");
							if(TextUtils.isEmpty(MySettingsHelper.getCookieUser())){
								db.execSQL("INSERT INTO settings VALUES('user_credential','"+cookieMap.get("u")+"');");
							}
                            ThreadPool threadPool = ThreadPool.getThreadPool();
                            threadPool.addTask(MainActivity.keyRunnable);
                            Intent it = new Intent();
                            it.setClass(LoginWebActivity.this, MainActivity.class);
                            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(it);
                            finish();                          
                        }
                    }
                }

                super.onPageFinished(view, url);
            }

        });

        mWeb.loadUrl(url);
    }

    private void clearCookies(Context context){
        CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
    }
    public void navBack(View view){
        Intent  it = new Intent();
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.setClass(LoginWebActivity.this, LoginActivity.class);
        startActivity(it);
        finish();
    }

    protected void onResume(){ 
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        super.onResume();
        
    }
    public void onPause() {
        super.onPause();
        
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent  it = new Intent();
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.setClass(LoginWebActivity.this, LoginActivity.class);
            startActivity(it);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }    
}
