package com.hekr.android.app.util;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.ccxx.websocket.WebSocketConnection;
import com.ccxx.websocket.WebSocketConnectionHandler;

import com.ccxx.websocket.WebSocketException;
import com.hekr.android.app.model.Global;

public class HekrWebSocket {
	private final static String TAG = "HekrWebSocket";
    private String wsuri = "ws://device.hekr.me:8080/websocket/t/"; 
	private boolean webSocketConnected = false;
    private WebSocketConnection wsc = new WebSocketConnection();
    private Timer pingTimer = new Timer();
    
    private Handler recHandler;
    public final static int MSG_REC_UART_DATA = 16001;

    public HekrWebSocket(Handler handler, String tid, String user) {
    	this.recHandler = handler;
    	try {
    	   wsuri +=  tid + "/code/" + Global.USERACCESSKEY + "/" +user;
    	   wsc.connect(wsuri, new WebSocketConnectionHandler() {
			   public void onOpen()
			   {
				   if (true)
					   Log.d(TAG, "Connected to " + wsuri);
				   webSocketConnected = true;
			   }
			   public void onClose(int code, String reason) 
			   {
				   if (true)
					   Log.d(TAG, "Connection lost.");
				   webSocketConnected = false;
			   }
			   public void onTextMessage(String payload) 
			   {
				   if (true)
					   Log.d(TAG, "rec: " + payload);
                   if (!TextUtils.isEmpty(payload))
                   {
                       Bundle data=new Bundle();
                       Message msg = new Message();
                       data.putString("payload",payload);
                       msg.setData(data);
                       msg.what = MSG_REC_UART_DATA;

                       recHandler.sendMessage(msg);
                   }
                   if ((payload.length() > 8)&&(payload.equals("(getall )")))
                   {
                       pingTimer.schedule(taskPing, 20000, 20000);
                   }

			   }
          });
       } catch (WebSocketException e) {
    	   e.printStackTrace();
       }
   }
    
    public void changeHandler(Handler handler)
    {
    	this.recHandler = handler;
    }
    
    public boolean checkWebSocketLinked()
    {
    	return webSocketConnected;
    }
    
    public void SendText(String buf)
    {
    	wsc.sendTextMessage(buf);
    }
    
    public void sendDevCall(String tid) {

    	if (webSocketConnected == false)
    	{
    		Log.d(TAG, "webSocketConnected false");
    		return;
    	}
        String send = "(@devcall "+'"'+ tid +'"'+"(dev.upgrade " +'"'+ tid +'"'+" "+'"'+'"'+" "+'"'+'"'+" "+'"'+'"'+ ")(lambda (x) x))\n";

		Log.d(TAG, send);

		wsc.sendTextMessage(send);
    }
    
	TimerTask taskPing = new TimerTask() {  
        @Override  
        public void run() {
            if(checkWebSocketLinked()){
                wsc.sendTextMessage("(ping)\n");
            }
        }
	};
	
	public static  byte calculateSum(byte[] src) {
		byte sum = 0;
		for (int i=0; i<src.length; i++)
		{
			sum += src[i];
		}
		return sum;
	}
	
	public static String bytesToHexString(byte[] src){  
        StringBuilder stringBuilder = new StringBuilder("");  
        if (src == null || src.length <= 0) {  
            return null;  
        }  
        for (int i = 0; i < src.length; i++) {  
            int v = src[i] & 0xFF;  
            String hv = Integer.toHexString(v);  
            if (hv.length() < 2) {  
                stringBuilder.append(0);  
            }  
            stringBuilder.append(hv);  
        }  
        return stringBuilder.toString();  
    }
	
	private static byte toByte(char c) {
	    byte b = (byte) "0123456789ABCDEF".indexOf(c);
	    return b;
	}

	private static byte[] hexStringToByte(String hex) {
	    int len = (hex.length() / 2);
	    byte[] result = new byte[len];
	    char[] achar = hex.toCharArray();
	    for (int i = 0; i < len; i++) {
	     int pos = i * 2;
	     result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
	    }
	    return result;
	}
}
