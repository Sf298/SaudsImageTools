package sauds.image.tools;

import java.util.Arrays;

import static java.util.Objects.nonNull;

public class Kernel {

    private final int w;
    private final int h;
    private final int valuesSum;
    private final int[] values;
    private final Kernel hComponent;
    private final Kernel vComponent;

    /**
     * Generates a box blur {@link Kernel}.
     * @param radius The width/height of the blur.
     * @return The generated {@link Kernel}.
     */
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

    /**
     * Generates a {@link Kernel} of all 1s in the horizontal direction.
     * @param radius The width of the blur.
     * @return The generated {@link Kernel}.
     */
    public static Kernel boxBlurH(int radius) {
        int width = radius * 2 + 1;
        int[] values = new int[width];
        Arrays.fill(values, 1);
        return new Kernel(width, 1, values);
    }

    /**
     * Generates a {@link Kernel} of all 1s in the vertical direction.
     * @param radius The height of the blur.
     * @return The generated {@link Kernel}.
     */
    public static Kernel boxBlurV(int radius) {
        int width = radius * 2 + 1;
        int[] values = new int[width];
        Arrays.fill(values, 1);
        return new Kernel(1, width, values);
    }

    /**
     * Gets a 3x3 sobel {@link Kernel} that is oriented in the X direction.
     * @return The generated {@link Kernel}.
     */
    public static Kernel sobelX() {
        int[] values = {
            -1, 0, 1,
            -2, 0, 2,
            -1, 0, 1
        };
        return new Kernel(3, 3, values);
    }

    /**
     * Gets a 3x3 sobel {@link Kernel} that is oriented in the Y direction.
     * @return The generated {@link Kernel}.
     */
    public static Kernel sobelY() {
        int[] values = {
            -1,-2,-1,
            0, 0, 0,
            1, 2, 1
        };
        return new Kernel(3, 3, values);
    }

    /**
     * Gets a 4 direction, 3x3 edge detection {@link Kernel}.
     * @return The generated {@link Kernel}.
     */
    public static Kernel edgeDetection4() {
        int[] values =  {
             0,-1, 0,
            -1, 4,-1,
             0,-1, 0
        };
        return new Kernel(3, 3, values);
    }

    /**
     * Gets an 8 direction, 3x3 edge detection {@link Kernel}.
     * @return The generated {@link Kernel}.
     */
    public static Kernel edgeDetection8() {
        int[] values =  new int[] {
                -1,-1,-1,
                -1, 8,-1,
                -1,-1,-1};
        return new Kernel(3, 3, values);
    }

    /**
     * Generates a custom gaussian {@link Kernel}. For 3x3, 5x5, or 7x7, used the provided methods.
     * @param width the width/height of the kernel.
     * @param sigma adjusts the spread of the distribution.
     * @param multiplier the number to multiply the results by in order to work
     * with discrete values.
     * @return The generated {@link Kernel}.
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

    /**
     * Gets a 3x3 gaussian {@link Kernel}.
     * @return The generated {@link Kernel}.
     */
    public static Kernel gaussian3x3() {
        int[] values = {
                1,2,1,
                2,4,2,
                1,2,1
        };
        return new Kernel(3, 3, values);
    }

    /**
     * Gets a 5x5 gaussian {@link Kernel}.
     * @return The generated {@link Kernel}.
     */
    public static Kernel gaussian5x5() {
        return gaussian(5, 1.05, 6.401);
    }

    /**
     * Gets a 7x7 gaussian {@link Kernel}.
     * @return The generated {@link Kernel}.
     */
    public static Kernel gaussian7x7() {
        return gaussian(7, 1.0141, 12.609);
    }


    /**
     * Create a custom standard {@link Kernel}.
     * @param w The width of the {@link Kernel}.
     * @param h The height of the {@link Kernel}.
     * @param values A 1D array of values representing the kernel.
     */
    public Kernel(int w, int h, int[] values) {
        this(w, h, values, null, null);
    }

    /**
     * Create a custom separable {@link Kernel}.
     * @param w The width of the {@link Kernel}.
     * @param h The height of the {@link Kernel}.
     * @param hComponent The horizontal component of the separable {@link Kernel}.
     * @param vComponent The vertical component of the separable {@link Kernel}.
     */
    public Kernel(int w, int h, int[] hComponent, int[] vComponent) {
        this(w, h, null, hComponent, vComponent);
    }

    private Kernel(int w, int h, int[] values, int[] hComponent, int[] vComponent) {
        this.w = w;
        this.h = h;
        this.values = values;
        this.hComponent = nonNull(hComponent) ? new Kernel(w, 1, hComponent) : null;
        this.vComponent = nonNull(vComponent) ? new Kernel(1, h, vComponent) : null;

        if (isSeparable()) {
            int sum = 0;
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    //sum += Math.abs(hComponent[i]) * Math.abs(vComponent[j]);
                    sum += hComponent[i] * vComponent[j];
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
