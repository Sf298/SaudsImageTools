package sauds.image.tools;

import org.apache.commons.lang3.tuple.Triple;
import sauds.toolbox.multiprocessing.tools.MPT;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ImageRaster implements WriteableImage {

    int width, height, depth;
    private byte[] values;
    public byte[] getValues() {
        return values;
    }
    public int[] getValuesInt() {
        int[] out = new int[values.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = getInt(i);
        }
        return out;
    }


    /////////////////////////////////////////////////////
    ////////////////     Constructors    ////////////////
    /////////////////////////////////////////////////////
    public static ImageRaster create(int width, int height, int channels) {
        ImageRaster out = new ImageRaster();
        out.width = width;
        out.height = height;
        out.depth = channels;
        out.values = new byte[width*height*channels];
        return out;
    }
    public static ImageRaster create(int width, int height, int channels, byte... values) {
        ImageRaster out = new ImageRaster();
        out.width = width;
        out.height = height;
        out.depth = channels;
        out.values = values;
        return out;
    }
    public static ImageRaster create(int width, int height, int channels, Function<Triple<Integer, Integer, Integer>, Byte> mapper) {
        ImageRaster out = new ImageRaster();
        out.width = width;
        out.height = height;
        out.depth = channels;
        out.values = new byte[width * height * channels];

        int x = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < channels; k++) {
                    out.values[x] = mapper.apply(Triple.of(i,j,k));
                    x++;
                }
            }
        }
        return out;
    }
    public static ImageRaster create(BufferedImage img) {
        if(img == null)
            throw new NullPointerException("Could not read image, img may be null");

        int width = img.getWidth();
        int height = img.getHeight();
        int channels;
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
        ImageRaster out = create(width, height, channels);

        MPT.run(threadCount, 0, height, 1, (procID, idx, val) -> {
            int y = idx;
            for(int x=0; x<width; x++) {
                Color col = new Color(img.getRGB(x, y));
                if(1 <= channels) out.setInt(x,y,0, col.getRed());
                if(2 <= channels) out.setInt(x,y,1, col.getGreen());
                if(3 <= channels) out.setInt(x,y,2, col.getBlue());
                if(4 <= channels) out.setInt(x,y,3, col.getAlpha());
            }
        });
        return out;
    }
    public static ImageRaster create(File f) throws IOException {
        return create(ImageIO.read(f));
    }


    /////////////////////////////////////////////////////
    //////////////// Getters and Setters ////////////////
    /////////////////////////////////////////////////////
    @Override
    public int getWidth() {
        return width;
    }
    @Override
    public int getHeight() {
        return height;
    }
    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public List<Layer<?>> getLayers() {
        List<Layer<?>> layers = new ArrayList<>();
        layers.add(new Layer<>(getClass(), null, false, null));
        return layers;
    }

    private byte getVal(int i) {
        return values[i];
    }
    private void setVal(int i, byte val) {
        values[i] = val;
    }

    @Override
    public int getInt(int i) {
        return getVal(i) & 0xFF;
    }
    @Override
    public void setInt(int i, int val) {
        setVal(i, (byte) val);
    }




}
