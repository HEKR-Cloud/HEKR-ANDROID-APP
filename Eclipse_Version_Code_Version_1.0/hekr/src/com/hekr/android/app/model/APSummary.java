package com.hekr.android.app.model;

/**
 * Created by xubukan on 2015/3/28.
 */
//路由器信息类
public class APSummary 
{
    private String auth_suites;
    private String ssid;
    private Integer channel;
    private Integer signal;
    private String bssid;


    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }
    public String getAuth_suites() {
        return auth_suites;
    }

    public void setAuth_suites(String auth_suites) {
        this.auth_suites = auth_suites;
    }

}
