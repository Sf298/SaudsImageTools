/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static sauds.image.tools.Img.nextPow2;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

/**
 *
 * @author saud
 */
public abstract class ImgInterface {
    
    public static final int BORDER_INNER = 0;
    public static final int BORDER_IGNORE = 1;
    public static final int BORDER_EXTEND = 2;
    
    public static int threadCount = MPT.coreCount();
    protected int width,height,channels;

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
    public Img subImg(int x1, int x2, int y1, int y2, int c1, int c2) {
		if(x2==-1) x2 = width;
		if(y2==-1) y2 = height;
		if(c2==-1) c2 = channels;
		return subImgNoChecks(x1, x2, y1, y2, c1, c2);
    }
    private Img subImgNoChecks(int x1, int x2, int y1, int y2, int c1, int c2) {
		Img out = new Img(x2-x1, y2-y1, c2-c1);
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
    public Img resize(int w, int h) {
		Img out = new Img(w, h, channels);
		double xMult = width/(double)w;
		double yMult = height/(double)h;
		int xScale = width/w;
		int yScale = height/h;
		MPT.run(threadCount, 0, h, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				double newY = Math.min(y*yMult, height-1);
				for(int x=0; x<w; x++) {
					double newX = Math.min(x*xMult, width-1);
					for(int c=0; c<channels; c++) {
						out.setVal(x, y, c, (byte) getInterp(newX, xScale, newY, yScale, c));
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
    public Img rescale(double w, double h) {
		return resize((int)(getWidth()*w), (int)(getHeight()*h));
    }
	
	/**
	 * Add padding to the right and bottom of the image. The resulting image is
	 * square and has a width/height that is big enough to fit the original
	 * image but also a power of 2.
	 * @return 
	 */
    public Img squareifyPow2() {
		Img out = new Img(nextPow2(width), nextPow2(height), channels);
		out.insert(0, 0, this);
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
    
    /**
     * Get the smallest and largest pixel value
     * @return 
     */
    public abstract int[] minMax();
    
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
					Color col = null;
					if(channels == 1) {
						int c = getInt(x,y,0);
						col = new Color(c, c, c);
					} else if(channels == 2)
						col = new Color(getInt(x,y,0), getInt(x,y,1), 0);
					else if(channels == 3)
						col = new Color(getInt(x,y,0), getInt(x,y,1), getInt(x,y,2));
					else if(channels == 4)
						col = new Color(getInt(x,y,0), getInt(x,y,1), getInt(x,y,2), getInt(x,y,3));

					out.setRGB(x, y, col.getRGB());
				}
			}
		});
		return out;
    }
    
    /**
     * Displays a collection of images in a JFrame
     * @param imgs 
     */
    public static void showImgs(Collection<ImgInterface> imgs) {
		JPanel panel = new JPanel();
		for(ImgInterface im : imgs) {
			panel.add(new JScrollPane(new JLabel(new ImageIcon(im.toBufferedImage()))));
		}

		JFrame frame = new JFrame("Images");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
    }
    
    /**
     * Displays an array of images in a JFrame
     * @param imgs 
     */
    public static void showImgs(ImgInterface... imgs) {
		showImgs(Arrays.asList(imgs));
    }
    
    /**
     * Display this image in a JFrame
     */
    public void show() {
		showImgs(this);
    }
    
	/**
	 * Run an element-wise operation on each pixel in the image
	 * @param op
	 * @return 
	 */
	public abstract Img runOp(Op op);
	public static abstract class Op {
		public abstract int run(int threadID, int pos, int prevVal);
	}
	
	public Img add(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal + value;
			}
		});
	}
	public Img add(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal + value.getInt(pos);
			}
		});
	}
	public Img sub(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal - value;
			}
		});
	}
	public Img subRev(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return value - prevVal;
			}
		});
	}
	public Img sub(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal - value.getInt(pos);
			}
		});
	}
	public Img mult(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal * value;
			}
		});
	}
	public Img mult(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal * value.getInt(pos);
			}
		});
	}
	public Img div(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal / value;
			}
		});
	}
	public Img divRev(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return value / prevVal;
			}
		});
	}
	public Img div(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal / value.getInt(pos);
			}
		});
	}
	public Img abs() {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.abs(prevVal);
			}
		});
	}
	public Img max(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.max(prevVal, value);
			}
		});
	}
	public Img max(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.max(prevVal, value.getInt(pos));
			}
		});
	}
	public Img min(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.min(prevVal, value);
			}
		});
	}
	public Img min(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return Math.min(prevVal, value.getInt(pos));
			}
		});
	}
	public Img equalTo(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal == value)?1:0;
			}
		});
	}
	public Img equalTo(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal == value.getInt(pos))?1:0;
			}
		});
	}
	public Img greaterThan(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal > value)?1:0;
			}
		});
	}
	public Img greaterThan(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal > value.getInt(pos))?1:0;
			}
		});
	}
	public Img greaterThanEq(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal >= value)?1:0;
			}
		});
	}
	public Img greaterThanEq(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal >= value.getInt(pos))?1:0;
			}
		});
	}
	public Img lessThan(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal < value)?1:0;
			}
		});
	}
	public Img lessThan(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal < value.getInt(pos))?1:0;
			}
		});
	}
	public Img lessThanEq(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal <= value)?1:0;
			}
		});
	}
	public Img lessThanEq(ImgInterface value) {
		assertShape(value);
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return (prevVal <= value.getInt(pos))?1:0;
			}
		});
	}
	public Img mod(int value) {
		return runOp(new Op() {
			@Override
			public int run(int threadID, int pos, int prevVal) {
				return prevVal % value;
			}
		});
	}
	public Img mod(ImgInterface value) {
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
