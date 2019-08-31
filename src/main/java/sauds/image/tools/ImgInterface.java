/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.imageio.ImageIO;
import static sauds.image.tools.Img.nextPow2;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

/**
 *
 * @author saud
 */
public abstract class ImgInterface {
    
	/**
	 * Behaves like the unchecked get(). Mainly used for the convolution function.
	 */
    public static final int BORDER_INNER = 0;
	/**
	 * Requests for pixels outside the bounds return null.
	 */
    public static final int BORDER_IGNORE = 1;
	/**
	 * Requests for pixels outside the bounds return the value of the nearest
	 * pixel within the bounds.
	 */
    public static final int BORDER_EXTEND = 2;
    //public static final int BORDER_WRAP = 3;
	
    public static final int CROP_CENTER = 0;
    public static final int CROP_NORTH = 1;
    public static final int CROP_SOUTH = 2;
    public static final int CROP_EAST = 3;
    public static final int CROP_WEST = 4;
    
    public static int threadCount = MPT.coreCount()-1;
    protected int width,height,channels;
    private int subPixelCount = -1;
	
	/**
	 * Create a new blank image of given size.
	 * @param width
	 * @param height
	 * @param channels
	 * @return 
	 */
    public abstract ImgInterface create(int width, int height, int channels);
	/**
	 * Create a new image of given size. It is then filled with the given value.
	 * @param width
	 * @param height
	 * @param channels
	 * @param initialValue
	 * @return 
	 */
    public abstract ImgInterface create(int width, int height, int channels, byte initialValue);
	/**
	 * Create a new image of given size. It is then filled with the given value.
	 * @param width
	 * @param height
	 * @param channels
	 * @param initialValue
	 * @return 
	 */
    public ImgInterface create(int width, int height, int channels, int initialValue) {
		return create(width, height, channels, (byte)initialValue);
	}
	/**
	 * Create a new Img object from the provided BufferedImage.
	 * @param img
	 * @return 
	 */
    public ImgInterface create(BufferedImage img) {
		if(img == null)
			throw new NullPointerException("Could not read image, img may be null");

		int width = img.getWidth();
		int height = img.getHeight();
		int channels;
		if(img.getType()==BufferedImage.TYPE_BYTE_BINARY
			|| img.getType()==BufferedImage.TYPE_BYTE_GRAY
			|| img.getType()==BufferedImage.TYPE_USHORT_GRAY)
			channels = 1;
		else if(img.getType()==BufferedImage.TYPE_3BYTE_BGR
			|| img.getType()==BufferedImage.TYPE_INT_BGR
			|| img.getType()==BufferedImage.TYPE_INT_RGB
			|| img.getType()==BufferedImage.TYPE_USHORT_555_RGB
			|| img.getType()==BufferedImage.TYPE_USHORT_565_RGB)
			channels = 3;
		else if(img.getType()==BufferedImage.TYPE_4BYTE_ABGR
			|| img.getType()==BufferedImage.TYPE_4BYTE_ABGR_PRE
			|| img.getType()==BufferedImage.TYPE_INT_ARGB
			|| img.getType()==BufferedImage.TYPE_INT_ARGB_PRE)
			channels = 4;
		else channels = 4;
		ImgInterface out = create(width, height, channels);

		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int idx, Object val) {
				int y = idx;
				for(int x=0; x<width; x++) {
					Color col = new Color(img.getRGB(x, y));
					if(1 <= channels) out.setInt(x,y,0, col.getRed());
					if(2 <= channels) out.setInt(x,y,1, col.getGreen());
					if(3 <= channels) out.setInt(x,y,2, col.getBlue());
					if(4 <= channels) out.setInt(x,y,3, col.getAlpha());
				}
			}
		});
		return out;
	}
	/**
	 * Create a new ImgInterface object from the provided image file.
	 * @param f
	 * @return 
	 * @throws IOException if an error occurs during reading.
	 */
    public ImgInterface create(File f) throws IOException {
		return create(ImageIO.read(f));
	}
	/**
	 * Create a copy of this Img object.
	 * @return 
	 */
    public abstract ImgInterface copy();

    public int getWidth() {
		return width;
    }
    public int getHeight() {
		return height;
    }
    public int getChannels() {
		return channels;
    }
    public int[] getShape() {
		return new int[] {width,height,channels};
    }
	public int getSubPixelCount() {
		if(subPixelCount == -1)
			subPixelCount = width*height*channels;
		return subPixelCount;
    }
    
	/**
	 * Gets the sub pixel value at the index in the internal flat array. Faster
	 * than using the 3D function.
	 * @param i
	 * @return 
	 */
    public abstract byte getVal(int i);
	/**
	 * Sets the sub pixel value at the index in the internal flat array. Faster
	 * than using the 3D function.
	 * @param i
	 * @param val 
	 */
    public abstract void setVal(int i, byte val);
	/**
	 * Gets the sub pixel value at the given image coordinate.
	 * @param x
	 * @param y
	 * @param c
	 * @return 
	 */
    public abstract byte getVal(int x, int y, int c);
	/**
	 * Gets the sub pixel value at the given image coordinate. Checks for out of
	 * bounds requests and handles them.
	 * @param x
	 * @param y
	 * @param c
	 * @param borderHandling
	 * @return 
	 */
    public abstract Byte getVal(int x, int y, int c, int borderHandling);
	/**
	 * Sets the sub pixel value at the given image coordinate.
	 * @param x
	 * @param y
	 * @param c
	 * @param val 
	 */
    public abstract void setVal(int x, int y, int c, byte val);
	/**
	 * Gets the sub pixel value at the index in the internal flat array. Faster
	 * than using the 3D function.
	 * @param i
	 * @return 
	 */
    public abstract int getInt(int i);
	/**
	 * Sets the sub pixel value at the index in the internal flat array. Faster
	 * than using the 3D function.
	 * @param i
	 * @param val 
	 */
    public abstract void setInt(int i, int val);
	/**
	 * Gets the sub pixel value at the given image coordinate.
	 * @param x
	 * @param y
	 * @param c
	 * @return 
	 */
    public abstract int getInt(int x, int y, int c);
	/**
	 * Sets the sub pixel value at the given image coordinate.
	 * @param x
	 * @param y
	 * @param c
	 * @param val 
	 */
    public abstract void setInt(int x, int y, int c, int val);
	/**
	 * Gets the sub pixel value at the given image coordinate. Checks for out of
	 * bounds requests and handles them.
	 * @param x
	 * @param y
	 * @param c
	 * @param borderHandling
	 * @return 
	 */
    public abstract Integer getInt(int x, int y, int c, int borderHandling);
	/**
	 * Gets a Color object for the given pixel coordinate.
	 * @param x
	 * @param y
	 * @return 
	 */
	public abstract Color getColor(int x, int y);
	/**
	 * Gets the sub pixel value at the given image coordinate. Interpolates or
	 * averages if needed.
	 * @param x the x coord
	 * @param xW the number of pixels to average. Interpolates if 0.
	 * @param y the y coord
	 * @param yW the number of pixels to average. Interpolates if 0.
	 * @param c the channel
	 * @return 
	 */
    public double getInterp(double x, int xW, double y, int yW, int c) {
		if((xW | yW) == 0) { // both need to interp
			int x1 = (int) Math.floor(x), x2 = (int) Math.ceil(x);
			int y1 = (int) Math.floor(y), y2 = (int) Math.ceil(y);
			double xR = x-(long)x;
			double v1 = linInterp(getInt(x1,y1,c), getInt(x2,y1,c), xR);
			double v2 = linInterp(getInt(x1,y2,c), getInt(x2,y2,c), xR);
			return linInterp(v1, v2, y-(long)y);
		} else if(xW!=0 & yW!=0) { // both need to average
			int halfXW = xW >>> 1; //xW / 2
			int halfYW = yW >>> 1; //yW / 2
			int xLeftPos = ( ((xW&1)==0) ? -halfXW + 1 : -halfXW) + (int) x;
			int xRightPos = halfXW + (int) x;
			int yLeftPos = ( ((yW&1)==0) ? -halfYW + 1 : -halfYW) + (int) y;
			int yRightPos = halfYW + (int) y;
			int sum = 0;
			int count = 0;
			for(int dx=xLeftPos; dx<=xRightPos; dx++) {
			for(int dy=yLeftPos; dy<=yRightPos; dy++) {
				Integer imVal = getInt(dx, dy, c, BORDER_IGNORE);
				if(imVal != null) {
				sum += imVal;
				count++;
				}
			}
			}
			return sum/count;
		} else if(xW!=0 & yW==0) { // x needs to average, y need to interp
			int y1 = (int) Math.floor(y), y2 = (int) Math.ceil(y);
			double yR = y-(long)y;

			int halfXW = xW >>> 1; //xW / 2
			int xLeftPos = ( ((xW&1)==0) ? -halfXW + 1 : -halfXW) + (int) x;
			int xRightPos = halfXW + (int) x;

			double sum = 0;
			int count = 0;
			for(int dx=xLeftPos; dx<=xRightPos; dx++) {
			if(0 < dx && dx < width) {
				double v1 = linInterp(getInt(dx,y1,c), getInt(dx,y2,c), yR);
				sum += v1;
				count++;
			}
			}
			return sum / count;
		} else if(xW==0 & yW!=0) {
			int x1 = (int) Math.floor(x), x2 = (int) Math.ceil(x);
			double xR = x-(long)x;

			int halfYW = yW >>> 1; //yW / 2
			int yLeftPos = ( ((yW&1)==0) ? -halfYW + 1 : -halfYW) + (int) y;
			int yRightPos = halfYW + (int) y;

			double sum = 0;
			int count = 0;
			for(int dy=yLeftPos; dy<=yRightPos; dy++) {
			if(0 < dy && dy < height) {
				double v1 = linInterp(getInt(x1,dy,c), getInt(x2,dy,c), xR);
				sum += v1;
				count++;
			}
			}
			return sum / count;
		}
		return Double.NaN;
    }
	/**
	 * Same as getInterp() but ensures that the requested pixel is within the region of the image.
	 * @param x the x coord
	 * @param xW the number of pixels to average. Interpolates if 0.
	 * @param y the y coord
	 * @param yW the number of pixels to average. Interpolates if 0.
	 * @param c the channel
	 * @return 
	 */
    public Double getInterpCheck(double x, int xW, double y, int yW, int c) {
		if(x < 0 || x >= width-1) return null;
		if(y < 0 || y >= height-1) return null;
		if(c < 0 || c >= channels) return null;
		return getInterp(x, xW, y, yW, c);
    }
    private double linInterp(double a, double b, double ratio) {
		/*if(ratio != 0)
			System.out.println("");*/
		return a + (b-a)*ratio;
    }
    
    /**
     * Get a sub section of the image
     * @param x1 left coordinate
     * @param x2 right coordinate
     * @param y1 top coordinate
     * @param y2 bottom coordinate
     * @param c1 the starting channel
     * @param c2 the ending channel
     * @return 
     */
    public ImgInterface subImg(int x1, int x2, int y1, int y2, int c1, int c2) {
		if(x2==-1) x2 = width;
		if(y2==-1) y2 = height;
		if(c2==-1) c2 = channels;
		if(x1<0 || x1>width) throw new ArrayIndexOutOfBoundsException("x1 out of bounds: 0 < "+x1+" < "+width);
		if(x2<0 || x2>width) throw new ArrayIndexOutOfBoundsException("x2 out of bounds: 0 < "+x2+" < "+width);
		if(y1<0 || y1>height) throw new ArrayIndexOutOfBoundsException("y1 out of bounds: 0 < "+y1+" < "+height);
		if(y2<0 || y2>height) throw new ArrayIndexOutOfBoundsException("y2 out of bounds: 0 < "+y2+" < "+height);
		if(c1<0 || c1>channels) throw new ArrayIndexOutOfBoundsException("c1 out of bounds: 0 < "+c1+" < "+channels);
		if(c2<0 || c2>channels) throw new ArrayIndexOutOfBoundsException("c2 out of bounds: 0 < "+c2+" < "+channels);
		return subImgNoChecks(x1, x2, y1, y2, c1, c2);
    }
    private ImgInterface subImgNoChecks(int x1, int x2, int y1, int y2, int c1, int c2) {
		ImgInterface out = create(x2-x1, y2-y1, c2-c1);
		MPT.run(threadCount, y1, y2, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=x1; x<x2; x++) {
					for(int c=c1; c<c2; c++) {
						out.setVal(x-x1, y-y1, c-c1, getVal(x, y, c));
					}
				}
			}
		});
		return out;
    }
	
	/**
	 * Crops an area of the image.
	 * @param x the position of the left side of the image.
	 * @param y the position of the upper side of the image.
	 * @param w the width of the cropped image.
	 * @param h the height of the cropped image.
	 * @return the cropped image.
	 */
	public ImgInterface crop(int x, int y, int w, int h) {
		return subImg(x, x+w, y, y+h, 0, channels);
	}
	
	/**
	 * Crops an area of the image.
	 * @param x the x coord of the center of the image, as a ratio of how far
	 * to the left or right the cropped area can be.
	 * @param y the y coord of the center of the image, as a ratio of how far
	 * to the up or down the cropped area can be.
	 * @param w the width of the cropped image.
	 * @param h the height of the cropped image.
	 * @return the cropped image.
	 */
	public ImgInterface crop(double x, double y, int w, int h) {
		//if(w > width) throw new ArrayIndexOutOfBoundsException("x1 out of bounds: 0 < "+x1+" < "+width);
		int xx = (int) (x*(width-w));
		int yy = (int) (y*(height-h));
		return crop(xx, yy, w, h);
	}
	
	/**
	 * Crops an area of the image.
	 * @param x the x coord of the center of the image, as a ratio of how far
	 * to the left or right the cropped area can be.
	 * @param y the y coord of the center of the image, as a ratio of how far
	 * to the up or down the cropped area can be.
	 * @param w the width of the cropped image, as a ratio of how wide the
	 * cropped area can be.
	 * @param h the height of the cropped image, as a ratio of how high the
	 * cropped area can be.
	 * @return the cropped image.
	 */
	public ImgInterface crop(double x, double y, double w, double h) {
		int ww = (int) (w*width);
		int hh = (int) (w*height);
		return crop(x, y, ww, hh);
	}
    
    /**
     * Overlay an image onto this image
     * @param x the x coord
     * @param y the y coord
     * @param im the image to insert
     */
    public void insert(int x, int y, ImgInterface im) {
		if(im.width+x > width || im.height+y > height)
			throw new RuntimeException("inserted image too large to fit");
		MPT.run(threadCount, 0, im.height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int j, Object val) {
				for(int i=0; i<im.width; i++) {
					for(int c=0; c<im.channels; c++) {
						setVal(i+x, j+y, c, im.getVal(i,j,c));
					}
				}
			}
		});
    }
    
    /**
     * Resize this image to the given width/height
     * @param w the new width
     * @param h the new height
     * @return 
     */
    public ImgInterface resize(int w, int h) {
		int ww = Math.abs(w);
		int hh = Math.abs(h);
		boolean flipX = w<0;
		boolean flipY = h<0;
		
		ImgInterface out = create(ww, hh, channels);
		double xMult = width/(double)ww;
		double yMult = height/(double)hh;
		int xScale = width/ww;
		int yScale = height/hh;
		MPT.run(threadCount, 0, hh, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				double newY = Math.min(y*yMult, height-1);
				for(int x=0; x<ww; x++) {
					double newX = Math.min(x*xMult, width-1);
					for(int c=0; c<channels; c++) {
						out.setVal(flipX?ww-x-1:x, flipY?hh-y-1:y, c, (byte) getInterp(newX, xScale, newY, yScale, c));
					}
				}
			}
		});
		return out;
    }
    
    /**
     * Adjust the size of the image by a ratio
     * @param w the ratio to adjust the width by
     * @param h the ratio to adjust the width by
     * @return 
     */
    public ImgInterface rescale(double w, double h) {
		return resize((int)(getWidth()*w), (int)(getHeight()*h));
    }
	
    /**
     * Efficiently upscales using nearest neighbor.
     * @param times 
     * @return 
     */
    public ImgInterface upscale(int times) {
		ImgInterface out = create(width*times, height*times, channels);
		MPT.run(threadCount, 0, out.height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<out.width; x++) {
					for(int c=0; c<channels; c++) {
						out.setVal(x, y, c, getVal(x/times, y/times, c));
					}
				}
			}
		});
		return out;
    }
	
    /**
     * Efficiently reduces the image scale by half
     * @return 
     */
    public ImgInterface downScale2x() {
		ImgInterface out = create(width/2, height/2, channels);
		MPT.run(threadCount, 0, out.height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<out.width; x++) {
					for(int c=0; c<channels; c++) {
						out.setInt(x, y, c, (
								getInt(x<<1, y<<1, c)+getInt((x<<1)+1, y<<1, c)+
								getInt(x<<1, (y<<1)+1, c)+getInt((x<<1)+1, (y<<1)+1, c)
							)>>>2);
						/*out.setInt(x, y, c, (
								getInt(x*2, y*2, c)+getInt(x*2+1, y*2, c)+
								getInt(x*2, y*2+1, c)+getInt(x*2+1, y*2+1, c)
							)/4);*/
					}
				}
			}
		});
		return out;
    }
	
    /**
     * Efficiently upscales by 2 using nearest neighbor.
     * @return 
     */
	public ImgInterface upscale2x() {
		ImgInterface out = create(width*2, height*2, channels);
		MPT.run(threadCount, 0, out.height, 1, new MTPListRunnable<Integer>() {
			@Override
			public void iter(int procID, int y, Integer val) {
				for(int x=0; x<out.width; x++) {
					for(int c=0; c<channels; c++) {
						out.setVal(x, y, c, getVal(x>>>1, y>>>1, c));
					}
				}
			}
		});
		return out;
    }
    
	/**
	 * Convert the image into grey scale. Returns a copy of this Img if it is
	 * already grey scale.
	 * @return 
	 */
    public ImgInterface toGrey() {
		if(channels == 1)
			return copy();
		ImgInterface out = create(width, height, 1);
		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					int sum = 0;
					for(int c=0; c<channels; c++)
						sum += getInt(x, y, c);
					out.setInt(x, y, 0, sum/channels);
				}
			}
		});
		return out;
    }
	
	/**
	 * Rotate the image by a specified amount. Pads the corners to avoid
	 * cropping the Image.
	 * @param rad the angle in radians to rotate by
	 * @return the rotated Img
	 */
    public ImgInterface rotate(double rad) {
		double theta = normaliseRadians(rad);
		double[] p1 = rotateTransform(0, 0, rad);
		double[] p2 = rotateTransform(width, 0, rad);
		double[] p3 = rotateTransform(0, height, rad);
		double[] p4 = rotateTransform(width, height, rad);
		double minX = Math.min(Math.min(p1[0], p2[0]), Math.min(p4[0], p3[0]));
		double maxX = Math.max(Math.max(p1[0], p2[0]), Math.max(p4[0], p3[0]));
		double minY = Math.min(Math.min(p1[1], p2[1]), Math.min(p4[1], p3[1]));
		double maxY = Math.max(Math.max(p1[1], p2[1]), Math.max(p4[1], p3[1]));
		
		ImgInterface out = create((int)(maxX-minX)-1, (int)(maxY-minY)-1, channels);
		// re-adjust position to move into box
		int xShift, yShift;
		if(theta < Math.PI/2) {
			xShift = (int) (p2[0]-(maxX-minX));
			yShift = 0;
		} else if(theta < Math.PI) {
			xShift = (int) -(maxX-minX);
			yShift = (int) (p3[1]-p1[1]);
		} else if(theta < Math.PI*3/2) {
			xShift = (int) (p2[0]-p1[0]);
			yShift = (int) -(maxY-minY);
		} else {
			xShift = 0;
			yShift = (int) -(p2[1]-p1[1]);;
		}
		MPT.run(threadCount, 0, out.getHeight(), 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<out.getWidth(); x++) {
					double[] oldCoords = rotateTransform(x+xShift, y+yShift, theta);
					double oldX = oldCoords[0];
					double oldY = oldCoords[1];
					for(int c=0; c<channels; c++) {
						Double temp = getInterpCheck(oldX, 0, oldY, 0, c);
						byte newValue;
						if(temp == null) {
							if(c == 3) newValue = -1; //255
							else newValue = 0;
						} else {
							newValue = (byte)(double)temp;
						}
						out.setVal(x, y, c, newValue);
					}
				}
			}
		});
		return out;
    }
	//<editor-fold defaultstate="collapsed" desc="public Img rotateCrop(double rad) {...}">
	/**
	 * Rotate the image by a specified amount. Crops the corners to avoid
	 * adding whitespace.
	 * @param rad the angle in radians to rotate by
	 * @return the rotated Img
	 */
	/*public Img rotateCrop(double rad) {
	Img out = rotate(rad);
	int atMinWH = (int) Math.sqrt(Math.pow(Math.min(width, height), 2)/2);
	int atMaxW = out.getWidth();
	int atMaxH = out.getHeight();
	
	//     | 0 & 90 is min 45 is max
	// -45 | -45 & 45 is min 0 is max
	// abs | 45 & 45 is min 0 is max
	// /45 | -1 & 1 is min 0 is max
	
	// abs | 45 & 45 is min 0 is max
	// 45- | 0 & 0 is min 45 is max
	
	double ratio = Math.abs((rad%(Math.PI/2)) - (Math.PI/4))/(Math.PI/4); // 0-1
	ratio = Math.pow((ratio+1)/2, 15);
	
	//double reducedAngle = Math.abs((rad%(Math.PI/2)) - (Math.PI/4)); // 45 & 45 is min 0 is max
	//double ratio = Math.tan(Math.PI/4-reducedAngle);
	
	int newW = (int) ((atMaxW-atMinWH) * ratio + atMinWH);
	int newH = (int) ((atMaxH-atMinWH) * ratio + atMinWH);
	return out.crop(0.5, 0.5, newW, newH);
	}
	public Img rotateCrop(double rad) {
	double theta = normaliseRadians(rad);
	double[] p1 = rotateTransform(0, 0, rad);
	double[] p2 = rotateTransform(width, 0, rad);
	double[] p3 = rotateTransform(0, height, rad);
	double[] p4 = rotateTransform(width, height, rad);
	double minX = Math.min(Math.min(p1[0], p2[0]), Math.min(p4[0], p3[0]));
	double maxX = Math.max(Math.max(p1[0], p2[0]), Math.max(p4[0], p3[0]));
	double minY = Math.min(Math.min(p1[1], p2[1]), Math.min(p4[1], p3[1]));
	double maxY = Math.max(Math.max(p1[1], p2[1]), Math.max(p4[1], p3[1]));
	
	int newH = (int)(maxY-minY)-1;
	int newW = (int)(maxX-minX)-1;
	int atMinWH = (int) Math.sqrt(Math.pow(Math.min(width, height),2)*2);
	if(0 < rad && rad <= Math.PI/4) {
	double mid = (maxX-minX)/2;
	double m = p1[0]-(mid-atMinWH/2);
	double x = m/Math.tan(rad);
	newH = (int) ((maxY-minY) - 2*x);
	System.out.println(newH);
	}
	
	Img out = new Img(newW, newH, channels);
	// re-adjust position to move into box
	int xShift, yShift;
	if(theta < Math.PI/2) {
	xShift = (int) (p2[0]-(maxX-minX));
	yShift = 0;
	} else if(theta < Math.PI) {
	xShift = (int) -(maxX-minX);
	yShift = (int) (p3[1]-p1[1]);
	} else if(theta < Math.PI*3/2) {
	xShift = (int) (p2[0]-p1[0]);
	yShift = (int) -(maxY-minY);
	} else {
	xShift = 0;
	yShift = (int) -(p2[1]-p1[1]);;
	}
	MPT.run(threadCount, 0, out.getHeight(), 1, new MTPListRunnable() {
	@Override
	public void iter(int procID, int y, Object val) {
	for(int x=0; x<out.getWidth(); x++) {
	double[] oldCoords = rotateTransform(x+xShift, y+yShift, theta);
	double oldX = oldCoords[0];
	double oldY = oldCoords[1];
	for(int c=0; c<channels; c++) {
	Double temp = getInterpCheck(oldX, 0, oldY, 0, c);
	out.setVal(x, y, c, (byte)((temp==null)?125:temp));
	}
	}
	}
	});
	return out;
	}
	*/
//</editor-fold>
	private static double[] rotateTransform(double x, double y, double rad) {
		return new double[] {
			x*Math.cos(rad) + y*Math.sin(rad),
			-x*Math.sin(rad) + y*Math.cos(rad)
		};
	}
	private static double normaliseRadians(double rad) {
		rad = rad % (2*Math.PI);
		return (rad<0) ? Math.PI*2+rad : rad;
	}
	
	/**
	 * Add padding to the right and bottom of the image. The resulting image is
	 * square and has a width/height that is big enough to fit the original
	 * image but also a power of 2.
	 * @return 
	 */
    public ImgInterface squareifyPow2() {
		ImgInterface out = create(nextPow2(width), nextPow2(height), channels);
		out.insert(0, 0, this);
		return out;
    }
	
	/**
	 * Converts the Img into a laplacian pyramid.
	 * @param levelCount
	 * @return 
	 */
    public ArrayList<ImgInterface> laplacianPyramid(int levelCount) {
		ArrayList<ImgInterface> out = new ArrayList<>();
		ImgInterface color = this;
		for(int i=0; i<levelCount; i++) {
			ImgInterface redu = color.downScale2x();
			ImgInterface upSc = redu.upscale2x();
			out.add(color.sub(upSc));
			color = redu;
		}
		out.add(color);
		return out;
    }
	/**
	 * Converts the laplacian pyramid back into an Img.
	 * @param pyramid
	 * @return 
	 */
    public static ImgInterface laplacianPyramid(ArrayList<ImgInterface> pyramid) {
		ImgInterface out = pyramid.get(pyramid.size()-1);
		for(int i=pyramid.size()-2; i>=0; i--) {
			out = out.upscale2x().add(pyramid.get(i));
		}
		return out;
    }
	
	/**
	 * Calculates the next power of 2 greater than the given number.
	 * @param i
	 * @return 
	 */
    public static int nextPow2(int i) {
		if(i < 0) return -1;
		int out = -2147483648;
		while((i&out)==0)
			out = out >>> 1;
		return (((out-1)&i) != 0) ? out<<1 : out;
    }
	
	private static final Integer ZERO = 0;
	/**
	 * Erode image using the 4 adjacent cells.
	 * @return 
	 */
    public ImgInterface erode4() {
		ImgInterface out = create(width, height, channels);
		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						if(ZERO.equals(getInt(x, y-1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x-1, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x+1, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x, y+1, c, BORDER_IGNORE))) {
							out.setInt(x, y, c, 0);
						} else {
							out.setInt(x, y, c, 1);
						}
					}
				}
			}
		});
		return out;
    }
	/**
	 * Dilate image using the 4 adjacent cells.
	 * @return 
	 */
    public ImgInterface dilate4() {
		ImgInterface out = create(width, height, channels);
		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						if(compGT(getInt(x, y-1, c, BORDER_IGNORE))
								|| compGT(getInt(x-1, y, c, BORDER_IGNORE))
								|| compGT(getInt(x, y, c, BORDER_IGNORE))
								|| compGT(getInt(x+1, y, c, BORDER_IGNORE))
								|| compGT(getInt(x, y+1, c, BORDER_IGNORE))) {
							out.setVal(x, y, c, (byte)1);
						}
					}
				}
			}
		});
		return out;
    }
	/**
	 * Erode image using the 4 adjacent cells and 4 diagonal cells.
	 * @return 
	 */
    public ImgInterface erode8() {
		ImgInterface out = create(width, height, channels);
		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						if(ZERO.equals(getInt(x-1, y-1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x, y-1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x+1, y-1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x-1, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x+1, y, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x-1, y+1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x, y+1, c, BORDER_IGNORE))
								|| ZERO.equals(getInt(x+1, y+1, c, BORDER_IGNORE))) {
							out.setInt(x, y, c, 0);
						} else {
							out.setInt(x, y, c, 1);
						}
					}
				}
			}
		});
		return out;
    }
	/**
	 * Dilate image using the 4 adjacent cells and 4 diagonal cells.
	 * @return 
	 */
    public ImgInterface dilate8() {
		ImgInterface out = create(width, height, channels);
		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						if(compGT(getInt(x-1, y-1, c, BORDER_IGNORE))
								|| compGT(getInt(x, y-1, c, BORDER_IGNORE))
								|| compGT(getInt(x+1, y-1, c, BORDER_IGNORE))
								|| compGT(getInt(x-1, y, c, BORDER_IGNORE))
								|| compGT(getInt(x, y, c, BORDER_IGNORE))
								|| compGT(getInt(x+1, y, c, BORDER_IGNORE))
								|| compGT(getInt(x-1, y+1, c, BORDER_IGNORE))
								|| compGT(getInt(x, y+1, c, BORDER_IGNORE))
								|| compGT(getInt(x+1, y+1, c, BORDER_IGNORE))) {
							out.setVal(x, y, c, (byte)1);
						}
					}
				}
			}
		});
		return out;
    }
	private boolean compGT(Integer i) {
		return i!=null && i>0;
	}
	
	/**
	 * Set the values of each pixel in the blobs to the given value.
	 * @param blobs the blobs to set.
	 * @param channel
	 * @param value 
	 */
	public void setValue(ArrayList<Blob> blobs, int channel, int value) {
		for(Blob blob : blobs) {
			setBlobValue(blob, channel, value);
		}
	}
	/**
	 * Set the values of each pixel in the blob to the given value.
	 * @param b the blobs to set.
	 * @param channel
	 * @param value 
	 */
	public void setBlobValue(Blob b, int channel, int value) {
		for(int i=0; i<b.getSize(); i++) {
			int[] p = b.getPoint(i);
			setInt(p[0], p[1], 0, 0);
		}
	}
	/**
	 * Scan the image for blobs (regions of non zero values).
	 * @param includeDiagonal if the search should scan diagonally.
	 * @return 
	 */
	public ArrayList<Blob> detectBlobs(boolean includeDiagonal) {
		ArrayList<Blob> blobs = new ArrayList<>();
		for(int c=0; c<channels; c++) {
			Img mask = Img.createNew(width, height, 1);
			for(int y=0; y<height; y++) {
				for(int x=0; x<width; x++) {
					if(mask.getInt(x,y,0) > 0) continue;
					if(getInt(x, y, c) == 0) {
						mask.setInt(x, y, 0, 1);
					} else {
						//start flood fill
						Blob blob = new Blob();
						Queue<int[]> q = new LinkedList<>();
						q.add(new int[] {x, y});
						mask.setInt(x,y,0, 1);
						while(!q.isEmpty()) {
							int[] currCoord = q.remove();
							int cx = currCoord[0];
							int cy = currCoord[1];
							blob.addPoint(cx, cy);
							int tx,ty;
							tx = cx+1; ty = cy;
							if(inBounds(tx,ty,0) && mask.getVal(tx,ty,0)==0 && getInt(tx,ty,c) > 0) {
								q.add(new int[] {tx,ty});
								mask.setInt(tx,ty,0,1);
							}
							tx = cx-1; ty = cy;
							if(inBounds(tx,ty,0) && mask.getVal(tx,ty,0)==0 && getInt(tx,ty,c) > 0) {
								q.add(new int[] {tx,ty});
								mask.setInt(tx,ty,0,1);
							}
							tx = cx; ty = cy+1;
							if(inBounds(tx,ty,0) && mask.getVal(tx,ty,0)==0 && getInt(tx,ty,c) > 0) {
								q.add(new int[] {tx,ty});
								mask.setInt(tx,ty,0,1);
							}
							tx = cx; ty = cy-1;
							if(inBounds(tx,ty,0) && mask.getVal(tx,ty,0)==0 && getInt(tx,ty,c) > 0) {
								q.add(new int[] {tx,ty});
								mask.setInt(tx,ty,0,1);
							}
						}
						blobs.add(blob);
					}
				}
			}
		}
		return blobs;
	}
	private boolean inBounds(int x, int y, int c) {
		return !(x<0 || y<0 || c<0 || x>=width || y>=height || c>=channels);
	}
    
    /**
     * Get the smallest and largest pixel value
     * @return 
     */
    public abstract int[] minMax();
	
	/**
	 * Calculate the histograms for each channel in the image.
	 * @return out[channels][0-255]
	 */
	public int[][] getHistogram() {
		int[][][] tempOut = new int[threadCount][channels][256];
		MPT.run(threadCount, 0, getHeight(), 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						tempOut[procID][c][getInt(x, y, c)]++;
					}
				}
			}
		});
		int[][] out = new int[channels][256];
		for(int i=0; i<channels; i++) {
			for(int j=0; j<256; j++) {
				int sum = 0;
				for(int k=0; k<threadCount; k++) {
					sum += tempOut[k][i][j];
				}
				out[i][j] = sum;
			}
		}
		return out;
	}
    
    /**
     * convert this image to a buffered image
     * @return 
     */
    public BufferedImage toBufferedImage() {
		int type = (channels == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage out = new BufferedImage(width, height, type);

		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int idx, Object val) {
				int y = idx;
				for(int x=0; x<width; x++) {
					Color col = getColor(x, y);
					out.setRGB(x, y, col.getRGB());
				}
			}
		});
		return out;
    }
    
    /**
     * Display this image in a JFrame
	 * @param title the title to display on the ImgViewer
	 * @return returns a reference to the ImgViewer created
     */
    public ImgViewer show(String title) {
		return ImgViewer.showAll(title, this);
    }
    
    /**
     * Display this image in a JFrame
	 * @return returns a reference to the ImgViewer created
     */
    public ImgViewer show() {
		return ImgViewer.showAll(this);
    }
    
	/**
	 * Run an element-wise operation on each pixel in the image
	 * @param op
	 * @return 
	 */
	public abstract ImgInterface runOp(Op op);
	public static abstract class Op {
		public abstract int run(int threadID, int pos, int prevVal);
	}
	
	public ImgInterface add(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal + value;
			}
		});
	}
	public ImgInterface add(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal + value.getInt(pos);
			}
		});
	}
	public ImgInterface sub(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal - value;
			}
		});
	}
	public ImgInterface subRev(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return value - prevVal;
			}
		});
	}
	public ImgInterface sub(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal - value.getInt(pos);
			}
		});
	}
	public ImgInterface mult(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal * value;
			}
		});
	}
	public ImgInterface mult(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal * value.getInt(pos);
			}
		});
	}
	public ImgInterface div(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal / value;
			}
		});
	}
	public ImgInterface divRev(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return value / prevVal;
			}
		});
	}
	public ImgInterface div(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal / value.getInt(pos);
			}
		});
	}
	public ImgInterface abs() {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.abs(prevVal);
			}
		});
	}
	public ImgInterface max(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.max(prevVal, value);
			}
		});
	}
	public ImgInterface max(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.max(prevVal, value.getInt(pos));
			}
		});
	}
	public ImgInterface min(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.min(prevVal, value);
			}
		});
	}
	public ImgInterface min(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.min(prevVal, value.getInt(pos));
			}
		});
	}
	public ImgInterface equalTo(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal == value)?1:0;
			}
		});
	}
	public ImgInterface equalTo(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal == value.getInt(pos))?1:0;
			}
		});
	}
	public ImgInterface greaterThan(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal > value)?1:0;
			}
		});
	}
	public ImgInterface greaterThan(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal > value.getInt(pos))?1:0;
			}
		});
	}
	public ImgInterface greaterThanEq(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal >= value)?1:0;
			}
		});
	}
	public ImgInterface greaterThanEq(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal >= value.getInt(pos))?1:0;
			}
		});
	}
	public ImgInterface lessThan(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal < value)?1:0;
			}
		});
	}
	public ImgInterface lessThan(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal < value.getInt(pos))?1:0;
			}
		});
	}
	public ImgInterface lessThanEq(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal <= value)?1:0;
			}
		});
	}
	public ImgInterface lessThanEq(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal <= value.getInt(pos))?1:0;
			}
		});
	}
	public ImgInterface mod(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal % value;
			}
		});
	}
	public ImgInterface mod(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal % value.getInt(pos);
			}
		});
	}
    
	/**
	 * Check that this Img and the given Img have the same shape.
	 * @param img 
	 */
    protected void assertShape(ImgInterface img) {
		if(width != img.width || height != img.height || channels != img.channels)
			throw new IndexOutOfBoundsException("Image sizes do not match");
    }
	
    @Override
    public int hashCode() {
		int hash = 7;
		hash = 89 * hash + this.width;
		hash = 89 * hash + this.height;
		hash = 89 * hash + this.channels;
		return hash;
    }

    @Override
    public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ImgInterface other = (ImgInterface) obj;
		if (this.width != other.width) {
			return false;
		}
		if (this.height != other.height) {
			return false;
		}
		if (this.channels != other.channels) {
			return false;
		}
		return true;
    }
    
}
