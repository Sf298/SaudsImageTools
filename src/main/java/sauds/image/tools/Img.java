
package sauds.image.tools;

import sauds.image.tools.external.SmallCache;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import static sauds.image.tools.ImgInterface.BORDER_IGNORE;
import static sauds.image.tools.ImgInterface.BORDER_INNER;
import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

/**
 *
 * @author saud
 */
public class Img extends ImgInterface {
    
	public static final int CONV_SUM = 0;
	public static final int CONV_MEAN = 1;
	public static final int CONV_MEDIAN = 2;
	
    private byte[] values;
	public byte[] getValues() {
		return values;
	}
    
	protected Img(){}
	@Override
	public Img create(int width, int height, int channels) {
		Img out = new Img();
		out.width = width;
		out.height = height;
		out.channels = channels;
		//initIdxCaches();
		out.values = new byte[width*height*channels];
		return out;
	}
	@Override
    public Img create(int width, int height, int channels, byte initialValue) {
		Img out = create(width, height, channels);
		MPT.run(threadCount, out.values, new MTPListRunnable<Byte>() {
			@Override
			public void iter(int procID, int idx, Byte b) {
				out.values[idx] = initialValue;
			}
		});
		return out;
	}
	@Override
    public Img copy() {
		Img out = new Img();
		out.width = width;
		out.height = height;
		out.channels = channels;
		//idxCache1 = Arrays.copyOf(im.idxCache1, im.idxCache1.length);
		//idxCache2 = Arrays.copyOf(im.idxCache2, im.idxCache2.length);
		out.values = Arrays.copyOf(values, values.length);
		return out;
    }
	/**
	 * Create a new blank image of given size.
	 * @param width
	 * @param height
	 * @param channels
	 * @return 
	 */
	public static Img createNew(int width, int height, int channels) {
		return new Img().create(width, height, channels);
	}
	/**
	 * Create a new image of given size. It is then filled with the given value.
	 * @param width
	 * @param height
	 * @param channels
	 * @param initialValue
	 * @return 
	 */
	public static Img createNew(int width, int height, int channels, byte initialValue) {
		return new Img().create(width, height, channels, initialValue);
	}
	/**
	 * Create a new image of given size. It is then filled with the given value.
	 * @param width
	 * @param height
	 * @param channels
	 * @param initialValue
	 * @return 
	 */
	public static Img createNew(int width, int height, int channels, int initialValue) {
		return (Img) new Img().create(width, height, channels, initialValue);
	}
	/**
	 * Create a new Img object from the provided BufferedImage.
	 * @param img
	 * @return 
	 */
    public static Img createNew(BufferedImage img) {
		return (Img) new Img().create(img);
	}
	/**
	 * Create a new Img object from the provided image file.
	 * @param f
	 * @return 
	 * @throws IOException if an error occurs during reading.
	 */
    public static Img createNew(File f) throws IOException {
		return (Img) new Img().create(f);
	}
    
    @Override
    public byte getVal(int i) {
		return values[i];
    }
    @Override
    public void setVal(int i, byte val) {
		values[i] = val;
    }
    @Override
    public byte getVal(int x, int y, int c) {
		return values[to1D(x,y,c)];
    }
    @Override
    public void setVal(int x, int y, int c, byte val) {
	values[to1D(x,y,c)] = val;
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
			/*case BORDER_WRAP:
				while(x<0) x += width; if(x >= width) x = x%width;
				while(y<0) y += height; if(y >= height) y = y%height;
				while(c<0) c += channels; if(c >= channels) c = c%channels;
				return getVal(x,y,c);*/
		}
		return null;
    }
    @Override
    public int getInt(int i) {
		return values[i] & 0xFF;
    }
	@Override
	public void setInt(int i, int val) {
		setVal(i, (byte) val);
	}
    @Override
    public int getInt(int x, int y, int c) {
		return getVal(x, y, c) & 0xFF;
    }
	@Override
	public void setInt(int x, int y, int c, int val) {
		setVal(x, y, c, (byte) val);
	}
    @Override
    public Integer getInt(int x, int y, int c, int borderHandling) {
		Byte val = getVal(x, y, c, borderHandling);
		if(val == null) return null;
		return val & 0xFF;
    }
	@Override
	public Color getColor(int x, int y) {
		int pos = to1D(x,y,0);
		if(channels == 1) {
			int temp = getInt(pos);
			return new Color(temp, temp, temp);
		} else if(channels == 2) {
			return new Color(getInt(pos), getInt(pos+1), 0);
		} else if(channels == 3) {
			return new Color(getInt(pos), getInt(pos+1), getInt(pos+2));
		} else if(channels == 4) {
			return new Color(getInt(pos), getInt(pos+1), getInt(pos+2), getInt(pos+3));
		}
		return null;
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
		for(int i=0; i<temp[0].length; i++) {
			out[0] = Math.min(out[0], temp[0][i]);
			out[1] = Math.max(out[1], temp[1][i]);
		}
		return out;
    }
	
	/**
	 * Calculates the sum of all pixels in each channel.
	 * @return 
	 */
    public long[] sumChannelValues() {
		long[][] rawSum = new long[threadCount][channels];

		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						rawSum[procID][c] += getInt(x, y, c);
					}
				}
			}
		});
		long[] out = new long[channels];
		for(int i=0; i<threadCount; i++) {
			for(int j=0; j<channels; j++) {
				out[j] += rawSum[i][j];
			}
		}
		return out;
    }
	
	/**
	 * Calculates the sum of all sub pixels in the image.
	 * @return 
	 */
    public long sumAllValues() {
		long[] rawSum = sumChannelValues();
		long out = 0;
		for(int i=0; i<rawSum.length; i++)
			out += rawSum[i];
		return out;
    }
    
	/**
	 * Convolve this image with a kernel.
	 * @param k the kernel
	 * @param borderHandling how to handle edge cases
	 * @param stride the stride
	 * @param operation i.e. sum, mean, median
	 * @param abs whether or not to take the absolute of the calculated value
	 * @param add the number to add to each pixel of the output image
	 * @return 
	 */
    public Img convolve(Kernel k, int borderHandling, int stride, int operation, boolean abs, int add) {
		if(k.isSeparable())
			return convolveSeparableV(k.getVComp(), borderHandling, stride, operation, abs, add)
					.convolveSeparableH(k.getHComp(), borderHandling, stride, operation, abs, add);
		return convolveGeneral(k, borderHandling, stride, operation, abs, add);
	}
    private Img convolveGeneral(Kernel k, int borderHandling, int stride, int operation, boolean abs, int add) {
		Img out;
		int wMod;
		int hMod;
		if(borderHandling == BORDER_INNER) {
			out = create(width-k.getHW()*2, height-k.getHH()*2, channels);
			wMod = k.getHW();
			hMod = k.getHH();
		} else {
			out = create(width, height, channels);
			wMod = 0;
			hMod = 0;
		}

		MPT.run(threadCount, hMod, height-hMod, stride, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				int endX = width-wMod;
				for(int x=wMod; x<endX; x+=stride) {
					for(int c=0; c<channels; c++) {
						int finalVal = convRegion(k, x, y, c, borderHandling, operation);
						
						if(abs)
							out.setInt(x, y, c, Math.min(Math.abs(finalVal+add), 255));
						else
							out.setInt(x, y, c, Math.max(0, Math.min(finalVal+add, 255)));
					}
				}
			}
		});
		return out;
    }
	public static int median(ArrayList<Integer> list) {
		Collections.sort(list);
		int s = list.size();
		int hs = s>>>1;
		if((s&1) == 0) {
			return (list.get(hs-1) + list.get(hs))>>>1;
		} else {
			return list.get(hs);
		}
	}
	
	/**
	 * Convolve this image with a kernel.
	 * @param k the kernel
	 * @param borderHandling how to handle edge cases
	 * @param stride the stride
	 * @param operation i.e. sum, mean, median
	 * @return 
	 */
    public Img convolve(Kernel k, int borderHandling, int stride, int operation) {
		if(k.isSeparable())
			return convolveSeparableV(k.getVComp(), borderHandling, stride, operation, false, 0)
					.convolveSeparableH(k.getHComp(), borderHandling, stride, operation, false, 0);
		return convolveGeneral(k, borderHandling, stride, operation);
	}
    private Img convolveGeneral(Kernel k, int borderHandling, int stride, int operation) {
		Img out;
		int wMod;
		int hMod;
		if(borderHandling == BORDER_INNER) {
			out = create(width-k.getHW()*2, height-k.getHH()*2, channels);
			wMod = k.getHW();
			hMod = k.getHH();
		} else {
			out = create(width, height, channels);
			wMod = 0;
			hMod = 0;
		}

		MPT.run(threadCount, hMod, height-hMod, stride, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				int endX = width-wMod;
				for(int x=wMod; x<endX; x+=stride) {
					for(int c=0; c<channels; c++) {
						int finalVal = convRegion(k, x, y, c, borderHandling, operation);
						out.setInt(x, y, c, Math.max(0, Math.min(finalVal, 255)));
					}
				}
			}
		});
		return out;
    }
	
	private int convRegion(Kernel k, int cx, int cy, int c, int borderHandling, int operation) {
		if(operation == CONV_SUM) {
			int sum = 0;
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					Integer imVal = getInt(cx+dx, tempY, c, borderHandling);
					if(imVal==null) continue;
					sum += k.getC(dx, dy) * imVal;
				}
			}
			return sum;
		} else if(operation == CONV_MEAN) {
			int sum = 0;
			int count = 0;
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					Integer imVal = getInt(cx+dx, tempY, c, borderHandling);
					if(imVal==null) continue;
					int kV = k.getC(dx, dy);
					sum += kV * imVal;
					count+=Math.abs(kV);
				}
			}
			return sum/count;
		} else if(operation == CONV_MEDIAN) {
			ArrayList<Integer> values = new ArrayList<>(k.getSize());
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					Integer imVal = getInt(cx+dx, tempY, c, borderHandling);
					if(imVal==null) continue;
					values.add(k.getC(dx, dy) * imVal);
				}
			}
			return median(values);
		}
		return 0;
	}
    private Img convolveSeparableH(int[] k, int borderHandling, int stride, int operation, boolean abs, int add) {
		Img out;
		int wMod;
		int hw = (k.length/2);
		if(borderHandling == BORDER_INNER) {
			out = create(width-hw*2, height, channels);
			wMod = hw;
		} else {
			out = create(width, height, channels);
			wMod = 0;
		}
		int endX = width-wMod;
		
		// horizontal kernel
		MPT.run(threadCount, 0, height, stride, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				// init cache
				SmallCache[] scs = new SmallCache[channels];
				for(int i=0; i<scs.length; i++) scs[i] = new SmallCache(k.length);
				for(int i=0; i<k.length; i++) {
					for(int j=0; j<channels; j++) {
						Integer temp = getInt(i-hw, y, j, borderHandling);
						scs[j].arr[i] = temp;
					}
				}
				for(int x=wMod; x<endX; x+=stride) {
					for(int c=0; c<channels; c++) {
						// multiply
						int newVal = convArrs(k, scs[c].arr, operation);
						out.setInt(x, y, c, newVal+add);
						
						// update cache
						scs[c].add(getInt(x+hw+1, y, c, borderHandling));
					}
				}
			}
		});
		return out;
    }
    private Img convolveSeparableV(int[] k, int borderHandling, int stride, int operation, boolean abs, int add) {
		Img out;
		int hMod;
		int hh = (k.length/2);
		if(borderHandling == BORDER_INNER) {
			out = create(width, height-hh*2, channels);
			hMod = hh;
		} else {
			out = create(width, height, channels);
			hMod = 0;
		}
		int endY = height-hMod;
		
		// horizontal kernel
		MPT.run(threadCount, 0, width, stride, new MTPListRunnable() {
			@Override
			public void iter(int procID, int x, Object val) {
				// init cache
				SmallCache[] scs = new SmallCache[channels];
				for(int i=0; i<scs.length; i++) scs[i] = new SmallCache(k.length);
				for(int i=0; i<k.length; i++) {
					for(int j=0; j<channels; j++) {
						scs[j].arr[i] = getInt(x, i-hh, j, borderHandling);
					}
				}
				for(int y=hMod; y<endY; y+=stride) {
					for(int c=0; c<channels; c++) {
						// multiply
						int newVal = convArrs(k, scs[c].arr, operation);
						out.setInt(x, y, c, newVal+add);
						
						// update cache
						scs[c].add(getInt(x, y+hh+1, c, borderHandling));
					}
				}
			}
		});
		return out;
    }
	/**
	 * Calculates the median of an ArrayList of Integers.
	 * @param list
	 * @return 
	 */
    private synchronized int convArrs(int[] k, Integer[] roi, int op) {
		if(op == CONV_SUM) {
			int sum = 0;
			for(int i=0; i<k.length; i++) {
				Integer imVal = roi[i];
				if(imVal==null) continue;
				sum += k[i] * imVal;
			}
			return sum;
		} else if(op == CONV_MEAN) {
			Integer test = 0;
			int sum = 0;
			int count = 0;
			for(int i=0; i<k.length; i++) {
				Integer imVal = roi[i];
				if(imVal==null) {
					test = i;
					continue;
				}
				sum += k[i] * imVal;
				count++;
			}
			if(count == 0) {
				System.out.println("roi "+Arrays.toString(roi)+" k "+Arrays.toString(k)+" "+test);
			}
			return sum/count;
		} else if(op == CONV_MEDIAN) {
			ArrayList<Integer> values = new ArrayList<>();
			for(int i=0; i<k.length; i++) {
				Integer imVal = roi[i];
				if(imVal==null) continue;
				values.add(k[i] * imVal);
			}
			return median(values);
		}
		return 0;
	}
	
	//<editor-fold defaultstate="collapsed" desc="region blur">
	/*private Img regionBlur(Kernel k, int operation) {
		Img out = create(width, height, channels);;
		int wMod = 0;
		int hMod = 0;
		
		MPT.run(threadCount, hMod, height-hMod, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				int endX = width-wMod;
				for(int x=wMod; x<endX; x++) {
					for(int c=0; c<channels; c++) {
						int finalVal = convRegion(k, x, y, c, BORDER_IGNORE, operation);
						out.setInt(x, y, c, Math.max(0, Math.min(finalVal, 255)));
					}
				}
			}
		});
		return out;
	}
	private int convRegionInBlob(Kernel k, Blob blob, int cx, int cy, int c, int operation) {
		if(operation == CONV_SUM) {
			int sum = 0;
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					if(blob.==null) continue;
					int imVal = getInt(cx+dx, tempY, c);
					sum += k.getC(dx, dy) * imVal;
				}
			}
			return sum;
		} else if(operation == CONV_MEAN) {
			int sum = 0;
			int count = 0;
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					Integer imVal = getInt(cx+dx, tempY, c, borderHandling);
					if(imVal==null) continue;
					int kV = k.getC(dx, dy);
					sum += kV * imVal;
					count+=Math.abs(kV);
				}
			}
			return sum/count;
		} else if(operation == CONV_MEDIAN) {
			ArrayList<Integer> values = new ArrayList<>(k.getSize());
			for(int dy=-k.getHW(); dy<=k.getHW(); dy++) {
				int tempY = cy+dy;
				for(int dx=-k.getHH(); dx<=k.getHH(); dx++) {
					Integer imVal = getInt(cx+dx, tempY, c, borderHandling);
					if(imVal==null) continue;
					values.add(k.getC(dx, dy) * imVal);
				}
			}
			return median(values);
		}
		return 0;
	}*/
//</editor-fold>
	
	/**
	 * Performs a 3x3 median blur on the image.
	 * @return 
	 */
	public Img removeBadPixels() {
		return convolve(Kernel.boxBlur(1), BORDER_IGNORE, 1, CONV_MEDIAN);
	}
	protected Integer isBadPixel(int x, int y, int c) {
		int aa = getInt(x, y, c);
		int bb = getInt(x+1, y, c);
		int cc = getInt(x, y+1, c);
		int dd = getInt(x-1, y, c);
		int ee = getInt(x, y-1, c);
		
		int mean = 0;
		int count = 0;
		if(aa == 255) {
			if(bb < 250) {
				count++;
				mean += bb;
			}
			if(cc < 250) {
				count++;
				mean += cc;
			}
			if(dd < 250) {
				count++;
				mean += dd;
			}
			if(ee < 250) {
				count++;
				mean += ee;
			}
		} else if(aa == 0) {
			if(bb > 10) {
				count++;
				mean += bb;
			}
			if(cc > 10) {
				count++;
				mean += cc;
			}
			if(dd > 10) {
				count++;
				mean += dd;
			}
			if(ee > 10) {
				count++;
				mean += ee;
			}
		}
		return (count>=3) ? mean/count : null;
	}
    
	
	/**
	 * Save the Img into a file.
	 * @param formatName a String containing the informal name of the format.
	 * @param f The file to save to.
	 * @exception IllegalArgumentException if any parameter is
     * <code>null</code>.
     * @exception IOException if an error occurs during writing.
	 */
    public void save(String formatName, File f) throws IOException {
		ImageIO.write(toBufferedImage(), formatName, f);
    }
    
    // source: https://stackoverflow.com/questions/20266201/3d-array-1d-flat-indexing
    private int to1D(int x, int y, int z) {
		return (y * channels * width) + (x * channels) + z;
		//return (x * channels * height) + (y * channels) + z;
	}
	private int to1DCache(int x, int y, int z) {
		//return idxCache1[x] + idxCache2[y] + z;
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
    public Img runOp(Op op) {
		Img out = create(width, height, channels);
		MPT.run(threadCount, values, new MTPListRunnable<Byte>() {
			@Override
			public void iter(int procID, int idx, Byte val) {
				out.setInt(idx, op.run(procID, idx, val&0xFF));
			}
		});
		return out;
	}

    @Override
    public int hashCode() {
		int hash = 3;
		hash = 17 * hash + super.hashCode();
		hash = 17 * hash + Arrays.hashCode(this.values);
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
		final Img other = (Img) obj;
		if(!super.equals(other)) {
			return false;
		}
		if (!Arrays.equals(this.values, other.values)) {
			return false;
		}
		return true;
    }
    
}
