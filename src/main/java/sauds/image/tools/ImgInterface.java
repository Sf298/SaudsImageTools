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
import multiprocessing.tools.MPT;
import multiprocessing.tools.MTPListRunnable;

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
    
    public abstract void setVal(int i, byte val);
    public abstract void setVal(int x, int y, int c, byte val);
    public abstract byte getVal(int x, int y, int c);
    public abstract Byte getVal(int x, int y, int c, int borderHandling);
    public abstract int getInt(int i);
    public abstract int getInt(int x, int y, int c);
    //public abstract int[] getInts(int[] x, int[] y, int[] c);
    public abstract Integer getInt(int x, int y, int c, int borderHandling);
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
    public void insert(int x, int y, Img im) {
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
    
    
    public Img add(int value) {
	return runOps(new Add(value));
    }
    public Img add(ImgInterface value) {
	return runOps(new AddIm(value));
    }
    public Img sub(int value) {
	return runOps(new Sub(value));
    }
    public Img subRev(int value) {
	return runOps(new SubRev(value));
    }
    public Img sub(ImgInterface value) {
	return runOps(new SubIm(value));
    }
    public Img mult(int value) {
	return runOps(new Mult(value));
    }
    public Img mult(ImgInterface value) {
	return runOps(new MultIm(value));
    }
    public Img div(int value) {
	return runOps(new Div(value));
    }
    public Img divRev(int value) {
	return runOps(new DivRev(value));
    }
    public Img div(ImgInterface value) {
	return runOps(new DivIm(value));
    }
    public Img abs() {
	return runOps(new Abs());
    }
    public Img max(int value) {
	return runOps(new Max(value));
    }
    public Img max(ImgInterface value) {
	return runOps(new MaxIm(value));
    }
    public Img min(int value) {
	return runOps(new Min(value));
    }
    public Img min(ImgInterface value) {
	return runOps(new MinIm(value));
    }
    public Img equalTo(int value) {
	return runOps(new EqualTo(value));
    }
    public Img equalTo(ImgInterface value) {
	return runOps(new EqualToIm(value));
    }
    public Img greaterThan(int value) {
	return runOps(new GreaterThan(value));
    }
    public Img greaterThan(ImgInterface value) {
	return runOps(new GreaterThanIm(value));
    }
    public Img greaterThanEq(int value) {
	return runOps(new GreaterThanEq(value));
    }
    public Img greaterThanEq(ImgInterface value) {
	return runOps(new GreaterThanEqIm(value));
    }
    public Img lessThan(int value) {
	return runOps(new LessThan(value));
    }
    public Img lessThan(ImgInterface value) {
	return runOps(new LessThanIm(value));
    }
    public Img lessThanEq(int value) {
	return runOps(new LessThanEq(value));
    }
    public Img lessThanEq(ImgInterface value) {
	return runOps(new LessThanEqIm(value));
    }
    public Img mod(int value) {
	return runOps(new Mod(value));
    }
    
    protected ArrayList<Op> ops = new ArrayList<>();
    public Img runOps() {
	Img out = runOps(ops);
	ops.clear();
	return out;
    }
    public Img runOps(List<Op> ops) {
	Op[] arr = ops.toArray(new Op[ops.size()]);
	return runOps(arr);
    }
    public abstract Img runOps(Op... ops);
    public void opAdd(int value) { ops.add(new Add(value)); }
    public void opAdd(ImgInterface value) { ops.add(new AddIm(value)); }
    public void opSub(int value) { ops.add(new Sub(value)); }
    public void opSubRev(int value) { ops.add(new SubRev(value)); }
    public void opSub(ImgInterface value) { ops.add(new SubIm(value)); }
    public void opMult(int value) { ops.add(new Mult(value)); }
    public void opMult(ImgInterface value) { ops.add(new MultIm(value)); }
    public void opDiv(int value) { ops.add(new Div(value)); }
    public void opDivRev(int value) { ops.add(new DivRev(value)); }
    public void opDiv(ImgInterface value) { ops.add(new DivIm(value)); }
    public void opAbs() { ops.add(new Abs()); }
    public void opMax(int value) { ops.add(new Max(value)); }
    public void opMax(ImgInterface value) { ops.add(new MaxIm(value)); }
    public void opMin(int value) { ops.add(new Min(value)); }
    public void opMin(ImgInterface value) { ops.add(new MinIm(value)); }
    public void opEqualTo(int value) { ops.add(new EqualTo(value)); }
    public void opEqualTo(ImgInterface value) { ops.add(new EqualToIm(value)); }
    public void opGreaterThan(int value) { ops.add(new GreaterThan(value)); }
    public void opGreaterThan(ImgInterface value) { ops.add(new GreaterThanIm(value)); }
    public void opGreaterThanEq(int value) { ops.add(new GreaterThanEq(value)); }
    public void opGreaterThanEq(ImgInterface value) { ops.add(new GreaterThanEqIm(value)); }
    public void opLessThan(int value) { ops.add(new LessThan(value)); }
    public void opLessThan(ImgInterface value) { ops.add(new LessThanIm(value)); }
    public void opLessThanEq(int value) { ops.add(new LessThanEq(value)); }
    public void opLessThanEq(ImgInterface value) { ops.add(new LessThanEqIm(value)); }
    public void opMod(int value) { ops.add(new Mod(value)); }
    
    protected interface Op {
	public int run(int pos, int prevVal);
    }
    protected class Add implements Op {
	private final int value;
	public Add(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal + value;
	}
    }
    protected class AddIm implements Op {
	private ImgInterface value;
	public AddIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal + value.getInt(pos);
	}
    }
    protected class Sub implements Op {
	private final int value;
	public Sub(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal - value;
	}
    }
    protected class SubRev implements Op {
	private final int value;
	public SubRev(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return value - prevVal;
	}
    }
    protected class SubIm implements Op {
	private ImgInterface value;
	public SubIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal - value.getInt(pos);
	}
    }
    protected class Mult implements Op {
	private final int value;
	public Mult(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal * value;
	}
    }
    protected class MultIm implements Op {
	private ImgInterface value;
	public MultIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal * value.getInt(pos);
	}
    }
    protected class Div implements Op {
	private final int value;
	public Div(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal / value;
	}
    }
    protected class DivRev implements Op {
	private final int value;
	public DivRev(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return value / prevVal;
	}
    }
    protected class DivIm implements Op {
	private ImgInterface value;
	public DivIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal / value.getInt(pos);
	}
    }
    protected class Abs implements Op {
	@Override
	public int run(int pos, int prevVal) {
	    return Math.abs(prevVal);
	}
    }
    protected class Max implements Op {
	private final int value;
	public Max(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return Math.max(prevVal, value);
	}
    }
    protected class MaxIm implements Op {
	private ImgInterface value;
	public MaxIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return Math.max(prevVal, value.getInt(pos));
	}
    }
    protected class Min implements Op {
	private final int value;
	public Min(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return Math.min(prevVal, value);
	}
    }
    protected class MinIm implements Op {
	private ImgInterface value;
	public MinIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return Math.min(prevVal, value.getInt(pos));
	}
    }
    protected class EqualTo implements Op {
	private final int value;
	public EqualTo(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal == value) ? 1 : 0;
	}
    }
    protected class EqualToIm implements Op {
	private final ImgInterface value;
	public EqualToIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal == value.getInt(pos)) ? 1 : 0;
	}
    }
    protected class GreaterThan implements Op {
	private final int value;
	public GreaterThan(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal > value) ? 1 : 0;
	}
    }
    protected class GreaterThanIm implements Op {
	private final ImgInterface value;
	public GreaterThanIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal > value.getInt(pos)) ? 1 : 0;
	}
    }
    protected class GreaterThanEq implements Op {
	private final int value;
	public GreaterThanEq(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal >= value) ? 1 : 0;
	}
    }
    protected class GreaterThanEqIm implements Op {
	private final ImgInterface value;
	public GreaterThanEqIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal >= value.getInt(pos)) ? 1 : 0;
	}
    }
    protected class LessThan implements Op {
	private final int value;
	public LessThan(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal < value) ? 1 : 0;
	}
    }
    protected class LessThanIm implements Op {
	private final ImgInterface value;
	public LessThanIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal < value.getInt(pos)) ? 1 : 0;
	}
    }
    protected class LessThanEq implements Op {
	private final int value;
	public LessThanEq(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal <= value) ? 1 : 0;
	}
    }
    protected class LessThanEqIm implements Op {
	private final ImgInterface value;
	public LessThanEqIm(ImgInterface val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return (prevVal <= value.getInt(pos)) ? 1 : 0;
	}
    }
    protected class Mod implements Op {
	private final int value;
	public Mod(int val) {
	    value = val;
	}
	@Override
	public int run(int pos, int prevVal) {
	    return prevVal % value;
	}
    }
}
