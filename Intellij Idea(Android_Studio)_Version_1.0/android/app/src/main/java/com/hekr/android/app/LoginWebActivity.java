package com.hekr.android.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.util.AssetsDatabaseManager;
import com.hekr.android.app.util.MySettingsHelper;
import com.hekr.android.app.util.ThreadPool;

import java.util.HashMap;

/**
 * Created by xubukan on 2015/3/20.
 */
public class LoginWebActivity extends Activity  {

    private WebView mWeb;

    private CustomProgress loginWebProgressBar;

    private String url;

    private AssetsDatabaseManager mg;

    private SQLiteDatabase db;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_web);

        initViews();
        initData();

        loginWebProgressBar = CustomProgress.show(LoginWebActivity.this, getResources().getString(R.string.login_loadding).toString(), false, null);

        mWeb.loadUrl(url);
    }

    class MyWebviewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            if(url.contains("success.htm"))
            {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookiestr = cookieManager.getCookie(url);

                HashMap<String,String> cookieMap = new HashMap<String, String>();
                if(!TextUtils.isEmpty(cookiestr))
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
                        if(TextUtils.isEmpty(MySettingsHelper.getCookieUser())){
                            db.execSQL("INSERT INTO settings VALUES('user_credential','"+cookieMap.get("u")+"');");
                        }

                        ThreadPool threadPool = ThreadPool.getThreadPool();
                        threadPool.addTask(MainActivity.keyRunnable);

                        Intent webToMain = new Intent(LoginWebActivity.this, MainActivity.class);
                        startActivity(webToMain);
                        LoginWebActivity.this.finish();
                    }
                }
            }
            if (loginWebProgressBar!=null&&loginWebProgressBar.isShowing()) {
                loginWebProgressBar.dismiss();
            }

            super.onPageFinished(view, url);
        }

    }

    private void initViews() {
        mWeb = (WebView)findViewById(R.id.id_webview);
    }

    private void initData() {
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWeb.setWebViewClient(new MyWebviewClient());

        mg = AssetsDatabaseManager.getManager();
        db = mg.getDatabase("db");
    }

    public void navBack(View view) {
        Intent  it = new Intent();
        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.setClass(LoginWebActivity.this, LoginActivity.class);
        startActivity(it);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent  it = new Intent();
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.setClass(LoginWebActivity.this, LoginActivity.class);
            startActivity(it);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {

        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loginWebProgressBar!=null&&loginWebProgressBar.isShowing()) {
            loginWebProgressBar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if(mWeb!=null){
            mWeb.destroy();
        }
        super.onDestroy();
    }

}
