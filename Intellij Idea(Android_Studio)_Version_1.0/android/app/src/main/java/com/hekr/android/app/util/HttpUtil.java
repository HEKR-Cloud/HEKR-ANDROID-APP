package com.hekr.android.app.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kj on 15/7/24.
 */
public class HttpUtil {

    public static String doGet(String url,String cookie){
        String backCode="";
        url= url.replaceAll(" ", "%20");
        HttpGet http = new HttpGet(url);

        if(cookie != null) {
            http.addHeader("cookie", "u="+cookie);
        }
        try{
            HttpResponse res = new DefaultHttpClient().execute(http);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = res.getEntity();
                backCode=EntityUtils.toString(entity, HTTP.UTF_8);
                return backCode;
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    public static String doPost(String url , String cookie , String name){
        String result="";
        HttpPost http = new HttpPost(url);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));

        if(cookie != null) {
            http.addHeader("cookie", cookie);
        }
        try{
            DefaultHttpClient dhc = new DefaultHttpClient();
            http.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
            HttpResponse res = dhc.execute(http);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = res.getEntity();
                result=EntityUtils.toString(entity, HTTP.UTF_8);
                return result;
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

}
