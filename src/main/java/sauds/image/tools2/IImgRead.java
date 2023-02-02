package sauds.image.tools2;

import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;

public interface IImgRead {


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
     * Get the list of layers in this stack
     * @return the list of layers, with the base first
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
        return null;
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

        MPT.run(threadCount, 0, getHeight(), 1, new MTPListRunnable() {
            @Override
            public void iter(int procID, int y, Object val) {
                for(int x=0; x<getWidth(); x++) {
                    for(int c=0; c<getDepth(); c++) {
                        rawSum[procID][c] += getInt(x, y, c);
                    }
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
        for(int i=0; i<rawSum.length; i++)
            out += rawSum[i];
        return out;
    }
    default BufferedImage toBufferedImage() {
        int type = (getDepth() == 3) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage out = new BufferedImage(getWidth(), getHeight(), type);

        MPT.run(threadCount, 0, getHeight(), 1, new MTPListRunnable() {
            @Override
            public void iter(int procID, int idx, Object val) {
                int y = idx;
                for(int x=0; x<getWidth(); x++) {
                    Color col = getColor(x, y);
                    out.setRGB(x, y, col.getRGB());
                }
            }
        });
        return out;
    }
    default Img evaluate() {
        Img out = Img.create(getWidth(), getHeight(), getDepth());

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

    /////////////////////////////////////////////////////
    ////////////////        Utils        ////////////////
    /////////////////////////////////////////////////////
    // source: https://stackoverflow.com/questions/20266201/3d-array-1d-flat-indexing
    default int to1D(int x, int y, int z) {
        //return (y * getDepth() * getWidth()) + (x * getDepth()) + z;
        return (x * getDepth() * getHeight()) + (y * getDepth()) + z;
    }
    default int[] to3D(int idx) {
        final int x = idx / (getDepth() * getHeight());
        idx -= (x * getDepth() * getHeight());
        final int y = idx / getDepth();
        final int z = idx % getDepth();
        return new int[]{ x, y, z };
    }

    default int b(int i) {
        return Math.min(Math.max(i, 0), 255);
    }
}
