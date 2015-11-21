package com.hekr.android.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.util.*;


public class RenameDeviceActivity extends Activity implements OnClickListener{

    //新名称
    private EditText deviceNewNameEditText;
    //设备种类
    private TextView deviceTypeTextView;
    //设备原名称
    private TextView deviceNameTextView;
    //确定修改
    private ImageButton updateNameButton;
    //原tid（唯一标识）
    private String oldTid;
    //设备种类name
    private String name;
    //网络操作类
    private HekrUser hekrUser;
    //detail切割
    private DetailCut detailCut;

    private CustomProgress renameProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_device);

        //获取控件id
        initViews();
        initData();

        updateNameButton.setOnClickListener(this);
    }

    private void initViews() {
        deviceNewNameEditText= (EditText) findViewById(R.id.devicetid);
        deviceTypeTextView=(TextView)findViewById(R.id.devicetype);
        deviceNameTextView=(TextView)findViewById(R.id.devicename);
        updateNameButton=(ImageButton)findViewById(R.id.updatename);
    }

    private void initData() {
        Intent i=getIntent();
        oldTid=i.getStringExtra("tid");
        name=i.getStringExtra("name");
        detailCut=DetailCut.getInstance(RenameDeviceActivity.this);
        deviceTypeTextView.setText(detailCut.getName(i.getStringExtra("detail")));
        hekrUser= HekrUser.getInstance(MySettingsHelper.getCookieUser());

        if(!TextUtils.isEmpty(name))
        {
            deviceNameTextView.setText(getResources().getString(R.string.device_oldname)+name);
        }
        else{
            if(!TextUtils.isEmpty(oldTid)){
                deviceNameTextView.setText(getResources().getString(R.string.device_oldname)+oldTid.substring(oldTid.length() - 2));
            }
            else{

            }
        }
    }

    @Override
    public void onClick(View view) {
        if(TextUtils.isEmpty(deviceNewNameEditText.getText().toString().trim())) {
            Toast.makeText(RenameDeviceActivity.this,getResources().getString(R.string.toast_newname_notnull),Toast.LENGTH_SHORT).show();
        }
        else{
            if(!TextUtils.isEmpty(oldTid)){

                new AsyncTask<Integer, Integer, Boolean>(){

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        renameProgressBar = CustomProgress.show(RenameDeviceActivity.this, getResources().getString(R.string.renameProgressBar_toast_updating), true, null);
                    }

                    @Override
                    protected Boolean doInBackground(Integer... integers) {
                            hekrUser.renameDevice(oldTid, deviceNewNameEditText.getText().toString().trim());
                            Log.d("MyLog", "tid:"+oldTid+" 新名称:" +deviceNewNameEditText.getText().toString()+ " 改名返回值:"+hekrUser.renameDevice(oldTid,deviceNewNameEditText.getText().toString().trim()));
                            return hekrUser.renameDevice(oldTid, deviceNewNameEditText.getText().toString().trim());
                    }

                    @Override
                    protected void onPostExecute(Boolean aBoolean) {
                        super.onPostExecute(aBoolean);
                        if(aBoolean){
                            Toast.makeText(RenameDeviceActivity.this,getResources().getString(R.string.rename_device_success_tip),Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(RenameDeviceActivity.this,MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            RenameDeviceActivity.this.finish();
                        }
                        else{
                            Toast.makeText(RenameDeviceActivity.this,getResources().getString(R.string.rename_device_fail_tip),Toast.LENGTH_SHORT).show();
                        }
                        if (renameProgressBar!=null&&renameProgressBar.isShowing()) {
                            renameProgressBar.dismiss();
                        }
                    }
                }.execute();
            }
            else{
                //Toast.makeText(RenameDeviceActivity.this,getResources().getString(R.string.toast_newname_notnull).toString(),Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void navBack(View view)
    {
        RenameDeviceActivity.this.finish();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (renameProgressBar!=null&&renameProgressBar.isShowing()) {
            renameProgressBar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
