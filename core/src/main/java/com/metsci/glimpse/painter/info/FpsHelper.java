package com.metsci.glimpse.painter.info;

public class FpsHelper {
	private float lastFpsEstimate = -1;
    private long timeOfLastCounterReset = -1;
    private float frameCount = 0;
    public FpsHelper()
    {
    }
    public FpsHelper(float lastEstimate, long timeOfLastCounterReset, float frameCount)
    {
    	lastFpsEstimate = lastEstimate;
    	this.timeOfLastCounterReset = timeOfLastCounterReset;
    	this.frameCount = frameCount;
    }
    public float getLastFpsEstimate()
    {
    	return lastFpsEstimate;
    }
    public long getTimeOfLastCounterReset()
    {
    	return timeOfLastCounterReset;
    }
    public float getFrameCount()
    {
    	return frameCount;
    }
    public void setFpsEstimate(float estimate)
    {
    	lastFpsEstimate = estimate;
    }
    public void setTimeOfCounterReset(long timeOfReset)
    {
    	timeOfLastCounterReset = timeOfReset;
    }
    public void setFrameCount(float frameCount)
    {
    	this.frameCount = frameCount;
    }
}
