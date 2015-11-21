package com.hekr.android.app.util;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xubukan on 2015/3/23.
 */
public class HttpHelper {

    private static String seed = getRandomString(4);

    public static String doGet(String url) {
        HttpGet http = new HttpGet(url+"_csrftoken_="+seed);

        String CookieUser=MySettingsHelper.getCookieUser();

        http.addHeader("cookie","u="+CookieUser+";_csrftoken_="+seed);
        try{
            HttpResponse res = new DefaultHttpClient().execute(http);

            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = res.getEntity();
                return EntityUtils.toString(entity, HTTP.UTF_8);

            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    //在热点设备上设置生成的key值
    public static String doGet(String ak,int i) {
        try{
            String str="http://192.168.10.1/t/set_ak?ak="+ak;
            HttpGet http = new HttpGet(str);
            HttpResponse res = new DefaultHttpClient().execute(http);

            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = res.getEntity();
                return EntityUtils.toString(entity, HTTP.UTF_8);

            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    //从服务器下载图片
    public static InputStream getStreamFromURL(String imageURL) {
        InputStream in=null;
        try {
            URL url=new URL(imageURL);
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200){
                in=connection.getInputStream();
                return in;
            }
            else{
                return null;
            }

        } catch (Exception e) {
            Log.d("MyLog","in---:"+e.getMessage()+"-- "+imageURL);
            return null;
        }
    }

    //得到设备
    public static String getUserDevice() {
        String url = "http://user.hekr.me/device/list.json?";

        return doGet(url);
    }

    //send吐槽
    public static String doPost(String uriApI,String userAccessKey,String content) {

        String result = "";
        HttpPost httpRequst = new HttpPost(uriApI);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("userAccessKey", userAccessKey));
        params.add(new BasicNameValuePair("content", content));

        try {
            httpRequst.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequst);
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);//取出应答字符串
            }
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        catch (ClientProtocolException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
        return result;
    }

    public static String getProductIconList() {
        String url = "http://poseido.hekr.me/appcategories.json?";
        //String url = "http://192.168.1.81:8080/poseido/appcategories.json?";
        return doGet(url);
    }

    private static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}
