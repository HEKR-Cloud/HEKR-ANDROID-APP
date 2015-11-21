package com.hekr.android.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import com.hekr.android.app.model.Global;
import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.util.DetailCut;
import org.xwalk.core.*;



/**
 * Created by xubukan on 2015/3/27.
 */
public class DeviceDetailActivity extends Activity {

    private static XWalkView mWeb;

    private CustomProgress detailProgressBar;
    private DetailCut detailCut;
    private String lang;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        initView();
        initData();
    }

    private void initView() {

        mWeb = (XWalkView)findViewById(R.id.id_device_web);

        if (Build.VERSION.SDK_INT >= 19)
        {
            mWeb.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWeb.setResourceClient(new ResourceClient(mWeb));

        XWalkPreferences.setValue("enable-javascript", true);
    }

    private void initData() {

        detailCut=DetailCut.getInstance(DeviceDetailActivity.this);
        //传过来的设备信息
        Intent i=getIntent();
        String tid=i.getStringExtra("tid");
        String mid=detailCut.getMid(i.getStringExtra("detail"));

        lang=getResources().getConfiguration().locale.getLanguage()+"-"+ getResources().getConfiguration().locale.getCountry();
        user=((TelephonyManager)DeviceDetailActivity.this.getSystemService(TELEPHONY_SERVICE)).getDeviceId();

        detailProgressBar = CustomProgress.show(DeviceDetailActivity.this, getResources().getString(R.string.login_loadding), true, null);

        if(!TextUtils.isEmpty(mid)&&!TextUtils.isEmpty(tid)&&!TextUtils.isEmpty(Global.USERACCESSKEY)&&user!=null){
            mWeb.load("http://app.hekr.me/vendor/" + mid + "/index.html?access_key=" + Global.USERACCESSKEY + "&tid=" + tid + "&lang=" + lang+"&user="+user,null);
        }
    }

    class ResourceClient extends XWalkResourceClient {

        public ResourceClient(XWalkView xwalkView) {
            super(xwalkView);
        }

        public void onLoadStarted(XWalkView view, String url) {
            super.onLoadStarted(view, url);

        }

        public void onLoadFinished(XWalkView view, String url) {
            if (detailProgressBar!=null&&detailProgressBar.isShowing()) {
                detailProgressBar.dismiss();
            }
            super.onLoadFinished(view, url);
        }

        public void onProgressChanged(XWalkView view, int progressInPercent) {
            super.onProgressChanged(view, progressInPercent);
        }

        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            return super.shouldInterceptLoadRequest(view, url);
        }

        public void onReceivedLoadError(XWalkView view, int errorCode, String description,
                                        String failingUrl) {
            if (detailProgressBar!=null&&detailProgressBar.isShowing()) {
                detailProgressBar.dismiss();
            }
            super.onReceivedLoadError(view, errorCode, description, failingUrl);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedSslError(XWalkView view, ValueCallback<Boolean> callback, SslError error) {
            super.onReceivedSslError(view, callback, error);
        }
    }

    public void navBack(View view)
    {
        this.finish();
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
        if (detailProgressBar!=null&&detailProgressBar.isShowing()) {
            detailProgressBar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWeb != null) {
            mWeb.onDestroy();
        }
        super.onDestroy();
    }
}
