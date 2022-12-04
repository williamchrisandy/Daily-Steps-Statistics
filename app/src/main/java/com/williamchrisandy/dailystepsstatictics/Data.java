package com.williamchrisandy.dailystepsstatictics;

import android.content.Context;
import android.provider.Settings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Data
{
    private String deviceId;
    private String date;

    public Data(String deviceId, String date)
    {
        this.deviceId = deviceId;
        this.date = date;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public Map<String, Object> toMap()
    {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("deviceId", deviceId);
        hashMap.put("date", date);

        return hashMap;
    }

}
