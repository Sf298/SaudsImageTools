package sauds.image.tools2;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * A type of readable {@link Image} that can combine information from more than 1 parent pixel.
 */
public abstract class Operation implements Image {

    protected final Image image;
    protected final Layer<?> layer;
    protected final int width;
    protected final int height;
    protected final int depth;

    protected Operation(Image image, Layer<?> layer) {
        this(image, image.getWidth(), image.getHeight(), image.getDepth(), layer);
    }
    protected Operation(Image image, int width, int height, int depth, Layer<?> layer) {
        this.image = image;
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * A function to calculate a pixel at this layer.
     * @param imageRead The wrapped image.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param c The channel.
     * @return The pixel at the specified coordinate.
     */
    abstract int applyOp(Image imageRead, int x, int y, int c);

    /**
     * Convolve this image with a kernel.
     * @param image the image to operate on
     * @param bh a {@link BorderHandling} to indicate how to handle edges
     * @param aggSupplier the {@link Aggregator} to use.
     * @param kernel the {@link Kernel}
     * @param hStep the horizontal stride
     * @param vStep the vertical stride
     */
    public static Image convolve(Image image, BorderHandling bh, Kernel kernel, Function<Kernel, Aggregator> aggSupplier, double hStep, double vStep) {
        if (!kernel.isSeparable()) {
            return convolveGeneral(image, bh, kernel, aggSupplier, hStep, vStep);
        }

        Image opResult = convolveGeneral(image, bh, kernel.getHComponent(), aggSupplier, 1, 1);
        return convolveGeneral(opResult, bh, kernel.getVComponent(), aggSupplier, 1, 1);
    }
    private static Image convolveGeneral(Image image, BorderHandling bh, Kernel kernel, Function<Kernel, Aggregator> aggSupplier, double hStep, double vStep) {
        Layer<?> layer = new Layer<>(Operation.class, "convolution", true, Triple.of(bh, aggSupplier, kernel));
        int kernelW = kernel.getW();
        int kernelH = kernel.getH();
        int hkW = kernelW / 2;
        int hkH = kernelH / 2;

        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        int newW = (int) (image.getWidth() / hStep);
        int newH = (int) (image.getHeight() / vStep);

        switch (bh) {
            case INNER:
                ImageROI innerBounds = new ImageROI(image,
                        (kernelW-1)/2, newW - (kernelW-1),
                        (kernelH-1)/2, newH - (kernelH-1),
                        0, image.getDepth()
                );
                return new Operation(innerBounds, newW, newH, image.getDepth(), layer) {
                    @Override
                    public int applyOp(Image imageRead, int x, int y, int c) {
                        x = (int) (x * hStep);
                        y = (int) (y * vStep);
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int j = 0; j < kernelH; j++) {
                            for (int i = 0; i < kernelW; i++) {
                                int imgValue = imageRead.getInt(x+i-hkW, y+j-hkH, c);
                                aggregator.addValue(imgValue * kernel.get(i, j));
                            }
                        }
                        return aggregator.getResult();
                    }
                };
            default:
                return new Operation(image, newW, newH, image.getDepth(), layer) {
                    @Override
                    public int applyOp(Image imageRead, int x, int y, int c) {
                        x = (int) (x * hStep);
                        y = (int) (y * vStep);
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int i = 0; i < kernelW; i++) {
                            for (int j = 0; j < kernelH; j++) {
                                Integer imgValue = imageRead.getInt(x+i-hkW, y+j-hkH, c, bh);
                                if (nonNull(imgValue)) {
                                    aggregator.addValue(imgValue * kernel.get(i, j));
                                }
                            }
                        }
                        return aggregator.getResult();
                    }
                };
        }
    }
    /*private static ImgOp convolveGeneral(IImgRead img, BorderHandling bh, Supplier<Aggregator> aggSupplier, IImgRead kernel) {
        Layer<?> layer = new Layer<>(ImgOp.class, "Convolution", true, Triple.of(bh, aggSupplier, kernel));
        int kernelW = kernel.getWidth();
        int kernelH = kernel.getHeight();
        int hkW = kernelW / 2;
        int hkH = kernelH / 2;

        if (img.hasSlowLayer()) {
            img = img.evaluate();
        }

        switch (bh) {
            case INNER:
                ImgROI innerBounds = new ImgROI(img,
                        (kernelW-1)/2, img.getWidth() - (kernelW-1),
                        (kernelH-1)/2, img.getHeight() - (kernelH-1),
                        0, img.getDepth()
                );

                return new ImgOp(innerBounds, layer) {
                    @Override
                    public int applyOp(IImgRead imgRead, int x, int y, int c) {
                        Aggregator aggregator = aggSupplier.get();
                        for (int i = 0; i < kernelW; i++) {
                            for (int j = 0; j < kernelH; j++) {
                                int imgValue = imgRead.getInt(x+i-hkW, y+j-hkH, c);
                                aggregator.addValue(imgValue * kernel.getInt(i, j,0));
                            }
                        }
                        return aggregator.getResult();
                    }
                };
            default:
                return new ImgOp(img, layer) {
                    @Override
                    public int applyOp(IImgRead imgRead, int x, int y, int c) {
                        Aggregator aggregator = aggSupplier.get();
                        for (int i = 0; i < kernelW; i++) {
                            for (int j = 0; j < kernelH; j++) {
                                Integer imgValue = imgRead.getInt(x+i-hkW, y+j-hkH, c, bh);
                                if (nonNull(imgValue)) {
                                    aggregator.addValue(imgValue * kernel.getInt(i, j, 0));
                                }
                            }
                        }
                        return aggregator.getResult();
                    }
                };
        }
    }*/

    /**
     * Halves the width and height of the {@link Image}. Optimised for this specific use case.
     * @param image The {@link Image} to resize.
     * @return A new {@link Image}.
     */
    public static Image halve(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "halve", true, Pair.of(w, h));
        return new Operation(image, w, h, image.getDepth(), layer) {
            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                int xx = x << 1;
                int yy = y << 1;
                return (imageRead.getInt(xx,yy,c) + imageRead.getInt(xx+1,yy,c) + imageRead.getInt(xx,yy+1,c) + imageRead.getInt(xx+1,yy+1,c)) >> 2;
            }
        };
    }


    /**
     * Resize this image to the given width/height
     * @param w the new width, if negative flips the Img, if null maintains aspect ratio
     * @param h the new height, if negative flips the Img, if null maintains aspect ratio
     */
    public static Image resize(Image image, Integer w, Integer h) {
        if(w==null && h==null) {
            throw new NullPointerException("Both inputs are null");
        } else if (w==null) {
            w = (h * image.getWidth())/ image.getHeight();
            if (h < 0) w *= -1; // correct the negative value
        } else if (h==null) {
            h = (w * image.getHeight())/ image.getWidth();
            if (w < 0) h *= -1; // correct the negative value
        }

        image = reflect(image, w<0, h<0);
        w = Math.abs(w);
        h = Math.abs(h);

        if (w * image.getHeight() < image.getWidth() * h) {
            Image a = resizeHorizontal(image, w);
            return resizeVertical(a, h);
        } else {
            Image a = resizeVertical(image, h);
            return resizeHorizontal(a, w);
        }
    }
    /**
     * Resize this image to the given width
     * @param w the new width
     */
    public static Image resizeHorizontal(Image image, Integer w) {
        if (w == image.getWidth()) {
            return image;
        }

        Layer<?> layer = new Layer<>(Operation.class, "resize-width", true, Pair.of(w, image.getHeight()));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }
        double scaleFactor = (image.getWidth()-1) / (w-1.0);

        if (w > image.getWidth()) {
            return new Operation(image, w, image.getHeight(), image.getDepth(), layer) {
                @Override
                public int applyOp(Image imageRead, int x, int y, int c) {
                    double realX = x * scaleFactor;
                    int lowerX = (int)realX;
                    int upperX = (realX - lowerX < 0.00001) ? lowerX : lowerX+1;
                    int lowerXVal = this.image.getInt(lowerX, y, c);
                    int upperXVal = this.image.getInt(upperX, y, c);

                    double lerp1 = interpolate(lowerXVal, upperXVal, realX - lowerX);
                    return (int) Math.round(lerp1);
                }
            };
        } else {
            double step = (double) image.getWidth() / w;
            return convolve(image, BorderHandling.IGNORE, Kernel.boxBlurH((int)step/2), Aggregator.MEAN, step, 1);
        }
    }
    /**
     * Resize this image to the given height
     * @param h the new height
     */
    public static Image resizeVertical(Image image, Integer h) {
        if (h == image.getHeight()) {
            return image;
        }

        Layer<?> layer = new Layer<>(Operation.class, "resize-height", true, Pair.of(image.getWidth(), h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }
        double scaleFactor = (image.getHeight()-1) / (h-1.0);

        if (h > image.getHeight()) {
            return new Operation(image, image.getWidth(), h, image.getDepth(), layer) {
                @Override
                public int applyOp(Image imageRead, int x, int y, int c) {
                    double realY = y * scaleFactor;
                    int lowerY = (int)realY;
                    int upperY = (realY - lowerY < 0.00001) ? lowerY : lowerY+1;
                    int lowerXVal = this.image.getInt(x, lowerY, c);
                    int upperXVal = this.image.getInt(x, upperY, c);

                    double lerp1 = interpolate(lowerXVal, upperXVal, realY - lowerY);
                    return (int) Math.round(lerp1);
                }
            };
        } else {
            double step = (double) image.getHeight() / h;
            return convolve(image, BorderHandling.IGNORE, Kernel.boxBlurV((int)step/2), Aggregator.MEAN, 1, step);
        }
    }
    /**
     * Adjust the size of the image by a ratio
     * @param w the ratio to adjust the width by
     * @param h the ratio to adjust the height by
     */
    public static Image rescale(Image image, double w, double h) {
        return resize(image, (int)(image.getWidth()*w), (int)(image.getHeight()*h));
    }
    private static double interpolate(double a, double b, double ratioFromA) {
        return (b-a) * ratioFromA + a;
    }

    /**
     * Rotate the image by a specified amount. Pads the corners to avoid cropping the Image.
     * @param rad the angle in radians to rotate by
     * @return the rotated {@link Image}
     */
    public Image rotate(Image image, double rad) {
        double theta = normaliseRadians(rad);
        double[] p1 = rotateTransform(0, 0, rad);
        double[] p2 = rotateTransform(width, 0, rad);
        double[] p3 = rotateTransform(0, height, rad);
        double[] p4 = rotateTransform(width, height, rad);
        double minX = Math.min(Math.min(p1[0], p2[0]), Math.min(p4[0], p3[0]));
        double maxX = Math.max(Math.max(p1[0], p2[0]), Math.max(p4[0], p3[0]));
        double minY = Math.min(Math.min(p1[1], p2[1]), Math.min(p4[1], p3[1]));
        double maxY = Math.max(Math.max(p1[1], p2[1]), Math.max(p4[1], p3[1]));

        // re-adjust position to move into box
        int xShift, yShift;
        if(theta < Math.PI/2) {
            xShift = (int) (p2[0]-(maxX-minX));
            yShift = 0;
        } else if(theta < Math.PI) {
            xShift = (int) -(maxX-minX);
            yShift = (int) (p3[1]-p1[1]);
        } else if(theta < Math.PI*3/2) {
            xShift = (int) (p2[0]-p1[0]);
            yShift = (int) -(maxY-minY);
        } else {
            xShift = 0;
            yShift = (int) -(p2[1]-p1[1]);;
        }

        Layer<?> layer = new Layer<>(Operation.class, "rotate", true, rad);

        return new Operation(image, (int)(maxX-minX)-1, (int)(maxY-minY)-1, image.getDepth(), layer) {
            @Override
            int applyOp(Image imageRead, int x, int y, int c) {
                double[] oldCoords = rotateTransform(x+xShift, y+yShift, theta);
                double oldX = oldCoords[0];
                double oldY = oldCoords[1];

                Double temp = getInterpolatedWithCheck(oldX, 0, oldY, 0, c);
                if(temp == null) {
                    return c==3 ? -1/*(255)*/ : 0;
                } else {
                    return (int) Math.round(temp);
                }
            }
        };
    }
    private static double normaliseRadians(double rad) {
        rad = rad % (2*Math.PI);
        return (rad<0) ? Math.PI*2+rad : rad;
    }
    private static double[] rotateTransform(double x, double y, double rad) {
        return new double[] {
                x*Math.cos(rad) + y*Math.sin(rad),
                -x*Math.sin(rad) + y*Math.cos(rad)
        };
    }

    /**
     * Reflect/flip the image in the specified axis.
     * @param horizontal whether the image should be flipped in the x-axis.
     * @param vertical whether the image should be flipped in the y-axis.
     * @return the rotated {@link Image}
     */
    public static Image reflect(Image image, boolean horizontal, boolean vertical) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "reflect", false, Pair.of(horizontal, vertical));
        int w = image.getWidth();
        int h = image.getHeight();
        if (horizontal && vertical) {
            return new Operation(image, layer) {
                @Override
                int applyOp(Image imageRead, int x, int y, int c) {
                    imageRead.getInt(w-x, h-y, c);
                    return 0;
                }
            };
        } else if (!horizontal && vertical) {
            return new Operation(image, layer) {
                @Override
                int applyOp(Image imageRead, int x, int y, int c) {
                    imageRead.getInt(x, h-y, c);
                    return 0;
                }
            };
        } else if (horizontal) {
            return new Operation(image, layer) {
                @Override
                int applyOp(Image imageRead, int x, int y, int c) {
                    imageRead.getInt(w-x, y, c);
                    return 0;
                }
            };
        }
        return image;
    }

    public static Image toGreyscale(Image image) {
        Layer<?> layer = new Layer<>(Operation.class, "greyscale", false, null);

        switch (image.getDepth()) {
            case 1:
                return image;
            case 2:
                return new Operation(image, image.getWidth(), image.getHeight(), 1, layer) {
                    @Override
                    public int applyOp(Image imageRead, int x, int y, int c) {
                        return (imageRead.getInt(x, y, 0) + imageRead.getInt(x, y, 1)) >> 1;
                    }
                };
            case 3:
                return new Operation(image, image.getWidth(), image.getHeight(), 1, layer) {
                    @Override
                    public int applyOp(Image imageRead, int x, int y, int c) {
                        return (imageRead.getInt(x, y, 0) + imageRead.getInt(x, y, 1) + imageRead.getInt(x, y, 2)) / 3;
                    }
                };
            default:
                return new Operation(image, image.getWidth(), image.getHeight(), 1, layer) {
                    @Override
                    public int applyOp(Image imageRead, int x, int y, int c) {
                        int sum = 0;
                        for (int i = 0; i < imageRead.getDepth(); i++) {
                            sum += imageRead.getInt(x, y, i);
                        }
                        return sum / imageRead.getDepth();
                    }
                };
        }
    }

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
        List<Layer<?>> layers = image.getLayers();
        layers.add(layer);
        return layers;
    }

    @Override
    public int getInt(int x, int y, int c) {
        return applyOp(image, x,y,c);
    }

}
