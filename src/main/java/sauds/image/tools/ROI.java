/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sauds.image.tools;

import java.util.Objects;
import static sauds.image.tools.ImgInterface.threadCount;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

/**
 *
 * @author saud
 */
public class ROI extends ImgInterface {
    
    private Img img;
    private final int x,y,c;
    private final int valuesLen;

    public ROI(int x, int w, int y, int h, int c, int d, Img img) {
	this.x = x; width = w;
	this.y = y; height = h;
	this.c = c; channels = d;
	this.valuesLen = w*h*d;
	this.img = img;
    }
    
    @Override
    public void setVal(int i, byte val) {
	int[] coords = to3D(i);
	int x = coords[0];
	int y = coords[1];
	int c = coords[2];
	img.setVal(x+this.x, y+this.y, c+this.c, val);
    }
    @Override
    public void setVal(int x, int y, int c, byte val) {
	img.setVal(x+this.x, y+this.y, c+this.c, val);
    }
    @Override
    public byte getVal(int x, int y, int c) {
	return img.getVal(x+this.x, y+this.y, c+this.c);
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
	int[] coords = to3D(i);
	int x = coords[0];
	int y = coords[1];
	int c = coords[2];
	return img.getInt(x+this.x, y+this.y, c+this.c);
    }
    @Override
    public int getInt(int x, int y, int c) {
	return getVal(x, y, c) & 0xFF;
    }
    @Override
    public Integer getInt(int x, int y, int c, int borderHandling) {
	return getVal(x, y, c, borderHandling) & 0xFF;
    }
    
    @Override
    public int[] minMax() {
	int[][] temp = new int[2][threadCount];
	MPT.run(threadCount, 0, valuesLen, 1, new MTPListRunnable<Byte>() {
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
    @Override
    public Img runOps(Op... ops) {
	Img out = new Img(width, height, channels);
	MPT.run(threadCount, 0, valuesLen, 1, new MTPListRunnable<Byte>() {
	    @Override
	    public void iter(int procID, int idx, Byte val) {
		int temp = getInt(idx);
		for(Img.Op op : ops) {
		    temp = op.run(idx, temp);
		}
		out.setVal(idx, (byte)temp);
	    }
	});
	return out;
    }
    
    public Img toImg2() {
	Img out = new Img(width, height, channels);
	MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
	    @Override
	    public void iter(int procID, int y, Object val) {
		for(int x=0; x<width; x++) {
		    for(int c=0; c<channels; c++) {
			out.setVal(x, y, c, getVal(x, y, c));
		    }
		}
	    }
	});
	return out;
    }
    
    private void assertShape(ImgInterface img) {
	if(width != img.getWidth() || height != img.getHeight() || channels != img.getChannels())
	    throw new IndexOutOfBoundsException("Image sizes do not match");
    }
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

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 23 * hash + Objects.hashCode(this.img);
	hash = 23 * hash + this.x;
	hash = 23 * hash + this.y;
	hash = 23 * hash + this.c;
	hash = 23 * hash + this.valuesLen;
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
	final ROI other = (ROI) obj;
	if (this.x != other.x) {
	    return false;
	}
	if (this.y != other.y) {
	    return false;
	}
	if (this.c != other.c) {
	    return false;
	}
	if (this.valuesLen != other.valuesLen) {
	    return false;
	}
	if (!Objects.equals(this.img, other.img)) {
	    return false;
	}
	return true;
    }
    
}
