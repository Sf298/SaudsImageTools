/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools.external;

/**
 *
 * @author saud
 */
public class SmallCache {
	
	public Integer[] arr;
	public final int size;
	
	public SmallCache(int size) {
		this.size = size;
		arr = new Integer[size];
	}
	
	public void add(Integer i) {
		shiftRight();
		arr[size-1] = i;
	}
	
	public void clear() {
		arr = new Integer[size];
	}
	
	private void shiftRight() {
		for(int i=1; i<size; i++) {
			arr[i-1] = arr[i];
		}
	}
	
}
