package sauds.image.tools2;

import sauds.toolbox.multiprocessing.tools.MPT;
import sauds.toolbox.multiprocessing.tools.MTPListRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Img implements IImgWriteable {

    int width, height, depth;
    private byte[] values;
    public byte[] getValues() {
        return values;
    }


    /////////////////////////////////////////////////////
    ////////////////     Constructors    ////////////////
    /////////////////////////////////////////////////////
    public static Img create(int width, int height, int channels) {
        Img out = new Img();
        out.width = width;
        out.height = height;
        out.depth = channels;
        out.values = new byte[width*height*channels];
        return out;
    }
    public static Img create(int width, int height, int channels, byte... values) {
        Img out = new Img();
        out.width = width;
        out.height = height;
        out.depth = channels;
        out.values = values;
        return out;
    }
    public static Img create(BufferedImage img) {
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
        Img out = create(width, height, channels);

        MPT.run(threadCount, 0, height, 1, new MTPListRunnable() {
            @Override
            public void iter(int procID, int idx, Object val) {
                int y = idx;
                for(int x=0; x<width; x++) {
                    Color col = new Color(img.getRGB(x, y));
                    if(1 <= channels) out.setInt(x,y,0, col.getRed());
                    if(2 <= channels) out.setInt(x,y,1, col.getGreen());
                    if(3 <= channels) out.setInt(x,y,2, col.getBlue());
                    if(4 <= channels) out.setInt(x,y,3, col.getAlpha());
                }
            }
        });
        return out;
    }
    public static Img create(File f) throws IOException {
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
