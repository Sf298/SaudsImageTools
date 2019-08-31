/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools.external;

/**
 * Runs the frameCall function at a regular rate. Provides tools to analyse when
 * the frameCall function takes too much time.
 * @author saud
 */
public abstract class FrameTimer {
	
	private final int delay;
	private long nextTime;
	private int frameCount = 0;
	private boolean toStop = false;
	private boolean isStopped = false;
	private long lagMs = 0;
	
	public FrameTimer(int fps) {
		delay = 1000/fps;
	}
	
	public abstract void frameCall(int frameNo, double lagMs);
	
	public void start() throws InterruptedException {
		while(!toStop) {
			nextTime = System.currentTimeMillis() + delay;
			frameCall(frameCount, lagMs);
			frameCount++;
			long sleepTime = nextTime-System.currentTimeMillis();
			lagMs = sleepTime;
			if(sleepTime > 0) {
				Thread.sleep(sleepTime);
			}
		}
		isStopped = true;
	}
	
	public void stop() {
		toStop = true;
	}
	
	public boolean isStopped() {
		return isStopped;
	}
	
	public long dTime() {
		return lagMs;
	}
	
	public int getDelay() {
		return delay;
	}
	
}
