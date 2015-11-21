package com.hekr.android.app.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by xubukan on 2015/3/29.
 */
public class CacheHelper {

    public static void doUpdateProductIconList(){
        ProductIconUpdateRunnable productIconUpdateRunnable = new ProductIconUpdateRunnable();
        ThreadPool threadPool = ThreadPool.getThreadPool();
        threadPool.addTask(productIconUpdateRunnable);
    }
}

class ProductIconUpdateRunnable implements Runnable{
    @Override
    public void run() {
        String productIconList = HttpHelper.getProductIconList();

        if(!TextUtils.isEmpty(productIconList))
        {
            productIconList = productIconList.replace("categoryCB(","").replace(");", "");
            try{
                JSONObject json = new JSONObject(productIconList);
                Iterator it = json.keys();

                while (it.hasNext()){

                    String id = it.next().toString();
                    JSONObject iconInfo = json.optJSONObject(id);
                    String updated = iconInfo.optString("updated_at");
                    String name = iconInfo.optString("name");
                    String ename = iconInfo.optString("ename");

                    AssetsDatabaseManager mg = AssetsDatabaseManager.getManager();
                    SQLiteDatabase db = mg.getDatabase("db");
                    if(mg.needProductIconInsert(id)){

                        JSONObject logoUrlJson = iconInfo.optJSONObject("logo_url");
                        String normalLogoUrl = logoUrlJson.getString("normal");
                        if(!TextUtils.isEmpty(normalLogoUrl))
                        {
                            Log.d("MyLog","需要增加的网络图片路径："+normalLogoUrl);
                        }
                        String imageURL=normalLogoUrl;
                        String realPath="/mnt/sdcard/Hekr/"+imageURL.substring(imageURL.lastIndexOf("/") + 1);
                        if(!TextUtils.isEmpty(realPath))
                        {
                            Log.d("MyLog",realPath);
                        }
                        Log.i("MyLog","插入数据库的数据：id:"+id+"--name:"+name+"--logo_url:"+realPath+"--updated_at:"+updated);
                        db.execSQL("insert into category(id,name,ename,logo_url,updated_at) values(?,?,?,?,?)",
                                new String[]{id, name, ename, realPath, updated});
                        if(!TextUtils.isEmpty(normalLogoUrl)){
                            AsyncBitmapLoader.SaveBitmap(imageURL);
                        }
                    }
                    if(mg.needProductIconUpdate(id, updated))
                    {

                        JSONObject logoUrlJson = iconInfo.optJSONObject("logo_url");
                        String normalLogoUrl = logoUrlJson.getString("normal");
                        if(!TextUtils.isEmpty(normalLogoUrl)){
                            Log.d("MyLog","需要更新的网络图片路径："+normalLogoUrl);
                        }
                        String imageURL=normalLogoUrl;
                        String realPath="";
                        if(!TextUtils.isEmpty(imageURL)){
                            realPath="/mnt/sdcard/Hekr/"+imageURL.substring(imageURL.lastIndexOf("/") + 1);
                        }

                        if(!TextUtils.isEmpty(realPath))
                        {
                            Log.d("MyLog",realPath);
                        }
                        db.execSQL("update category set id=?,updated_at=?,logo_url=? where id=?",
                                new String[]{id,updated,realPath,id});
                        if(!TextUtils.isEmpty(imageURL)){
                            AsyncBitmapLoader.SaveBitmap(imageURL);
                        }
                    }
                    Cursor cursor=null;
                    try {
                        cursor = db.rawQuery("select id,name,ename,logo_url,updated_at from category where id=?",
                                new String[]{id});
                        if (cursor.moveToNext()) {
                        } else {
                            Log.d("MyLog", "查询数据库路径失败");
                        }
                    }catch (Exception e){
                        Log.d("MyLog","异常：查询数据:"+e.getMessage());
                    }finally {
                        if(cursor!=null) {
                            cursor.close();
                        }
                    }
                }
            }catch (Exception e){
                Log.d("MyLog", "缓存图片抛异常:"+e.toString());
            }

        }
    }
}
