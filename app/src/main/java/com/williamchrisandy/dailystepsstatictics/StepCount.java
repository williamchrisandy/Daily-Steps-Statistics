package com.williamchrisandy.dailystepsstatictics;

import java.util.ArrayList;
import java.util.Vector;

public class StepCount
{
    private String deviceId;
    private long[] stepCount;
    private long totalstepCount;
    private boolean[] isCounted;

    public StepCount(String deviceId, int minute)
    {
        int i;
        this.deviceId = deviceId;
        stepCount = new long[1445];
        isCounted = new boolean[1445];
        for(i=0; i<=1440; i++)
        {
            stepCount[i] = 0;
            isCounted[i] = false;
        }
        stepCount[minute] = 1;
        isCounted[minute] = true;
        totalstepCount = 1;
    }

    public void addStep(int minute)
    {
        totalstepCount += 1;
        stepCount[minute] = totalstepCount;
        isCounted[minute] = true;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public long getStepCount()
    {
        return totalstepCount;
    }

    public boolean isEnabled(int minutes)
    {
        return isCounted[minutes];
    }

    public long getCount(int minutes)
    {
        return stepCount[minutes];
    }

}
