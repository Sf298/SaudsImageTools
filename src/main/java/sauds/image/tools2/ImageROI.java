package sauds.image.tools2;

import java.util.List;

import static java.util.Arrays.asList;

public class ImageROI implements WriteableImage {

    private final Image image;
    private final int x,y,c, w,h,d;

    /**
     * Creates a new region of interest in an Img.
     * @param image the Img to crop
     * @param x the starting x coord (inclusive)
     * @param w the width of the ROI, if -1 it is set to the width of the Img
     * @param y the starting y coord (inclusive)
     * @param h the height of the ROI, if -1 it is set to the height of the Img
     * @param c the starting c coord (inclusive)
     * @param d the depth of the ROI, if -1 it is set to the depth of the Img
     */
    public ImageROI(Image image, int x, int w, int y, int h, int c, int d) {
        this.image = image;
        if(w == -1) w = image.getWidth() - x;
        if(h == -1) h = image.getHeight() - y;
        if(d == -1) d = image.getDepth() - c;
        this.x = x; this.w = w;
        this.y = y; this.h = h;
        this.c = c; this.d = d;
    }

    @Override
    public int getWidth() {
        return this.w;
    }
    @Override
    public int getHeight() {
        return this.h;
    }
    @Override
    public int getDepth() {
        return this.d;
    }

    @Override
    public List<Layer<?>> getLayers() {
        List<Layer<?>> layers = image.getLayers();
        layers.add(new Layer<>(getClass(), "ROI", false, asList(x,y,c, w,h,d)));
        return layers;
    }

    @Override
    public int getInt(int x, int y, int c) {
        return image.getInt(x+this.x, y+this.y, c+this.c);
    }

    @Override
    public void setInt(int x, int y, int c, int val) {
        ((WriteableImage) image).setInt(x+this.x, y+this.y, c+this.c, val);
    }

}
