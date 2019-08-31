/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools;

import sauds.image.tools.external.AdvancedxMaths;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * The container class for generating and storing non-rectangular regions of
 * non-zero values.
 * @author saud
 */
public class Blob {
	
	private long sumX = 0;
	private int minX = Integer.MAX_VALUE, maxX = 0;
	private long sumY = 0;
	private int minY = Integer.MAX_VALUE, maxY = 0;
	private ArrayList<int[]> points = new ArrayList<>();
	
	/**
	 * Add a new point to this blob.
	 * @param x
	 * @param y 
	 */
	public void addPoint(int x, int y) {
		points.add(new int[]{x,y});
		sumX += x;
		if(x < minX) minX = x;
		if(x > maxX) maxX = x;
		if(y < minY) minY = y;
		if(y > maxY) maxY = y;
		sumY += y;
		clearCache();
	}
	
	/**
	 * Gets a coordinate from this blob.
	 * @param i the index of the coordinate (in order of points added).
	 * @return 
	 */
	public int[] getPoint(int i) {
		return points.get(i);
	}
	
	/**
	 * Gets the mean X coordinate
	 * @return 
	 */
	public double getMeanX() {
		return (sumX/(double)getSize());
	}
	/**
	 * Gets the mean Y coordinate
	 * @return 
	 */
	public double getMeanY() {
		return (sumY/(double)getSize());
	}
	
	/**
	 * Gets number of points added
	 * @return 
	 */
	public int getSize() {
		return points.size();
	}
	
	/**
	 * Gets the bounding box that encompasses all points in this blob
	 * @return 
	 */
	public Rectangle getBBox() {
		return new Rectangle(minX, minY, maxX-minX+1, maxY-minY+1); // +1 is to include outer bounds of coords
	}
	
	/**
	 * Calculates the bounding box density. I.e. the number of points divided by
	 * the size of the bounding box. 
	 * @return 
	 */
	public double getBBoxDensity() {
		return getSize()/(double)((maxX-minX+1)*(maxY-minY+1));
	}
	
	/**
	 * Performs a principal component analysis on the points added.
	 */
	private void calcPCA() {
		//https://alyssaq.github.io/2015/computing-the-axes-or-orientation-of-a-blob/
		double a = covarianceX();
		double b = covarianceXY();
		double d = covarianceY();
		double[] eVals = AdvancedxMaths.eigenValues(a, b, b, d);
		double[][] eVecs = AdvancedxMaths.eigenVectors(a, b, b, d, eVals);
		double xV, yV;
		if(eVals[0] > eVals[1]) {
			xV = eVecs[0][0];
			yV = eVecs[0][1];
		} else {
			xV = eVecs[1][0];
			yV = eVecs[1][1];
		}
		angle = Math.tanh(xV/yV);
		
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (int[] point : points) {
			double[] rotated = AdvancedxMaths.rotateTransform(point[0], point[1], angle);
			minX = Math.min(minX, rotated[0]);
			maxX = Math.max(maxX, rotated[0]);
			minY = Math.min(minY, rotated[1]);
			maxY = Math.max(maxY, rotated[1]);
		}
		double w = maxX-minX;
		double h = maxY-minY;
		majorAxis = Math.max(w, h)+1; // +1 is to include outer bounds of coords
		minorAxis = Math.min(w, h)+1;
	}
	
	private Double angle, minorAxis, majorAxis;
	/**
	 * Gets the angle of the blob.
	 * @return 
	 */
	public double getAngle() {
		if(angle==null) calcPCA();
		return angle;
	}
	/**
	 * Gets the major axis of the blob in the direction of the angle.
	 * @return 
	 */
	public double getMajorAxis() {
		if(minorAxis==null) calcPCA();
		return minorAxis;
	}
	/**
	 * Gets the minor axis of the blob in perpendicular to the direction of the angle.
	 * @return 
	 */
	public double getMinorAxis() {
		if(majorAxis==null) calcPCA();
		return majorAxis;
	}
	/**
	 * Calculates the density of the blob. I.e. the number of points divided by
	 * the area of the major and minor axes. 
	 * @return 
	 */
	public double getAxisDensity() {
		return getSize()/(double)(getMinorAxis()*getMajorAxis());
	}
	
	private Double covCacheXY = null;
	private Double covCacheX = null;
	private Double covCacheY = null;
	private double covarianceXY() {
		if(covCacheXY != null) return covCacheXY;
		
		double[][] t1 = new double[getSize()][2];
		for(int i=0; i<getSize(); i++)
			for(int j=0; j<2; j++)
				t1[i][j] = points.get(i)[j];
		
		return covCacheXY = AdvancedxMaths.covariance(t1);
	}
	private double covarianceX() {
		if(covCacheX != null) return covCacheX;
		
		double[][] t1 = new double[getSize()][2];
		for(int i=0; i<getSize(); i++) {
			t1[i][0] = points.get(i)[0];
			t1[i][1] = points.get(i)[0];
		}
		return covCacheX = AdvancedxMaths.covariance(t1);
	}
	private double covarianceY() {
		if(covCacheY != null) return covCacheY;
		
		double[][] t1 = new double[getSize()][2];
		for(int i=0; i<getSize(); i++) {
			t1[i][0] = points.get(i)[1];
			t1[i][1] = points.get(i)[1];
		}
		return covCacheY = AdvancedxMaths.covariance(t1);
	}
	private void clearCache() {
		covCacheXY = null;
		covCacheX = null;
		covCacheY = null;
		angle = minorAxis = majorAxis = null;
	}
	
}
