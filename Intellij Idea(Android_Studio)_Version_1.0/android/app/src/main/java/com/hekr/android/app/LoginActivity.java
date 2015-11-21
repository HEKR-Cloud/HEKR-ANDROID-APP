package com.hekr.android.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import com.hekr.android.app.util.AssetsDatabaseManager;
import android.util.Log;


/**
 * Created by xubukan on 2015/3/18.
 */
public class LoginActivity extends Activity {

    private static final String TAG="LoginActivity";

    private long firstime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initData();
    }

    private void initData(){

        AssetsDatabaseManager.initManager(getApplication());
        AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
        SQLiteDatabase db = mg.getDatabase("db");

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select setting_value from settings where setting_key=?",
                    new String[]{"user_credential"});

            if (cursor.moveToNext())
            {
                Intent intoMain = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intoMain);
                LoginActivity.this.finish();
            }
            else{
                Log.i(TAG,"游标移动失败！");
            }
        }catch (Exception keye){
            Log.d(LoginActivity.class.getSimpleName(),"Login从数据库查询key异常："+keye.getMessage());
        }finally {
            if(cursor!=null) {
                cursor.close();
            }
        }

    }

    //直接在xml中触发调用
    public void loginQQ(View view){
        startLoginWeb("http://login.hekr.me/oauth.htm?type=qq");
    }

    public void loginTwitter(View view){
        startLoginWeb("http://login.smartmatrix.mx/oauth.htm?type=tw");
    }

    public void loginWeibo(View view){

        startLoginWeb("http://login.hekr.me/oauth.htm?type=weibo");
    }

    public void loginGoogle(View view){
        startLoginWeb("http://login.smartmatrix.mx/oauth.htm?type=g");
    }

    private void startLoginWeb(String url) {
        Intent intoLoginWeb = new Intent();
        intoLoginWeb.putExtra("url",url);
        intoLoginWeb.setClass(LoginActivity.this, LoginWebActivity.class);
        startActivity(intoLoginWeb);
        LoginActivity.this.finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondtime = System.currentTimeMillis();
            if (secondtime - firstime > 3000) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.press_again_exit), Toast.LENGTH_SHORT).show();
                firstime = System.currentTimeMillis();
                return true;
            } else {
                System.exit(0);
            }
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
    protected void onDestroy() {
        super.onDestroy();
    }

}
