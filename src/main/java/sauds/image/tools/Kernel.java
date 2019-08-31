
package sauds.image.tools;

import java.util.Arrays;

/**
 *
 * @author saud
 */
public class Kernel {
    
    private final int size;
    private final int width;
    private final int halfWidth;
    private final int height;
    private final int halfHeight;
    private final int[] k;
    private final int[] vComp;
    private final int[] hComp;

	private Kernel(int width, int height, int[] k, int[] vComp, int[] hComp) {
		this.width = width;
		this.halfWidth = width/2;
		this.height = height;
		this.halfHeight = height/2;
		this.size = width*height;
		this.k = k;
		this.vComp = vComp;
		this.hComp = hComp;
	}
    public Kernel(int width, int... values) {
		if(values.length % width != 0)
			throw new RuntimeException("Length of values is not a multiple of width, cannot make a rectangle");

		this.width = width;
		this.height = values.length/width;

		if(width%2==0 || height%2==0)
			throw new RuntimeException("Kernel must have an odd numbered width and height");

		this.halfWidth = width/2;
		this.halfHeight = height/2;
		this.size = width*height;
		this.k = values;
		
		if(k.length >1000) {
			int[][] sep = separate();
			vComp = sep[0];
			hComp = sep[1];
		} else {
			vComp = null;
			hComp = null;
		}
    }
	public Kernel(int[] verticalComp, int[] horizontalComp) {
		vComp = verticalComp;
		hComp = horizontalComp;
		k = null;
		width = hComp.length;
		height = vComp.length;
		this.size = width*height;
		this.halfWidth = width/2;
		this.halfHeight = height/2;
	}
	
	public static Kernel sobelX() {
		int[] vals =  new int[] {
				-1, 0, 1,
				-2, 0, 2,
				-1, 0, 1};
		Kernel out = new Kernel(3, 3, vals, null, null) {
			@Override
			public int get(int x, int y) {
				return (x==1) ? 0 : ((x==2) ? ((y&1)+1) : ~( ((y&1)+1) )+1 ) ;
			}
		};
		return out;
	}
	public static Kernel sobelY() {
		int[] vals =  new int[] {
				-1,-2,-1,
				0, 0, 0,
				1, 2, 1};
		Kernel out = new Kernel(3, 3, vals, null, null) {
			@Override
			public int get(int x, int y) {
				return (y==1) ? 0 : ((y==2) ? ((x&1)+1) : ~( ((x&1)+1) )+1 ) ;
			}
		};
		return out;
	}
	public static Kernel edgeDetection4() {
		int[] vals =  new int[] {
				 0,-1, 0,
				-1, 4,-1,
				 0,-1, 0};
		Kernel out = new Kernel(3, 3, vals, null, null) {
			@Override
			public int get(int x, int y) {
				return (y==1 && x==1) ? -8 : 1;
			}
		};
		return out;
	}
	public static Kernel edgeDetection8() {
		int[] vals =  new int[] {
				-1,-1,-1,
				-1, 8,-1,
				-1,-1,-1};
		Kernel out = new Kernel(3, 3, vals, null, null) {
			@Override
			public int get(int x, int y) {
				return (y==1 && x==1) ? -8 : 1;
			}
		};
		return out;
	}
    
    public static Kernel boxBlur(int radius) {
		int width = radius * 2 + 1;
		Kernel out = new Kernel(width, width, new int[]{1,1,1, 1,1,1, 1,1,1}, null, null) {
			@Override
			public int getC(int xFromCenter, int yFromCenter) {
				return 1;
			}
			@Override
			public int get(int x, int y) {
				return 1;
			}
			@Override
			public int get(int i) {
				return 1;
			}
		};
		return out;
    }
	
	/**
	 * Used for custom gaussian kernels for 3x3, 5x5, or 7x7, used the provided
	 * methods.
	 * @param width the width/height of the kernel
	 * @param sigma adjusts the spread of the distribution
	 * @param multiplier the number to multiply the results by in order to work
	 * with discrete values
	 * @return 
	 */
    public static Kernel gaussian(int width, double sigma, double multiplier) {
		double[] vals1D = new double[width];
		double denom = 2 * Math.pow(sigma, 2);
		for(int x=0; x<width; x++) {
			int xx = x - width/2;
			vals1D[x] = Math.exp(-(xx*xx)/denom) * multiplier;
		}
		int[] vals = new int[width*width];
		for(int x=0; x<width; x++) {
			for(int y=0; y<width; y++) {
				vals[y*width + x] = (int) (vals1D[x] * vals1D[y]);
				System.out.print(vals[y*width + x]+" ");
			}
			System.out.println();
		}
		Kernel out = new Kernel(width, vals);
		return out;
    }
	public static Kernel gaussian3x3() {
		Kernel out = new Kernel(3, 3, new int[]{1,2,1, 2,4,2, 1,2,1}, null, null) {
			@Override
			public int get(int x, int y) {
				return ((x&1)+1) << (y&1);
			}
		};
		return out;
	}
	public static Kernel gaussian5x5() {
		return gaussian(5, 1.05, 6.401);
	}
	public static Kernel gaussian7x7() {
		return gaussian(7, 1.0141, 12.609);
	}
    
    public int getWidth() {
	return width;
    }
    public int getHW() {
	return halfWidth;
    }
    public int getHeight() {
	return height;
    }
    public int getHH() {
	return halfHeight;
    }
	
	public int getSize() {
		return size;
	}
    
    public int getC(int xFromCenter, int yFromCenter) {
	return get(xFromCenter+halfWidth, yFromCenter+halfHeight);
    }
    
    public int get(int x, int y) {
	return get(y*width + x); 
    }
	
    public int get(int i) {
	return k[i]; 
    }
	
	public int[] getKernel() {
		return k;
	}
	
	public boolean isSeparable() {
		return hComp != null;
	}
	private int[][] separate() {
		int[] ver = new int[height];
		int[] hor = new int[width];
		
		int min = Integer.MAX_VALUE;
		for(int i=0; i<width; i++) {
			int temp = get(i, 0);
			if(temp!=0 && Math.abs(temp) < Math.abs(min))
				min = temp;
		}
		for(int i=0; i<width; i++) {
			int val = get(i, 0);
			if(val%min != 0) return new int[][] {null, null};
			hor[i] = val/min;
		}
		
		min = Integer.MAX_VALUE;
		for(int i=0; i<height; i++) {
			int temp = get(i, 0);
			if(temp!=0 && Math.abs(temp) < Math.abs(min))
				min = temp;
		}
		for(int i=0; i<height; i++) {
			int val = get(0, i);
			if(val%min != 0) return new int[][] {null, null};
			ver[i] = val/min;
		}
		
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++) {
				int a=hor[i], b=ver[j], c=get(i,j);
				if(hor[i]*ver[j] != get(i, j))
					return new int[][] {null, null};
			}
		}
		
		return new int[][] {ver, hor};
	}
	
	public Kernel getHKernel() {
		return new Kernel(width, hComp);
	}
	
	public Kernel getVKernel() {
		return new Kernel(1, vComp);
	}
	
	public int[] getHComp() {
		return Arrays.copyOf(hComp, hComp.length);
	}
	
	public int[] getVComp() {
		return Arrays.copyOf(vComp, vComp.length);
	}
	
}
