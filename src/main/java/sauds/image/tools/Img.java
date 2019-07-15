
package sauds.image.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.imageio.ImageIO;
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
	
    private final byte[] values;
    
    public Img(int width, int height, int channels) {
		this.width = width;
		this.height = height;
		this.channels = channels;
		//initIdxCaches();
		values = new byte[width*height*channels];
    }
    public Img(int width, int height, int channels, byte initialValue) {
		this(width, height, channels);
		MPT.run(threadCount, values, new MTPListRunnable<Byte>() {
			@Override
			public void iter(int procID, int idx, Byte b) {
				values[idx] = initialValue;
			}
		});
    }
    public Img(int width, int height, int channels, int initialValue) {
		this(width, height, channels, (byte)initialValue);
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
		if(img == null)
			throw new NullPointerException("Could not read image, img may be null");

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
		values = new byte[width*height*channels];

		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int idx, Object val) {
				int y = idx;
				for(int x=0; x<width; x++) {
					Color col = new Color(img.getRGB(x, y));
					if(1 <= channels) setInt(x,y,0, col.getRed());
					if(2 <= channels) setInt(x,y,1, col.getGreen());
					if(3 <= channels) setInt(x,y,2, col.getBlue());
					if(4 <= channels) setInt(x,y,3, col.getAlpha());
				}
			}
		});
	}
	public Img(File f) throws IOException {
		this(ImageIO.read(f));
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
		long[][] rawSum = new long[channels][threadCount];

		MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
			@Override
			public void iter(int procID, int y, Object val) {
				for(int x=0; x<width; x++) {
					for(int c=0; c<channels; c++) {
						rawSum[c][procID] += getInt(x, y, c);
					}
				}
			}
		});
		long[] out = new long[channels];
		for(int i=0; i<width; i++) {
			for(int j=0; j<channels; j++) {
				out[i] += rawSum[i][j];
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
	public Img upscale2x() {
		Img out = new Img(width*2, height*2, channels);
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
    
	/**
	 * Convert the image into grey scale. Returns a copy of this Img if it is
	 * already grey scale.
	 * @return 
	 */
    public Img toGrey() {
		if(channels == 1)
			return new Img(this);
		Img out = new Img(width, height, 1);
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
	 * Converts the Img into a laplacian pyramid.
	 * @param levelCount
	 * @return 
	 */
    public ArrayList<Img> laplacianPyramid(int levelCount) {
		ArrayList<Img> out = new ArrayList<>();
		Img color = this;
		for(int i=0; i<levelCount; i++) {
			Img redu = color.downScale2x();
			Img upSc = redu.upscale2x();
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
    public static Img laplacianPyramid(ArrayList<Img> pyramid) {
		Img out = pyramid.get(pyramid.size()-1);
		for(int i=pyramid.size()-2; i>=0; i--) {
			out = out.upscale2x().add(pyramid.get(i));
		}
		return out;
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
					for(int c=0; c<channels; c++) {
						int sum = 0;
						int count = 0;
						ArrayList<Integer> values = new ArrayList<>();
						for(int dx=-k.getHW(); dx<k.getHW(); dx++) {
							for(int dy=-k.getHH(); dy<k.getHH(); dy++) {
								int kVal = k.getC(dx, dy);
								Integer imVal = getInt(x+dx, y+dy, c, borderHandling);

								if(imVal==null) continue;
								if(operation <= CONV_MEAN) sum += kVal * imVal;
								if(operation == CONV_MEAN) count++;
								if(operation == CONV_MEDIAN) values.add(kVal * imVal);
							}
						}
						int finalVal = 0;
						if(operation == CONV_SUM) {
							finalVal = sum;
						} else if(operation == CONV_MEAN) {
							finalVal = sum/count;
						} else if(operation == CONV_MEDIAN) {
							finalVal = median(values);
						}
						out.setInt(x, y, c, Math.max(Byte.MIN_VALUE, Math.min(finalVal, Byte.MAX_VALUE)));
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
    public Img runOp(Op op) {
		Img out = new Img(width, height, channels);
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
