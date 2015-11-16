package com.hekr.android.app.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;



import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
//设备图片
class ProductIconUpdateRunnable implements Runnable{
    @Override
    public void run() {
        String productIconList = HttpHelper.getProductIconList();      
        if(productIconList!=null&&productIconList!="")
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
                        if(normalLogoUrl!=null){
                            Log.d("MyLog","需要增加的网络图片路径："+normalLogoUrl);
                        }
                        String imageURL=normalLogoUrl;
                        String realPath="/mnt/sdcard/Hekr/"+imageURL.substring(imageURL.lastIndexOf("/") + 1);
                        if(realPath!=null)
                        {
                            Log.d("MyLog",realPath);
                        }                      
                        if(!("".equals(normalLogoUrl))&&normalLogoUrl!=null){
                            AsyncBitmapLoader.SaveBitmap(imageURL);
                            db.execSQL("insert into category(id,name,ename,logo_url,updated_at) values(?,?,?,?,?)",
                                    new String[]{id,name,ename,realPath,updated});
                        }
                    }
                    if(mg.needProductIconUpdate(id, updated))
                    {
                        JSONObject logoUrlJson = iconInfo.optJSONObject("logo_url");
                        String normalLogoUrl = logoUrlJson.getString("normal");
                        String imageURL=normalLogoUrl;
                        String realPath="";
                        if(!("".equals(imageURL))&&imageURL!=null){
                            realPath="/mnt/sdcard/Hekr/"+imageURL.substring(imageURL.lastIndexOf("/") + 1);
                        }
                        if(!("".equals(imageURL))&&imageURL!=null){
                            AsyncBitmapLoader.SaveBitmap(imageURL);
                            db.execSQL("update category set id=?,updated_at=?,logo_url=? where id=?",
                                    new String[]{id,updated,realPath,id});
                        }
                    }
                    Cursor cursor=null;
                    try {
                        cursor = db.rawQuery("select id,name,ename,logo_url,updated_at from category where id=?",
                                new String[]{id});
                        if (cursor.moveToNext()) {
                            //Log.d("MyLog", "修改后的id:"+cursor.getString(0)+"修改后的name:"+cursor.getString(1)+"修改后的ename:"+cursor.getString(2)+"修改后的图片路径:" + cursor.getString(3)+"修改后的时间："+cursor.getString(4));
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



