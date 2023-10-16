package sauds.image.tools2;

import java.util.List;

public abstract class SubpixelOperation extends Operation {

    protected SubpixelOperation(Image image, Layer<?> layer) {
        super(image, layer);
    }

    /**
     * A function to calculate a pixel at this layer.
     * @param image The wrapped image.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param c The channel.
     * @return The pixel at the specified coordinate.
     */
    public abstract int applyOp(Image image, int x, int y, int c);
    /**
     * A function to calculate a pixel at this layer.
     * @param image The wrapped image.
     * @param i The index in the data array.
     * @return The pixel at the specified coordinate.
     */
    public abstract int applyOp(Image image, int i);

    @Override
    public int getInt(int i) {
        return applyOp(images.get(0), i);
    }

    @Override
    public int getInt(int x, int y, int c) {
        return applyOp(images.get(0), x, y, c);
    }

    @Override
    public int applyOp(List<Image> images, int x, int y, int c) {
        return applyOp(images.get(0), x, y, c);
    }

}
