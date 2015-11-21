package com.hekr.android.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.hekr.android.app.model.Global;
import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.util.DetailCut;
import com.hekr.android.app.util.HekrUser;
import com.hekr.android.app.util.HekrWebSocket;
import com.hekr.android.app.util.MySettingsHelper;
import org.json.JSONException;
import org.json.JSONObject;


public class SetActivity extends Activity {

    private static final String TAG="SetActivity";

    private DetailCut detailCut;
    //进入重命名页面
    private ImageView inToRenameButton;
    //进入控制页面按钮
    private TextView controlButton;
    //固件更新按钮
    private TextView updateButton;
    //设备名称
    private TextView deviceNameText;
    //设备品牌
    private TextView deviceBrandText;
    //设备型号
    private TextView deviceTypeText;
    //固件版本
    private TextView firmwareVersionText;
    //设备icon
    private ImageView circleImageView;
    //进度条的数值显示TextView
    private TextView present;

    private Dialog updateDialog;

    //更新按钮是否可点击并发送命令
    private boolean flag=true;

    //原tid
    private String oldTid;
    //mid
    private String mid;
    //原名字
    private String name;
    //设备detail
    private String detail;
    //固件版本号
    private String binver;
    //固件版本类型
    private String bintype;

    //请求固件更新网络操作对象
    private HekrUser hekrUser;
    //进入设置页面获取用户固件信息ProgressBar
    private CustomProgress setProgressBar;
    //发送指令之后进度条
    private ProgressBar mProgress;
    //发送指令之后进度条数值
    private int progressBarNum=0;
    //websocket
    private HekrWebSocket hekr_wsc;

    //fa送命令返回的状态
    private String orderStatus="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        initViews();
        initDatas();
        initUpdateStatus();

        if(!TextUtils.isEmpty(Global.uid)&&networkIsAvailable(SetActivity.this)){
            hekr_wsc = new HekrWebSocket(WebSocketHandler, Global.uid, "USER");
        }

    }
    //初始化控件
    private void initViews(){
        inToRenameButton= (ImageView) findViewById(R.id.cominupdatename);
        controlButton= (TextView) findViewById(R.id.control);
        updateButton= (TextView) findViewById(R.id.update);
        deviceNameText= (TextView) findViewById(R.id.deviceName);
        deviceBrandText= (TextView) findViewById(R.id.deviceBrand);
        deviceTypeText= (TextView) findViewById(R.id.deviceType);
        firmwareVersionText= (TextView) findViewById(R.id.firmwareVersion);
        circleImageView= (ImageView) findViewById(R.id.circleImageView);

        inToRenameButton.setOnClickListener(buttonClick);
        deviceNameText.setOnClickListener(buttonClick);
        controlButton.setOnClickListener(buttonClick);
        updateButton.setOnClickListener(buttonClick);
    }

    private void initDatas(){
        Intent i=getIntent();
        oldTid=i.getStringExtra("tid");
        name=i.getStringExtra("name");
        detail=i.getStringExtra("detail");

        detailCut=DetailCut.getInstance(SetActivity.this);
        hekrUser=HekrUser.getInstance(MySettingsHelper.getCookieUser());

        mid=detailCut.getMid(detail);

        if(!TextUtils.isEmpty(name))
        {
            deviceNameText.setText(name);
        }
        else{
            if(!TextUtils.isEmpty(oldTid)&&oldTid.length()>2){
                deviceNameText.setText(oldTid.substring(oldTid.length() - 2));
            }
            else{
                deviceNameText.setText("");
            }
        }

        circleImageView.setImageBitmap(detailCut.getIcon(detail,SetActivity.this));

        Log.i(TAG,"detail:"+detail);
        if(!TextUtils.isEmpty(detailCut.getVerAndType(detail))){
            firmwareVersionText.setText(detailCut.getVerAndType(detail));
            binver=detailCut.getVerAndType(detail).substring(0,detailCut.getVerAndType(detail).length()-3);
            bintype=detailCut.getVerAndType(detail).substring(detailCut.getVerAndType(detail).length()-2,detailCut.getVerAndType(detail).length()-1);
        }
        else{
            if(!TextUtils.isEmpty(detailCut.getBinver(detail))&&!TextUtils.isEmpty(detailCut.getBintype(detail))){
                firmwareVersionText.setText(detailCut.getBinver(detail)+"("+detailCut.getBintype(detail)+")");
                binver=detailCut.getBinver(detail);
                bintype=detailCut.getBintype(detail);
            }
            else{
                firmwareVersionText.setText("");
                binver="";
                bintype="";
            }
        }

      new AsyncTask<Void,Void,String>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return hekrUser.deviceFirmwareInformation(mid);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject jsonObject=new JSONObject(s);
                    String vaule=jsonObject.getString("value");
                    JSONObject deviceFirmareInfornation=new JSONObject(vaule);
                    String pName=deviceFirmareInfornation.getString("pname");
                    String mname=deviceFirmareInfornation.getString("mname");
                    deviceBrandText.setText(pName.toString().trim());
                    deviceTypeText.setText(mname.toString().trim());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();

    }

    private void initUpdateStatus() {
        //
        String updateButtonText=null;
        //
        String reallyBintype=null;
        if(!TextUtils.isEmpty(mid)&&!TextUtils.isEmpty(binver)&&!TextUtils.isEmpty(bintype)&&!TextUtils.isEmpty(oldTid)){
            reallyBintype=getReallyBintype(bintype);
            Log.i(TAG,"reallyBintype:"+reallyBintype+"||mid:"+mid+"||binver:"+binver+"||oldTid:"+oldTid);

            final String finalReallyBintype = reallyBintype;

            new AsyncTask<Void,Void,String>(){

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    if(networkIsAvailable(SetActivity.this)){
                        setProgressBar = CustomProgress.show(SetActivity.this, getResources().getString(R.string.loading_device), false, null);
                    }
                }

                @Override
                protected String doInBackground(Void... voids) {

                    return hekrUser.appFirmwareUpdate(mid,oldTid,binver, finalReallyBintype);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    JSONObject jo=null;
                    try {
                        if(!TextUtils.isEmpty(s)){
                            jo = new JSONObject(s);
                        }
                        else{
                            Log.i(TAG,"jo(固件升级接口返回信息类):为空");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int code;
                    //String message="";
                    int compulsory=400;
                    JSONObject vauleJo=null;
                    if(jo!=null){
                        try {
                            code=jo.getInt("code");
                            String backFirmwareUpdateValue=jo.optString("value");
                            if(!TextUtils.isEmpty(backFirmwareUpdateValue)){
                                vauleJo = new JSONObject(backFirmwareUpdateValue);
                            }
                            else{
                                Log.i(TAG,"backFirmwareUpdateValue:为空");
                            }

                            if(vauleJo!=null){
                                compulsory=vauleJo.getInt("compulsory");
                            }
                            else{
                                Log.i(TAG,"vauleJo(固件升级接口返回value类):为空");
                            }
                            if(code==200){
                                if(compulsory==0){
                                    updateButton.setText(getResources().getString(R.string.set_button_permission_updateDevice));
                                }
                                else{
                                    updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
                                }
                            }
                            else{
                                updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.i(TAG,"jo(固件升级接口返回信息类):为空");
                    }
                    if (setProgressBar!=null&&setProgressBar.isShowing()) {
                        setProgressBar.dismiss();
                    }
                }
            }.execute();
        }
        else{
            updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
        }
    }

    //如果当前的固件版本为A那么请求B,如果为B那么为请求A
    public String getReallyBintype(String bintype){
        String reallyBintype=null;
        if("B".equals(bintype)){
            reallyBintype= "A";
        }
        if("A".equals(bintype)){
            reallyBintype= "B";
        }
        return reallyBintype;
    }

    //网络是否可用
    private boolean networkIsAvailable(Context mContext){
        ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(mContext.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isAvailable()){
            return true;
        }
        else{
            return false;
        }
    }

    //固件更新进度条
    private void showFirmwareUpdateDialog(Context mContext){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.set_showFirmwareUpdateDialog_title));

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.activity_set_progress, null);
        mProgress = (ProgressBar)v.findViewById(R.id.progress);
        present= (TextView) v.findViewById(R.id.present);

        builder.setView(v);

        updateDialog = builder.create();
        updateDialog.setCanceledOnTouchOutside(false);
        if(updateDialog!=null&&mContext!=null){
            updateDialog.show();
        }
    }

    //计算已过去的时间
    private long getPassTime(long beginTime) {
        return System.currentTimeMillis() - beginTime;
    }

    View.OnClickListener buttonClick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                //右键进入改名页面
                case R.id.cominupdatename:
                    Intent intoRenameimageView=new Intent(SetActivity.this,RenameDeviceActivity.class);
                    intoRenameimageView.putExtra("tid",oldTid);
                    intoRenameimageView.putExtra("name",name);
                    intoRenameimageView.putExtra("detail",detail);
                    startActivity(intoRenameimageView);
                    break;
                //文字进入改名设备
                case R.id.deviceName:
                    Intent intoRenameText=new Intent(SetActivity.this,RenameDeviceActivity.class);
                    intoRenameText.putExtra("tid",oldTid);
                    intoRenameText.putExtra("name",name);
                    intoRenameText.putExtra("detail",detail);
                    startActivity(intoRenameText);
                    break;
                //进入控制页面
                case R.id.control:
                    Intent intoDetail=new Intent(SetActivity.this,DeviceDetailActivity.class);
                    intoDetail.putExtra("tid",oldTid);
                    intoDetail.putExtra("detail",detail);
                    startActivity(intoDetail);
                    break;
                case R.id.update:
                    if(getResources().getString(R.string.set_button_permission_updateDevice).equals(updateButton.getText().toString().trim())){
                        if ((hekr_wsc != null) && hekr_wsc.checkWebSocketLinked())
                        {
                            if(networkIsAvailable(SetActivity.this)){
                                showFirmwareUpdateDialog(SetActivity.this);
                                hekr_wsc.sendDevCall(oldTid);
                            }
                            else{
                                Log.i(TAG,"打开固件更新dialog发现并无网络服务！");
                            }

                        }
                        else{
                            Log.i(TAG,"hekr_wsc断开连接！");
                        }
                    }
                    else{
                        Toast.makeText(SetActivity.this,getResources().getString(R.string.set_after_onclick_not_need_update_button_tip),Toast.LENGTH_SHORT).show();
                    }
                default:
                    break;
            }
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler WebSocketHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            switch(msg.what)
            {
                case HekrWebSocket.MSG_REC_UART_DATA:
                    String payload=data.getString("payload");
                    Log.i(TAG,"payload:"+payload);

                    if("0".equals(payload)){
                        orderStatus="0";
                        //Log.i(TAG,"发送指令返回状态："+orderStatus+"||成功");
                    }
                    if("1".equals(payload)){
                        orderStatus="1";
                        //Log.i(TAG,"发送指令返回状态："+orderStatus+"||TID错误");
                    }
                    if("2".equals(payload)){
                        orderStatus="2";
                        //Log.i(TAG,"发送指令返回状态："+orderStatus+"||固件类型错误");
                    }
                    if("3".equals(payload)){
                        orderStatus="3";
                        //Log.i(TAG,"发送指令返回状态："+orderStatus+"||升级中");
                    }
                    if(!TextUtils.isEmpty(orderStatus)&&"0".equals(orderStatus)) {

                        if (!DetailCut.getDetailList(payload).isEmpty() && DetailCut.getDetailList(payload).size() >= 4 &&
                                oldTid.equals(DetailCut.getDetailList(payload).get(2)) && "upgradeprogress".equals(DetailCut.getDetailList(payload).get(3))) {
                            switch (detailCut.getUpgradestate(payload)) {
                                case 404:
                                    if(SetActivity.this!=null) {
                                        Toast.makeText(SetActivity.this, getResources().getString(R.string.set_after_onclick_button_Upgradestate_404_tip), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case 0:
                                    progressBarNum = 100;
                                    if(SetActivity.this!=null) {
                                        Toast.makeText(SetActivity.this, getResources().getString(R.string.set_after_onclick_button_Upgradestate_0_tip), Toast.LENGTH_SHORT).show();
                                    }
                                    if(updateButton!=null){
                                        updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
                                    }
                                    if (updateDialog != null && updateDialog.isShowing()) {
                                        updateDialog.dismiss();
                                    }
                                    break;
                                case 1:
                                    progressBarNum = detailCut.getUpgradeprogress(payload);
                                    Log.i(TAG, "当前progressBarNum:" + progressBarNum);
                                    if(mProgress!=null){
                                        mProgress.setProgress(progressBarNum);
                                    }
                                    if(present!=null){
                                        present.setText(progressBarNum + "%");
                                    }
                                    if (progressBarNum == 100) {
                                        if(updateButton!=null){
                                            updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
                                        }
                                        if (updateDialog != null && updateDialog.isShowing()) {
                                            updateDialog.dismiss();
                                        }
                                    }
                                    break;
                                case 2:
                                    if(SetActivity.this!=null) {
                                        Toast.makeText(SetActivity.this, getResources().getString(R.string.set_after_onclick_button_Upgradestate_2_tip), Toast.LENGTH_SHORT).show();
                                    }
                                        if(updateButton!=null) {
                                        updateButton.setText(getResources().getString(R.string.set_button_permission_updateDevice));
                                    }

                                    break;
                                case 3:
                                    if(SetActivity.this!=null) {
                                        Toast.makeText(SetActivity.this, getResources().getString(R.string.set_after_onclick_button_Upgradestate_3_tip), Toast.LENGTH_SHORT).show();
                                    }
                                        if(updateButton!=null) {
                                        updateButton.setText(getResources().getString(R.string.set_button_permission_updateDevice));
                                    }
                                    break;
                                case 4:
                                    if(SetActivity.this!=null) {
                                        Toast.makeText(SetActivity.this, getResources().getString(R.string.set_after_onclick_button_Upgradestate_4_tip), Toast.LENGTH_SHORT).show();
                                    }
                                        if(updateButton!=null) {
                                        updateButton.setText(getResources().getString(R.string.set_button_permission_updateDevice));
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            if (!DetailCut.getDetailList(payload).isEmpty() && DetailCut.getDetailList(payload).size() >= 4 &&
                                    oldTid.equals(DetailCut.getDetailList(payload).get(2)) && "login".equals(DetailCut.getDetailList(payload).get(3))) {
                                //Toast.makeText(SetActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
                                if(updateButton!=null) {
                                    updateButton.setText(getResources().getString(R.string.set_button_not_need_updateDevice));
                                }
                                else{

                                }
                                if (updateDialog != null && updateDialog.isShowing()) {
                                    updateDialog.dismiss();
                                } else {
                                    Log.i(TAG,"set_updateDialog_is_null");
                                }
                            } else {
                                //Log.i(TAG,"指令中字段为空||指令的长度小于4||指令中的第三个字段不是登录字段！");
                            }
                        }
                    }
                    else{
                        if("1".equals(orderStatus)){
                            orderStatus="1";
                            Log.i(TAG,"发送指令返回状态："+orderStatus+"||TID错误");
                        }
                        if("2".equals(orderStatus)){
                            orderStatus="2";
                            Log.i(TAG,"发送指令返回状态："+orderStatus+"||固件类型错误");
                        }
                        if("3".equals(orderStatus)){
                            orderStatus="3";
                            Log.i(TAG,"发送指令返回状态："+orderStatus+"||升级中");
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    };

    //回退键
    public void navBack(View view) {

        SetActivity.this.finish();
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
    protected void onStop() {
        if(setProgressBar!=null&&setProgressBar.isShowing()){
            setProgressBar.dismiss();
        }
        orderStatus="";
        if(updateDialog!=null&&updateDialog.isShowing()){
            updateDialog.dismiss();
        }
        super.onStop();
    }

}
