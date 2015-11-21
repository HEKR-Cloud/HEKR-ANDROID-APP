package com.hekr.android.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.*;

/**
 * Created by xubukan on 2015/3/29.
 */
public class AsyncBitmapLoader
{
    /**
     * 内存图片软引用缓冲
     */
    private static String CachePath = "/mnt/sdcard/Hekr/";

    public AsyncBitmapLoader()
    {
    }

    public static Bitmap SaveBitmap(final String imageURL) {

        Runnable iconDownRunnable = new Runnable() {

            public void run() {
                InputStream bitmapIs = HttpHelper.getStreamFromURL(imageURL);
                Bitmap bitmap=null;
                if(bitmapIs!=null){
                    bitmap = BitmapFactory.decodeStream(bitmapIs);
                }
                FileOutputStream fos = null;
                File dir = new File(CachePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File bitmapFile = new File(CachePath +
                        imageURL.substring(imageURL.lastIndexOf("/") + 1));
                try {
                    if(!bitmapFile.exists()){
                        try {
                            bitmapFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    fos = new FileOutputStream(bitmapFile);
                    if(bitmap!=null){
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);// 把数据写入文件
                    }
                } catch (Exception e) {
                    Log.d("MyLog","save图片时流转换成bitmap出错:"+e.getMessage());
                } finally {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        };
        iconDownRunnable.run();
        return null;
    }
}
