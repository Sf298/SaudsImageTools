
package sauds.image.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import multiprocessing.tools.MPT;
import multiprocessing.tools.MTPListRunnable;

/**
 *
 * @author saud
 */
public class Img extends ImgInterface {
    
    private final byte[] values;
    
    public Img(int width, int height, int channels) {
	this.width = width;
	this.height = height;
	this.channels = channels;
	//initIdxCaches();
	values = new byte[width*height*channels];
    }
    public Img(Img im) {
	width = im.width;
	height = im.height;
	channels = im.channels;
	//idxCache1 = Arrays.copyOf(im.idxCache1, im.idxCache1.length);
	//idxCache2 = Arrays.copyOf(im.idxCache2, im.idxCache2.length);
	values = Arrays.copyOf(im.values, im.values.length);
    }
    public Img(BufferedImage img) {
	width = img.getWidth();
	height = img.getHeight();
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
	//initIdxCaches();
	values = new byte[width*height*channels];
	/*int pos = 0;
	for(int y=0; y<height; y++) {
	    for(int x=0; x<width; x++) {
		Color col = new Color(img.getRGB(x, y));
		if(1 <= channels) values[pos] = (byte)col.getRed();
		if(2 <= channels) values[pos] = (byte)col.getGreen();
		if(3 <= channels) values[pos] = (byte)col.getBlue();
		if(4 <= channels) values[pos] = (byte)col.getAlpha();
		pos++;
	    }
	}*/
	
	MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
	    @Override
	    public void iter(int procID, int idx, Object val) {
		int y = idx;
		for(int x=0; x<width; x++) {
		    Color col = new Color(img.getRGB(x, y));
		    if(1 <= channels) setVal(x,y,0, (byte)col.getRed());
		    if(2 <= channels) setVal(x,y,1, (byte)col.getGreen());
		    if(3 <= channels) setVal(x,y,2, (byte)col.getBlue());
		    if(4 <= channels) setVal(x,y,3, (byte)col.getAlpha());
		}
	    }
	});
    }
    public Img(File f) throws IOException {
	this(ImageIO.read(f));
    }
    
    @Override
    public void setVal(int i, byte val) {
	values[i] = val;
    }
    @Override
    public void setVal(int x, int y, int c, byte val) {
	values[to1D(x,y,c)] = val;
    }
    @Override
    public byte getVal(int x, int y, int c) {
	//if(to1D(x,y,c) >= values.length)
	  //  System.out.println(x);
	return values[to1D(x,y,c)];
    }
    @Override
    public Byte getVal(int x, int y, int c, int borderHandling) {
	switch (borderHandling) {
	    case BORDER_EXTEND:
		if(x < 0) x = 0; if(x >= width) x = width - 1;
		if(y < 0) y = 0; if(y >= height) y = height - 1;
		if(c < 0) c = 0; if(c >= channels) c = channels - 1;
		return getVal(x,y,c);
	    case BORDER_IGNORE:
		if(x < 0 || x >= width) return null;
		if(y < 0 || y >= height) return null;
		if(c < 0 || c >= channels) return null;
		return getVal(x,y,c);
	    case BORDER_INNER:
		return getVal(x,y,c);
	}
	return null;
    }
    @Override
    public int getInt(int i) {
	return values[i] & 0xFF;
    }
    @Override
    public int getInt(int x, int y, int c) {
	return getVal(x, y, c) & 0xFF;
    }
    @Override
    public Integer getInt(int x, int y, int c, int borderHandling) {
	Byte val = getVal(x, y, c, borderHandling);
	if(val == null) return null;
	return val & 0xFF;
    }
    
    @Override
    public int[] minMax() {
	int[][] temp = new int[2][threadCount];
	MPT.run(threadCount, values, new MTPListRunnable<Byte>() {
	    @Override
	    public void iter(int procID, int idx, Byte val) {
		temp[0][procID] = Math.min(temp[0][procID], getInt(idx));
		temp[1][procID] = Math.max(temp[1][procID], getInt(idx));
	    }
	});
	int[] out = new int[2];
	for(int i=0; i<temp.length; i++) {
	    out[0] = Math.min(out[0], temp[0][i]);
	    out[1] = Math.max(out[1], temp[1][i]);
	}
	return out;
    }
    
    /**
     * Efficiently reduces the image scale by half
     * @return 
     */
    public Img downScale2x() {
	Img out = new Img(width/2, height/2, channels);
	MPT.run(threadCount, 0, out.height, 1, new MTPListRunnable() {
	    @Override
	    public void iter(int procID, int y, Object val) {
		for(int x=0; x<out.width; x++) {
		    for(int c=0; c<channels; c++) {
			out.setVal(x, y, c, (byte)((
				getInt(x*2, y*2, c)+getInt(x*2+1, y*2, c)+
				getInt(x*2, y*2+1, c)+getInt(x*2+1, y*2+1, c)
			    )/4));
		    }
		}
	    }
	});
	return out;
    }
    
    /**
     * Efficiently upscales using nearest neighbor.
     * @param times 
     * @return 
     */
    public Img upscale(int times) {
	Img out = new Img(width*times, height*times, channels);
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
    
    public Img toGrey() {
	if(channels == 1)
	    return new Img(this);
	Img out = new Img(width, height, 1);
	MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
	    @Override
	    public void iter(int procID, int y, Object val) {
		for(int x=0; x<width; x++) {
		    int sum = 0;
		    if(x == 1333 && y == 2550)
			System.out.println(sum+"/"+channels+"="+((byte)(sum/channels)));
		    for(int c=0; c<channels; c++)
			sum += getInt(x, y, c);
		    out.setVal(x, y, 0, (byte)(sum/channels));
		}
	    }
	});
	return out;
    }
    public Img squareifyPow2() {
	Img out = new Img(nextPow2(width), nextPow2(height), channels);
	out.insert(0, 0, this);
	return out;
    }
    public static int nextPow2(int i) {
	if(i < 0) return -1;
	int out = -2147483648;
	while((i&out)==0)
	    out = out >>> 1;
	return (((out-1)&i) != 0) ? out<<1 : out;
    }
    public ArrayList<Img> laplacianPyramid(int levelCount) {
	ArrayList<Img> out = new ArrayList<>();
	Img color = this;
	for(int i=0; i<levelCount; i++) {
	    Img redu = color.downScale2x();
	    Img upSc = redu.upscale(2);
	    out.add(color.sub(upSc));
	    color = redu;
	}
	out.add(color);
	return out;
    }
    public static Img laplacianPyramid(ArrayList<Img> pyramid) {
	Img out = pyramid.get(pyramid.size()-1);
	for(int i=pyramid.size()-2; i>=0; i--) {
	    out = out.upscale(2).add(pyramid.get(i));
	}
	return out;
    }
    
    public Img convolve(Kernel k, int channel, int borderHandling, int stride, int operation) {
	Img out;
	int wMod;
	int hMod;
	if(borderHandling == BORDER_INNER) {
	    out = new Img(width-k.getHW()*2, height-k.getHH()*2, channels);
	    wMod = k.getHW();
	    hMod = k.getHH();
	} else {
	    out = new Img(width, height, channels);
	    wMod = 0;
	    hMod = 0;
	}
	
	MPT.run(threadCount, hMod, height-hMod, stride, new MTPListRunnable() {
	    @Override
	    public void iter(int procID, int y, Object val) {
		for(int x=wMod; x<width-wMod; x+=stride) {
		    int sum = 0;
		    for(int dx=-k.getHW(); dx<k.getHW(); dx++) {
			for(int dy=-k.getHH(); dy<k.getHH(); dy++) {
			    int kVal = k.getC(dx, dy);
			    Byte imVal = getVal(x+dx, y+dy, channel, borderHandling);
			    
			    if(imVal==null || kVal==0) continue;
			    sum += kVal * imVal;
			}
		    }
		    out.setVal(x, y, channel, (byte)Math.max(Byte.MIN_VALUE, Math.min(sum, Byte.MAX_VALUE)));
		}
	    }
	});
	return out;
    }
    
    public void save(String formatName, File f) throws IOException {
	ImageIO.write(toBufferedImage(), formatName, f);
    }
    
    private void assertShape(Img img) {
	if(width != img.width || height != img.height || channels != img.channels)
	    throw new IndexOutOfBoundsException("Image sizes do not match");
    }
    
    //https://stackoverflow.com/questions/20266201/3d-array-1d-flat-indexing
    private int to1D(int x, int y, int z) {
	//return idxCache1[x] + idxCache2[y] + z;
	return (x * channels * height) + (y * channels) + z;
    }
    private int to1DNoCache(int x, int y, int z) {
	return (x * channels * height) + (y * channels) + z;
    }
    private int[] to3D(int idx) {
	final int x = idx / (channels * height);
	idx -= (x * channels * height);
	final int y = idx / channels;
	final int z = idx % channels;
	return new int[]{ x, y, z };
    }
    
    
    public Img runOps(Op... ops) {
	Img out = new Img(width, height, channels);
	MPT.run(threadCount, values, new MTPListRunnable<Byte>() {
	    @Override
	    public void iter(int procID, int idx, Byte val) {
		int temp = val;
		for(Op op : ops) {
		    temp = op.run(idx, temp);
		}
		out.setVal(idx, (byte)temp);
	    }
	});
	this.ops.clear();
	return out;
    }
    
}
