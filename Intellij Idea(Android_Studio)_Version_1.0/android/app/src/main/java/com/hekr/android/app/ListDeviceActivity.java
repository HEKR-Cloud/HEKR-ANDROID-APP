package com.hekr.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.hekr.android.app.refreshui.PullToRefreshBase;
import com.hekr.android.app.refreshui.PullToRefreshListView;
import com.hekr.android.app.ui.CustomProgress;
import com.hekr.android.app.model.DeviceSummary;
import com.hekr.android.app.util.*;
import com.lambdatm.runtime.lang.*;
import com.lambdatm.runtime.lib.Base;
import com.lambdatm.runtime.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.text.SimpleDateFormat;


public class ListDeviceActivity extends Activity  {

    public final static String TAG="MyLog";

    public static Handler listHandler;
    //展示设备和设备信息的控件
    private ListView listView;
    //存放设备信息的类
    private List<DeviceSummary> lData;
    //listView的自定义适配器
    private MyAdapter mAdapter;
    //启动线程转动条
    private CustomProgress listProgressBar;
    private BroadcastReceiver listRefreshReceiver;

    //字体
    private Typeface face;
    private HekrUser hekrUser;

    //刷新view
    private PullToRefreshListView newPullListView;
    //下拉刷新中的时间刷新
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    private AssetsDatabaseManager mg;

    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        newPullListView = new PullToRefreshListView(ListDeviceActivity.this);
        setContentView(newPullListView);

        newPullListView.setPullLoadEnabled(false);
        newPullListView.setScrollLoadEnabled(false);

        final String[] items = {getResources().getString(R.string.list_set), getResources().getString(R.string.list_delete), getResources().getString(R.string.list_cancel)};

        lData = initData();
        //生成一个listView
        listView=newPullListView.getRefreshableView();
        //设置listView的属性
        listView.setBackgroundColor(0x14000000);
        listView.setDivider(new ColorDrawable(0x00000000));
        listView.setDividerHeight(0);
        listView.setSelector(new ColorDrawable(0x14ffffff));
        //自定义的适配器
        mAdapter = new MyAdapter(this);
        listView.setAdapter(mAdapter);

        mg = AssetsDatabaseManager.getManager();
        db = mg.getDatabase("db");

        newPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView)
            {
                ThreadPool threadPool=ThreadPool.getThreadPool();
                threadPool.addTask(ListDeviceActivity.lRunnable);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView)
            {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(!lData.isEmpty()&&lData.get(position).getOnline()==1) {
                    Intent it = new Intent();
                    if (lData.get(position).getDetail() != null) {
                        it.putExtra("detail", lData.get(position).getDetail());
                    }
                    if (lData.get(position).getTid() != null) {
                        it.putExtra("tid", lData.get(position).getTid());
                    }
                    it.setClass(ListDeviceActivity.this, DeviceDetailActivity.class);
                    startActivity(it);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                new AlertDialog.Builder(ListDeviceActivity.this)
                        .setTitle(getResources().getString(R.string.list_alert_dialog_title))
                        .setItems(items,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(items[i].equals(getResources().getString(R.string.list_set))){
                            if(dialog!=null){
                                dialog.cancel();
                            }
                            if(!lData.isEmpty()&&lData.get(position).getOnline()==1) {
                                Intent r = new Intent();
                                if (lData.get(position).getTid() != null) {
                                    r.putExtra("tid", lData.get(position).getTid());
                                }
                                if (lData.get(position).getDetail() != null) {
                                    r.putExtra("detail", lData.get(position).getDetail());
                                }
                                if (lData.get(position).getName() != null) {
                                    r.putExtra("name", lData.get(position).getName());
                                }
                                r.setClass(ListDeviceActivity.this, SetActivity.class);
                                startActivity(r);
                            }
                            else{
                                Toast.makeText(ListDeviceActivity.this,getResources().getString(R.string.list_offline_device_un_rename_tip),Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (items[i].equals(getResources().getString(R.string.list_delete))){
                            if(dialog!=null){
                                dialog.cancel();
                            }
                            hekrUser= HekrUser.getInstance(MySettingsHelper.getCookieUser());
                            new AsyncTask<Integer, Integer, Boolean>(){

                                @Override
                                protected Boolean doInBackground(Integer... integers) {
                                    if(!lData.isEmpty()&&lData.get(position).getOnline()==1){
                                        boolean flag;
                                        flag=hekrUser.removeDevice(lData.get(position).getTid());
                                        return flag;
                                    }
                                    else {
                                        hekrUser.deleteDevice(lData.get(position).getTid());
                                        Log.d(TAG, "删除的tid:" + lData.get(position).getTid() + "删除函数返回值:" + hekrUser.deleteDevice(lData.get(position).getTid()));
                                        return hekrUser.deleteDevice(lData.get(position).getTid());
                                    }

                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    super.onPostExecute(aBoolean);
                                    if(aBoolean){

                                        int i=0;
                                        Iterator <DeviceSummary> it = lData.iterator();
                                        while(it.hasNext())
                                        {
                                            if(lData.get(position).equals(it.next()))
                                            {
                                                it.remove();
                                                i++;
                                                Log.i(TAG,"匹配次数："+i);
                                                mAdapter.notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                        Toast.makeText(ListDeviceActivity.this,getResources().getString(R.string.list_delete_success_tip),Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(ListDeviceActivity.this,getResources().getString(R.string.list_delete_fail_tip),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.execute();
                        }
                        if(items[i].equals(getResources().getString(R.string.list_cancel))){
                            if(dialog!=null){
                                dialog.cancel();
                            }
                        }
                    }
                }).show();
                return true;
            }
        });
        ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isAvailable())
        {
            listProgressBar = CustomProgress.show(ListDeviceActivity.this, getResources().getString(R.string.loading_device).toString(), true, null);
        }
        listHandler = new Handler()
        {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                newPullListView.onPullDownRefreshComplete();
                setLastUpdateTime();
                Bundle data = msg.getData();
                if(!lData.isEmpty()){
                    lData.clear();
                }

                if(mAdapter!=null){
                    mAdapter.notifyDataSetChanged();
                }
                String json_str = data.getString("json");
                if(json_str!=null){
                    try {
                        JSONArray json_list = new JSONArray(json_str);

                        for (int i=0;i<json_list.length();i++){
                            JSONObject item = json_list.getJSONObject(i);

                            DeviceSummary summary = new DeviceSummary();
                            if(item.has("detail")){
                                summary.setDetail(item.getString("detail"));

                            }
                            if(item.has("state")){
                                summary.setState(item.getString("state"));

                            }
                            else{
                                summary.setState(null);
                            }
                            if(item.has("name")){

                                summary.setName(item.getString("name") == null ? null : item.getString("name"));

                            }else{

                                summary.setName(null);

                            }
                            if(item.has("online")){
                                summary.setOnline(item.getInt("online"));
                            }
                            else{
                                summary.setOnline(0);
                            }
                            if(item.has("tid")){
                                summary.setTid(item.getString("tid"));
                            }

                            if(item.has("uid")){
                                summary.setUid(item.getString("uid"));
                            }
                            if(item.has("time")){
                                summary.setTime(item.getLong("time"));
                            }
                            if(!TextUtils.isEmpty(summary.getDetail())){
                                lData.add(summary);
                            }
                        }

                        mAdapter.notifyDataSetChanged();
                    }catch (Exception e){
                        Log.i("MyLog", "取设备信息出现异常："+e.getMessage());
                    }

                }
                if (listProgressBar!=null&&listProgressBar.isShowing()) {
                    listProgressBar.dismiss();
                }
            }
        };
        face = Typeface.createFromAsset(getAssets(),"font/hanxizhongyuantong.ttf");
    }

    private void createReceiver() {
        // 创建网络监听广播
        listRefreshReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
                {

                    ConnectivityManager mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                    if(netInfo != null && netInfo.isAvailable())
                    {
                        //网络连接
                        //String name = netInfo.getTypeName();
                        ThreadPool threadPool = ThreadPool.getThreadPool();
                        threadPool.addTask(ListDeviceActivity.lRunnable);
                        if(netInfo.getType()==ConnectivityManager.TYPE_WIFI)
                        {

                        }else if(netInfo.getType()==ConnectivityManager.TYPE_ETHERNET)
                        {


                        }else if(netInfo.getType()==ConnectivityManager.TYPE_MOBILE){

                        }
                    }
                    else {

                    }
                }
            }
        };
        // 注册网络监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(listRefreshReceiver, intentFilter);

    }

    //刷新时间
    private void setLastUpdateTime() {
        String text = formatDateTime(System.currentTimeMillis());
        newPullListView.setLastUpdatedLabel(text);
    }

    private String formatDateTime(long time) {
        if (0 == time) {
            return "";
        }

        return mDateFormat.format(new Date(time));
    }

    //获取设备信息
    static Runnable lRunnable = new Runnable()
    {
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("json", HttpHelper.getUserDevice());
            msg.setData(data);
            listHandler.sendMessage(msg);
        }
    };
    private List<DeviceSummary> initData() {
        List<DeviceSummary> list = new ArrayList<DeviceSummary>();
        //ToDO 加载本地数据
        return list;
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView name;
        public TextView statusOne;
        public TextView message;
        public LinearLayout device_item_ly;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public MyAdapter(Context context){
            //根据context上下文加载布局，这里的是ListDeviceActivity本身，即this
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if(lData.isEmpty()){
                return 0;
            }
            return lData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            //获取数据集中与指定索引对应的数据项
            return position;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            //获取在列表中与指定索引对应的行id
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {

            ViewHolder holder = new ViewHolder();
            //根据自定义的Item布局加载布局
            convertView = mInflater.inflate(R.layout.deviceitem, null);

            //产品类型图片
            holder.img = (ImageView) convertView.findViewById(R.id.device1);
            holder.name= (TextView) convertView.findViewById(R.id.devicename);
            holder.statusOne=(TextView) convertView.findViewById(R.id.statusone);
            holder.message=(TextView) convertView.findViewById(R.id.message);
            holder.device_item_ly= (LinearLayout) convertView.findViewById(R.id.device_item_ly);


            holder.name.setTypeface(face);
            if(!lData.isEmpty()&&lData.size()!=0)
            {
                //设备图片
                if(lData.get(position)!=null){
                    Bitmap bitmap = getIcon(lData.get(position));
                    if(bitmap!=null){
                        holder.img.setImageBitmap(bitmap);
                    }
                }
                //设备名称
                if(TextUtils.isEmpty(lData.get(position).getName())) {
                    String stid = "";
                    try {
                        stid = lData.get(position).getTid();
                        if(stid!=null&&stid.length()>2)
                        {
                            stid = " "+stid.substring(stid.length() - 2);
                        }
                    }catch (Exception ex){
                    }
                    holder.name.setText(getName(lData.get(position)) + stid );
                }else
                {
                    holder.name.setText(lData.get(position).getName());
                }
                //设备开关情况
                holder.statusOne.setTypeface(face);
                if(getPower(lData.get(position))==0||getPower(lData.get(position))==1)
                {
                    if (getPower(lData.get(position))==1)
                    {
                        holder.statusOne.setTextColor(0xffffffff);
                        holder.statusOne.setText(getResources().getString(R.string.open));
                    }
                    else
                    {
                        holder.statusOne.setTextColor(0x35ffffff);
                        holder.statusOne.setText(getResources().getString(R.string.close));
                    }
                }else{
                    //服务器下发数据当中没有power字段则显示离线
                    holder.statusOne.setText("");
                }
                //设备在线情况,不在线无焦点，不可以点击进去操作设备
                holder.message.setTypeface(face);
                if(lData.get(position).getOnline()==1||lData.get(position).getOnline()==0)
                {
                    if(lData.get(position).getOnline()==1)
                    {
                        holder.message.setTextColor(0xffffffff);
                        if(returnDisplayData(lData.get(position))!=null){
                            holder.message.setText(""+ returnDisplayData(lData.get(position)));
                        }else{
                            holder.message.setText( R.string.online );
                        }
                    }else{
                        holder.message.setTextColor(0x35ffffff);
                        holder.message.setText(getResources().getString(R.string.offline));
                    }
                }
                else{
                    holder.message.setText("");
                }
            }

            return convertView;
        }

        //获取设备图片
        public Bitmap getIcon(DeviceSummary device) {
            String detail = device.getDetail();
            String iconUrl="";
            if(!TextUtils.isEmpty(detail)){
                iconUrl = mg.getIconUrlByCid(""+ getDetailMap(detail).get("cid"));
            }
            InputStream is=null;
            InputStream iis=null;
            InputStream iiis=null;
            try
            {
                is=getAssets().open(iconUrl);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;

            } catch (Exception e)
            {
                try{
                    Bitmap bitmap = BitmapFactory.decodeFile(iconUrl);
                    if(bitmap==null){
                        iis=getAssets().open("product/weizhi.png");
                        Bitmap citmap = BitmapFactory.decodeStream(iis);
                        iis.close();
                        return citmap;
                    }else{
                        return bitmap;
                    }
                }catch (Exception ee){
                    try {
                        iiis=getAssets().open("product/weizhi.png");
                        Bitmap bitmap = BitmapFactory.decodeStream(iiis);
                        iiis.close();
                        return bitmap;
                    } catch (Exception eee)
                    {
                        return null;
                    }
                }

            }
            finally {
                try {
                    if(is!=null){
                        is.close();
                    }
                    if(iis!=null){
                        iis.close();
                    }
                    if(iiis!=null){
                        iiis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        //设备名称
        public String getName(DeviceSummary device) {
            String detail = device.getDetail();
            String countryCategory=getResources().getConfiguration().locale.getCountry();
            Cursor cursor = null;
            String cid="";

            if(!TextUtils.isEmpty(detail)){
                cid=getDetailMap(detail).get("cid")+"";
            }

            if("".equals(cid)||"null".equals(cid))
            {
                return "unknow";
            }

            if(countryCategory.equals("CN")){
                try {
                    cursor = db.rawQuery("select name from category where id=?",
                            new String[]{cid});
                    if (cursor.moveToNext()) {
                        return cursor.getString(0);
                    }
                    return "unknow";
                }catch (Exception e){
                    return "unknow";

                }finally {
                    if(cursor!=null) {
                        cursor.close();
                    }
                }
            }
            else{
                try {
                    cursor = db.rawQuery("select ename from category where id=?",
                            new String[]{cid});
                    if (cursor.moveToNext()) {
                        return cursor.getString(0);
                    }
                    return "unknow";
                }catch (Exception e){
                    return "unknow";

                }finally {
                    if(cursor!=null) {
                        cursor.close();
                    }
                }
            }

        }

        //设备开关状态
        public int getPower(DeviceSummary device) {
            String state = device.getState();
            if(!TextUtils.isEmpty(state))
            {
                if(!TextUtils.isEmpty(getDetailMap(state).get("power").toString().trim()))
                {
                    if(((com.lambdatm.runtime.lang.Number)getDetailMap(state).get("power")).intValue()==0||((com.lambdatm.runtime.lang.Number)getDetailMap(state).get("power")).intValue()==1)
                    {
                        return ((com.lambdatm.runtime.lang.Number)getDetailMap(state).get("power")).intValue();
                    }
                    else{
                        return 2;
                    }
                }
                return 2;
            }
            return 2;
        }

        public Object returnDisplayData(DeviceSummary device) {
            if(device.getState()!=null)
            {
                return returnDisplayData( getDetailMap (device.getState()));
            }
            return null;
        }

        public Object returnDisplayData(Map state) {
            try {
                if (state.containsKey("humidity")) {
                    double a = ((com.lambdatm.runtime.lang.Number) state.get("humidity")).doubleValue();
                    int b = (int) a;
                    double c = (double) b;
                    //判断是否为浮点型、整形
                    if (a == c) {
                        return ((com.lambdatm.runtime.lang.Number) state.get("humidity")).intValue() + "%";
                    }
                    return ((com.lambdatm.runtime.lang.Number) state.get("humidity")).doubleValue() + "%";
                } else if (state.containsKey("temperature")) {
                    return ((com.lambdatm.runtime.lang.Number) state.get("temperature")).doubleValue() + "°C";
                } else {
                    return ((com.lambdatm.runtime.lang.Number) state.get("power")).intValue() == 0 ? getResources().getString(R.string.close).toString() : getResources().getString(R.string.open).toString();
                }
            }catch (Exception ex){
                return null;
            }
        }

    }

    public static List<Object> getDetailList(String detail) {
        try{

        if (!TextUtils.isEmpty(detail)) {
            List stateList = Util.tolist((Cell) Base.read.pc(detail, null));
            return stateList;
        }
        }catch (Exception e){
            return Util.tolist((Cell) Base.read.pc("(\"mid\" 0 \"pid\" 0 \"cid\" 0 )", null));
        }
        return Util.tolist((Cell) Base.read.pc("(\"mid\" 0 \"pid\" 0 \"cid\" 0 )", null));
    }

    public static Map<Object, Object> getDetailMap(String detail) {

        if(!TextUtils.isEmpty(detail)){

            List stateList = getDetailList(detail);
            if(stateList!=null&&stateList.size()>=2){
                Map<Object, Object> detailMap=new HashMap<Object, Object>();
                try {
                    for (int i = 0; i < stateList.size(); i = i + 2) {
                        detailMap.put(stateList.get(i), stateList.get(i + 1));
                    }
                }catch(Exception ex){
                }
                return detailMap;
            }
            return null;
        }
        return null;
    }

    @Override
    protected void onResume() {
        createReceiver();
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listProgressBar!=null&&listProgressBar.isShowing()) {
            listProgressBar.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if(listRefreshReceiver!=null){
            unregisterReceiver(listRefreshReceiver);
        }
        super.onDestroy();
    }
}
