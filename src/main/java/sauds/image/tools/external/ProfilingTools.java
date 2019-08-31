/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools.external;

import java.util.ArrayList;

/**
 *
 * @author saud
 */
public class ProfilingTools {
	
	private static ArrayList<Long> timeAccumilated = new ArrayList<>();
	private static ArrayList<Long> timeResumed = new ArrayList<>();
	private static ArrayList<Long> resumeCount = new ArrayList<>();
	
	public static void start(int id) {
		expandTo(id);
		resumeCount.set(id, resumeCount.get(id)+1);
		timeResumed.set(id, System.currentTimeMillis());
	}
	
	public static void stop(int id) {
		long currTime = System.currentTimeMillis();
		long timeSinceStart = currTime-timeResumed.get(id);
		timeAccumilated.set(id, timeAccumilated.get(id)+timeSinceStart);
		timeResumed.set(id, -1l);
	}
	
	public static boolean isPaused(int id) {
		return timeResumed.get(id) == -1;
	}
	
	public static long getTotalTime(int id) {
		assertPaused(id);
		return timeAccumilated.get(id);
	}
	public static void stopAndPrintTotalTime(int id, String name) {
		stop(id);
		printTotalTime(id, name);
	}
	public static void printTotalTime(int id, String name) {
		System.out.println(name+" total: "+getTotalTime(id));
	}
	
	public static long getMeanTime(int id) {
		assertPaused(id);
		return timeAccumilated.get(id) / resumeCount.get(id);
	}
	public static void stopAndPrintMeanTime(int id, String name) {
		stop(id);
		printMeanTime(id, name);
	}
	public static void printMeanTime(int id, String name) {
		System.out.println(name+" mean: "+getMeanTime(id));
	}
	
	public static int getIdCount() {
		return timeAccumilated.size();
	}
	
	public static void reset(int id) {
		timeAccumilated.set(id, 0l);
		timeResumed.set(id, 0l);
	}
	
	public static void resetAll() {
		timeAccumilated.clear();
		timeResumed.clear();
	}
	
	private static void expandTo(int id) {
		while(timeAccumilated.size() <= id) {
			timeAccumilated.add(0l);
			timeResumed.add(0l);
			resumeCount.add(0l);
		}
	}
	
	private static void assertPaused(int id) {
		if(!isPaused(id))
			throw new IllegalStateException("Timer with id: "+id+" is still running");
	}
	
}
