package sauds.image.tools;

import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Image {


    int threadCount = MPT.coreCount()-1;

    /**
     * Gets the width of this readable image's layer.
     * @return the width
     */
    int getWidth();

    /**
     * Gets the height of this readable image's layer.
     * @return the height
     */
    int getHeight();

    /**
     * Gets the number of channels in this readable image's layer.
     * @return the number of channels
     */
    int getDepth();

    /**
     * Gets the number of channels in this readable image's layer.
     * @return the number of channels
     */
    default int getSubpixelCount() {
        return getWidth() * getHeight() * getDepth();
    }

    /**
     * Get the list of layers in this stack.
     * @return the list of layers, with the base layer at index 0
     */
    List<Layer<?>> getLayers();

    /**
     * Checks if this readable image has any layers that are particularly slow to process (e.g. convolution).
     * @return true, if the layer stack contains a slow layer. Otherwise false
     */
    default boolean hasSlowLayer() {
        return getLayers().stream().anyMatch(Layer::isSlowLayer);
    }

    default int getInt(int i) {
        int[] coord = to3D(i);
        return getInt(coord[0], coord[1], coord[2]);
    }
    default int getInt(int x, int y, int c) {
        return getInt(to1D(x, y, c));
    } // must pass through
    default Integer getInt(int x, int y, int c, BorderHandling bh) {
        int w = getWidth(), h = getHeight(), d = getDepth();
        switch (bh) {
            case EXTEND:
                if(x < 0) x = 0; if(x >= w) x = w - 1;
                if(y < 0) y = 0; if(y >= h) y = h - 1;
                if(c < 0) c = 0; if(c >= d) c = d - 1;
                return getInt(x,y,c);
            case IGNORE:
                if(x < 0 || x >= w) return null;
                if(y < 0 || y >= h) return null;
                if(c < 0 || c >= d) return null;
                return getInt(x,y,c);
            case INNER:
                return getInt(x,y,c);
			/*case BORDER_WRAP:
				while(x<0) x += width; if(x >= width) x = x%width;
				while(y<0) y += height; if(y >= height) y = y%height;
				while(c<0) c += channels; if(c >= channels) c = c%channels;
				return getVal(x,y,c);*/
        }
        return null;
    }
    default Color getColor(int x, int y) {
        int pos = to1D(x,y,0);
        switch (getDepth()) {
            case 1:
                int temp = b(getInt(pos));
                return new Color(temp, temp, temp);
            case 2:
                return new Color(b(getInt(pos)), b(getInt(pos+1)), 0);
            case 3:
                return new Color(b(getInt(pos)), b(getInt(pos+1)), b(getInt(pos+2)));
            case 4:
                return new Color(b(getInt(pos)), b(getInt(pos+1)), b(getInt(pos+2)), b(getInt(pos+3)));
        }
        throw new UnsupportedOperationException("Failed to get color. Unsupported depth of "+getDepth());
    }
    default boolean isInBounds(int x, int y, int c) {
        return !(x<0 || y<0 || c<0 || x>=getWidth() || y>=getHeight() || c>=getDepth()); // todo find and remove duplicates
    }

    /**
     * Gets the sub pixel value at the given image coordinate. Interpolates or
     * averages if needed.
     * @param x the x coord
     * @param xW the number of pixels to average. Interpolates if 0.
     * @param y the y coord
     * @param yW the number of pixels to average. Interpolates if 0.
     * @param c the channel
     */
    default double getInterpolated(double x, int xW, double y, int yW, int c) {
        if((xW | yW) == 0) { // both need to interp
            int x1 = (int) Math.floor(x), x2 = (int) Math.ceil(x);
            int y1 = (int) Math.floor(y), y2 = (int) Math.ceil(y);
            double xR = x-(long)x;
            double v1 = linearInterpolation(getInt(x1,y1,c), getInt(x2,y1,c), xR);
            double v2 = linearInterpolation(getInt(x1,y2,c), getInt(x2,y2,c), xR);
            return linearInterpolation(v1, v2, y-(long)y);
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
                    Integer imVal = getInt(dx, dy, c, BorderHandling.IGNORE);
                    if(imVal != null) {
                        sum += imVal;
                        count++;
                    }
                }
            }
            return sum/(double)count;
        } else if(xW!=0 & yW==0) { // x needs to average, y need to interp
            int y1 = (int) Math.floor(y), y2 = (int) Math.ceil(y);
            double yR = y-(long)y;

            int halfXW = xW >>> 1; //xW / 2
            int xLeftPos = ( ((xW&1)==0) ? -halfXW + 1 : -halfXW) + (int) x;
            int xRightPos = halfXW + (int) x;

            double sum = 0;
            int count = 0;
            for(int dx=xLeftPos; dx<=xRightPos; dx++) {
                if(0 < dx && dx < getWidth()) {
                    double v1 = linearInterpolation(getInt(dx,y1,c), getInt(dx,y2,c), yR);
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
                if(0 < dy && dy < getHeight()) {
                    double v1 = linearInterpolation(getInt(x1,dy,c), getInt(x2,dy,c), xR);
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
     */
    default Double getInterpolatedWithCheck(double x, int xW, double y, int yW, int c) {
        if(x < 0 || x >= getWidth()-1) return null;
        if(y < 0 || y >= getHeight()-1) return null;
        if(c < 0 || c >= getDepth()) return null;
        return getInterpolated(x, xW, y, yW, c);
    }
    default double linearInterpolation(double a, double b, double ratio) {
		/*if(ratio != 0)
			System.out.println("");*/
        return a + (b-a)*ratio; // todo remove this duplicate
    }

    /**
     * Get the smallest and largest pixel value
     * @return an array containing the min and max values respectively
     */
    default int[] getMinMax() {
        int[][] temp = new int[2][threadCount];
        MPT.run(threadCount, 0, getSubpixelCount(), 1, (MTPListRunnable<Integer>) (procID, idx, val) -> {
            temp[0][procID] = Math.min(temp[0][procID], getInt(idx));
            temp[1][procID] = Math.max(temp[1][procID], getInt(idx));
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
     * @return the sums
     */
    default long[] sumChannelValues() {
        long[][] rawSum = new long[threadCount][getDepth()];

        MPT.run(threadCount, 0, getHeight(), 1, (procID, y, val) -> {
            for(int x=0; x<getWidth(); x++) {
                for(int c=0; c<getDepth(); c++) {
                    rawSum[procID][c] += getInt(x, y, c);
                }
            }
        });
        long[] out = new long[getDepth()];
        for(int i=0; i<threadCount; i++) {
            for(int j=0; j<getDepth(); j++) {
                out[j] += rawSum[i][j];
            }
        }
        return out;
    }

    /**
     * Calculates the sum of all sub pixels in the image.
     * @return the sum
     */
    default long sumAllValues() {
        long[] rawSum = sumChannelValues();
        long out = 0;
        for (long l : rawSum) out += l;
        return out;
    }
    default BufferedImage toBufferedImage() {
        int type = (getDepth() == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage out = new BufferedImage(getWidth(), getHeight(), type);

        MPT.run(threadCount, 0, getHeight(), 1, (procID, idx, val) -> {
            int y = idx;
            for(int x=0; x<getWidth(); x++) {
                Color col = getColor(x, y);
                out.setRGB(x, y, col.getRGB());
            }
        });
        return out;
    }
    default ImageRaster evaluate() {
        ImageRaster out = ImageRaster.create(getWidth(), getHeight(), getDepth());

        MPT.run(threadCount, 0, getHeight(), 1, (procID, y, val) -> {
            for(int x=0; x<getWidth(); x++) {
                for(int c=0; c<getDepth(); c++) {
                    int value = getInt(x, y, c);
                    out.setInt(x, y, c, value);
                }
            }
        });
        return out;
    }

    /**
     * Calculate the histograms for each channel in the image.
     * @return out[channels][0-255]
     */
    default int[][] getHistogram() {
        int[][][] tempOut = new int[threadCount][getDepth()][256];
        MPT.run(threadCount, 0, getHeight(), 1, (procID, y, val) -> {
            for(int x=0; x<getWidth(); x++) {
                for(int c=0; c<getDepth(); c++) {
                    tempOut[procID][c][getInt(x, y, c)]++;
                }
            }
        });
        int[][] out = new int[getDepth()][256];
        for(int i=0; i<getDepth(); i++) {
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
     * Save the Img into a file.
     * @param formatName a String containing the informal name of the format. Same as
     *                   in {@link ImageIO#write}.
     * @param f The file to save to.
     * @exception IllegalArgumentException if any parameter is
     * <code>null</code>.
     * @exception IOException if an error occurs during writing.
     */
    default void save(String formatName, File f) throws IOException {
        ImageIO.write(toBufferedImage(), formatName, f);
    }

    /////////////////////////////////////////////////////
    ////////////////        Utils        ////////////////
    /////////////////////////////////////////////////////
    // source: https://stackoverflow.com/questions/20266201/3d-array-1d-flat-indexing
    default int to1D(int x, int y, int z) {
        return (z * getWidth() * getHeight()) + (y * getWidth()) + x;
    }
    default int[] to3D(int idx) {
        final int z = idx / (getWidth() * getHeight());
        idx -= (z * getWidth() * getHeight());
        final int y = idx / getWidth();
        final int x = idx % getWidth();
        return new int[]{ x, y, z };
    }

    default int b(int i) {
        return Math.min(Math.max(i, 0), 255);
    }
}
