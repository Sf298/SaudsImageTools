package sauds.image.tools2;

import java.util.Arrays;

import static java.util.Objects.nonNull;

public class Kernel {

    private final int w;
    private final int h;
    private final int valuesSum;
    private final int[] values;
    private final Kernel hComponent;
    private final Kernel vComponent;

    public static Kernel boxBlur(int radius) {
        int width = radius * 2 + 1;
        if (radius < 3) {
            int[] values = new int[width*width];
            Arrays.fill(values, 1);
            return new Kernel(width, width, values);
        } else {
            int[] values = new int[width];
            Arrays.fill(values, 1);
            return new Kernel(width, width, values, values);
        }
    }

    public static Kernel sobelX() {
        int[] values = {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
        };
        return new Kernel(3, 3, values);
    }
    public static Kernel sobelY() {
        int[] values = {
            -1,-2,-1,
            0, 0, 0,
            1, 2, 1
        };
        return new Kernel(3, 3, values);
    }
    public static Kernel edgeDetection4() {
        int[] values =  {
             0,-1, 0,
            -1, 4,-1,
             0,-1, 0
        };
        return new Kernel(3, 3, values);
    }
    public static Kernel edgeDetection8() {
        int[] values =  new int[] {
                -1,-1,-1,
                -1, 8,-1,
                -1,-1,-1};
        return new Kernel(3, 3, values);
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
        /*int[] vals1D = new int[width];
        double denom = 2 * Math.pow(sigma, 2);
        for(int x=0; x<width; x++) {
            int xx = x - width/2;
            vals1D[x] = (int) Math.round(Math.exp(-(xx*xx)/denom) * multiplier);
        }
        return new Kernel(width, width, vals1D, vals1D);*/
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
        return new Kernel(width, width, vals);
    }
    public static Kernel gaussian3x3() {
        int[] values = {
                1,2,1,
                2,4,2,
                1,2,1
        };
        return new Kernel(3, 3, values);
    }
    public static Kernel gaussian5x5() {
        return gaussian(5, 1.05, 6.401);
    }
    public static Kernel gaussian7x7() {
        return gaussian(7, 1.0141, 12.609);
    }


    public Kernel(int w, int h, int[] values) {
        this(w, h, values, null, null);
    }

    public Kernel(int w, int h, int[] hComponent, int[] vComponent) {
        this(w, h, null, hComponent, vComponent);
    }

    public Kernel(int w, int h, int[] values, int[] hComponent, int[] vComponent) {
        this.w = w;
        this.h = h;
        this.values = values;
        this.hComponent = nonNull(hComponent) ? new Kernel(w, 1, hComponent) : null;
        this.vComponent = nonNull(vComponent) ? new Kernel(1, h, vComponent) : null;

        if (isSeparable()) {
            int sum = 0;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    sum += Math.abs(hComponent[i]) * Math.abs(vComponent[j]);
                }
            }
            this.valuesSum = sum;
        } else {
            this.valuesSum = Arrays.stream(values).sum();
        }
    }

    public int getW() {
        return w;
    }
    public int getH() {
        return h;
    }
    public int[] getValues() {
        return values;
    }
    public Kernel getHComponent() {
        return hComponent;
    }
    public Kernel getVComponent() {
        return vComponent;
    }
    public boolean isSeparable() {
        return nonNull(hComponent);
    }

    public final int get(int x, int y) {
        return values[y*w+x];
    }

    /**
     * Get the horizontal radius of this kernel (half of the width)
     * @return the half width
     */
    public int getHW() {
        return w >> 1;
    }

    /**
     * Get the horizontal radius of this kernel (half of the width)
     * @return the half width
     */
    public int getHH() {
        return h >> 1;
    }

    /**
     * Get the horizontal radius of this kernel (half of the width)
     * @return the half width
     */
    public int getValuesSum() {
        return valuesSum;
    }

}
