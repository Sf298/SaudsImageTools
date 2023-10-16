package sauds.image.tools2;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A type of readable {@link Image} that can combine information from more than 1 parent pixel.
 */
public abstract class Operation implements Image {

    protected final List<Image> images;
    protected final Layer<?> layer;
    protected final int width;
    protected final int height;
    protected final int depth;

    protected Operation(Image image, Layer<?> layer) {
        this(image, layer, image.getWidth(), image.getHeight(), image.getDepth());
    }
    protected Operation(Image image, Layer<?> layer, int width, int height, int depth) {
        this(Collections.singletonList(image), layer, width, height, depth);
    }
    protected Operation(List<Image> images, Layer<?> layer, int width, int height, int depth) {
        this.images = images;
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * A function to calculate a pixel at this layer.
     * @param images The wrapped images.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param c The channel.
     * @return The pixel at the specified coordinate.
     */
    public abstract int applyOp(List<Image> images, int x, int y, int c);

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
        List<Layer<?>> layers = images.stream().flatMap(i->i.getLayers().stream()).collect(Collectors.toList());
        layers.add(layer);
        return layers;
    }

    @Override
    public int getInt(int x, int y, int c) {
        return applyOp(images, x,y,c);
    }

}
