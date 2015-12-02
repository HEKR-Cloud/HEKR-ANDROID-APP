package com.hekr.android.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.webkit.*;

import com.hekr.android.app.model.Global;
import com.hekr.android.app.ui.CustomProgress;

import android.util.Log;

import com.lambdatm.runtime.lang.Cell;
import com.lambdatm.runtime.lib.Base;
import com.lambdatm.runtime.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xubukan on 2015/3/27.
 */
public class DeviceDetailActivity extends Activity {
    static  WebView mWeb;
    private ProgressDialog vProgressBar;

    private Handler handler=new Handler();
    private String mid;
    private String tid;
    private CustomProgress detailProgressBar;


    class ResourceClient extends WebViewClient {

        @Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
        	Log.d("MyLog", "Load Finished:" + url);
            if (detailProgressBar!=null&&detailProgressBar.isShowing()) {
                detailProgressBar.dismiss();
            }
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			Log.d("MyLog", "Load Started:" + url);
		}

		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view,
				String url) {
			// TODO Auto-generated method stub
			return super.shouldInterceptRequest(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			return super.shouldOverrideUrlLoading(view, url);
		}
    }
    protected void onCreate(Bundle savedInstanceState){
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        mWeb = (WebView)findViewById(R.id.id_device_web);


        //传过来的设备信息
        Intent i=getIntent();      
        mid=getMid(i.getStringExtra("detail"));
        tid=i.getStringExtra("tid");

        String lang=getResources().getConfiguration().locale.getLanguage()+"-"+ getResources().getConfiguration().locale.getCountry();
        String user=((TelephonyManager)DeviceDetailActivity.this.getSystemService(TELEPHONY_SERVICE)).getDeviceId();

        detailProgressBar = CustomProgress.show(DeviceDetailActivity.this, getResources().getString(R.string.login_loadding).toString(), true, null);
        mWeb.setWebViewClient(new ResourceClient());
        if(mid!=null&&tid!=null&&Global.USERACCESSKEY!=null&&user!=null){
            mWeb.loadUrl("http://app.hekr.me/vendor/" + mid + "/index.html?access_key=" + Global.USERACCESSKEY + "&tid=" + tid + "&lang=" + lang+"&user="+user);           
        }      
    }
    public void onPause() {
        super.onPause();
       
    }

    public Map<Object, Object> getDetailMap(String detail)
    {

        List stateList = Util.tolist((Cell) Base.read.pc(detail, null));
        Map<Object, Object> detailMap=new HashMap<Object, Object>();
        try {
            for (int i = 0; i < stateList.size(); i = i + 2) {
                detailMap.put(stateList.get(i), stateList.get(i + 1));
            }
        }catch (Exception e){
            //e.printStackTrace();
        }
        return detailMap;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mWeb != null) {
            mWeb.destroy();
        }
    }

    protected void onResume()
    { 
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        super.onResume();
       
    }
    public String getMid(String detail)
    {
        String mid="";
        if(detail!=null){
            mid=getDetailMap(detail).get("mid")+"";
        }
        return mid;
    }
    public void navBack(View view)
    {
        this.finish();
    }
}
