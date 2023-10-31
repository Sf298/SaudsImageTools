package sauds.image.tools;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

public class Operations {

    /////////////////////////////////////////////////
    //              Fancy Operations               //
    /////////////////////////////////////////////////
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
        int kernelW = kernel.getW();
        int kernelH = kernel.getH();
        int hkW = kernelW / 2;
        int hkH = kernelH / 2;

        Layer<?> layer = new Layer<>(Operation.class, "convolution", true, Triple.of(bh, aggSupplier, kernel));
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
                return new Operation(innerBounds, layer, newW, newH, image.getDepth()) {
                    @Override
                    public int applyOp(List<Image> images, int x, int y, int c) {
                        Image image = images.get(0);
                        x = (int) (x * hStep);
                        y = (int) (y * vStep);
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int j = 0; j < kernelH; j++) {
                            for (int i = 0; i < kernelW; i++) {
                                int imgValue = image.getInt(x+i-hkW, y+j-hkH, c);
                                aggregator.addValue(imgValue * kernel.get(i, j));
                            }
                        }
                        return aggregator.getResult();
                    }
                };
            default:
                return new Operation(image, layer, newW, newH, image.getDepth()) {
                    @Override
                    public int applyOp(List<Image> images, int x, int y, int c) {
                        Image image = images.get(0);
                        x = (int) (x * hStep);
                        y = (int) (y * vStep);
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int i = 0; i < kernelW; i++) {
                            for (int j = 0; j < kernelH; j++) {
                                Integer imgValue = image.getInt(x+i-hkW, y+j-hkH, c, bh);
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
     * Builds a laplacian pyramid. from the image.
     * @param levelCount The number of levels in the pyramid
     * @return The layers of the pyramid as a list, from the largest layer to the smallest layer
     */
    public ArrayList<Image> laplacianPyramid(Image image, int levelCount) {
        ArrayList<Image> out = new ArrayList<>();
        Image color = image;
        for(int i=0; i<levelCount; i++) {
            Image redu = downscale2x(color);
            Image upSc = upscale2x(redu);
            out.add(subtract(color, upSc));
            color = redu;
        }
        out.add(color);
        return out;
    }
    /**
     * Converts a laplacian pyramid back into an image.
     * @param pyramid The layers of the pyramid as a list, from the largest layer to the smallest layer
     * @return the converted image
     */
    public static Image laplacianPyramid(List<Image> pyramid) {
        Image out = pyramid.get(pyramid.size()-1);
        for(int i=pyramid.size()-2; i>=0; i--) {
            out = add(upscale2x(out), pyramid.get(i));
        }
        return out;
    }

    private static final Integer ZERO = 0;
	/**
	 * Erode image using the 4 adjacent cells. 'on' values are any number above 0, values are 
     * 'off' when they are 0.
	 * @return 
	 */
    public Image erode4(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "downscale2x", true, Pair.of(w, h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if(ZERO.equals(getInt(x, y-1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x-1, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x+1, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x, y+1, c, BorderHandling.IGNORE))) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
    }
	/**
	 * Dilate image using the 4 adjacent cells. 'on' values are any number above 0, values are 
     * 'off' when they are 0.
	 * @return 
	 */
    public Image erode8(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "downscale2x", true, Pair.of(w, h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if(ZERO.equals(getInt(x-1, y-1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x, y-1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x+1, y-1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x-1, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x+1, y, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x-1, y+1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x, y+1, c, BorderHandling.IGNORE))
                        || ZERO.equals(getInt(x+1, y+1, c, BorderHandling.IGNORE))) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
    }
	/**
	 * Erode image using the 4 adjacent cells and 4 diagonal cells. 'on' values are any number 
     * above 0, values are 'off' when they are 0.
	 * @return 
	 */
    public Image dilate4(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "downscale2x", true, Pair.of(w, h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if(compGT(getInt(x, y-1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x-1, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x+1, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x, y+1, c, BorderHandling.IGNORE))) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }
	/**
	 * Dilate image using the 4 adjacent cells and 4 diagonal cells. 'on' values are any number 
     * above 0, values are 'off' when they are 0.
	 * @return 
	 */
    public Image dilate8(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "downscale2x", true, Pair.of(w, h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if(compGT(getInt(x-1, y-1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x, y-1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x+1, y-1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x-1, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x+1, y, c, BorderHandling.IGNORE))
                        || compGT(getInt(x-1, y+1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x, y+1, c, BorderHandling.IGNORE))
                        || compGT(getInt(x+1, y+1, c, BorderHandling.IGNORE))) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }

    /**
     * Set the values of each pixel in the {@link Blob}s to the given value.
     * @param blobs the {@link Blob}s to set.
     * @param channel The channel in which to insert the {@link Blob}s
     * @param value The value to set the {@link Blob} to
     */
    public Image insertBlobs(Image image, ArrayList<Blob> blobs, int channel, int value) {
        for(Blob blob : blobs) {
            image = insertBlob(image, blob, channel, value);
        }
        return image;
    }
    /**
     * Set the values of each pixel in the {@link Blob} to the given value.
     * @param b the {@link Blob} to set
     * @param channel The channel in which to insert the {@link Blob}
     * @param value The value to set the {@link Blob} to
     */
    public Image insertBlob(Image image, Blob b, int channel, int value) {
        if(b.getMaxX() > image.getWidth() || b.getMaxY() > image.getHeight())
            throw new RuntimeException("Blob has pixels outside the range");

        Layer<Pair<Integer, Integer>> layer = new Layer<>(Operation.class, "insert blob", false, null);

        b.indexCoordinates();
        return new Operation(image, layer, image.getWidth(), image.getHeight(), image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if (c == channel && b.containsPoint(x,y)) {
                    return images.get(0).getInt(x, y, c);
                } else {
                    return value;
                }
            }
        };
    }
    /**
     * Scan the image for blobs (regions of non-zero values).
     * @param includeDiagonal if the search should scan diagonally.
     * @return A list of all blobs detected
     */
    public List<Blob> detectBlobs(Image image, boolean includeDiagonal) {
        ArrayList<Blob> blobs = new ArrayList<>();
        for(int c=0; c<image.getDepth(); c++) {
            ImageRaster mask = ImageRaster.create(image.getWidth(), image.getHeight(), 1);
            for(int y=0; y< image.getHeight(); y++) {
                for(int x=0; x< image.getWidth(); x++) {
                    if(mask.getInt(x,y,0) > 0) continue;
                    if(image.getInt(x, y, c) == 0) {
                        mask.setInt(x, y, 0, 1);
                    } else {
                        //start flood fill
                        Blob blob = new Blob();
                        Queue<int[]> q = new LinkedList<>();
                        q.add(new int[] {x, y});
                        mask.setInt(x,y,0, 1);
                        while(!q.isEmpty()) {
                            int[] currCoord = q.remove();
                            int cx = currCoord[0];
                            int cy = currCoord[1];
                            blob.addPoint(cx, cy);
                            int tx,ty;
                            tx = cx+1; ty = cy;
                            if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                q.add(new int[] {tx,ty});
                                mask.setInt(tx,ty,0,1);
                            }
                            tx = cx-1; ty = cy;
                            if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                q.add(new int[] {tx,ty});
                                mask.setInt(tx,ty,0,1);
                            }
                            tx = cx; ty = cy+1;
                            if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                q.add(new int[] {tx,ty});
                                mask.setInt(tx,ty,0,1);
                            }
                            tx = cx; ty = cy-1;
                            if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                q.add(new int[] {tx,ty});
                                mask.setInt(tx,ty,0,1);
                            }
                            if (includeDiagonal) {
                                tx = cx+1; ty = cy+1;
                                if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                    q.add(new int[] {tx,ty});
                                    mask.setInt(tx,ty,0,1);
                                }
                                tx = cx-1; ty = cy+1;
                                if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                    q.add(new int[] {tx,ty});
                                    mask.setInt(tx,ty,0,1);
                                }
                                tx = cx+1; ty = cy-1;
                                if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                    q.add(new int[] {tx,ty});
                                    mask.setInt(tx,ty,0,1);
                                }
                                tx = cx-1; ty = cy-1;
                                if(image.isInBounds(tx,ty,0) && image.getInt(tx,ty,0)==0 && image.getInt(tx,ty,c) > 0) {
                                    q.add(new int[] {tx,ty});
                                    mask.setInt(tx,ty,0,1);
                                }
                            }
                        }
                        blobs.add(blob);
                    }
                }
            }
        }
        return blobs;
    }


    /////////////////////////////////////////////////
    //             General Operations              //
    /////////////////////////////////////////////////
    /**
     * Halves the width and height of the {@link Image}. Optimised for this specific use case.
     * @param image The {@link Image} to resize.
     * @return A new {@link Image}.
     */
    public static Image downscale2x(Image image) {
        int w = image.getWidth() >> 1;
        int h = image.getHeight() >> 1;
        Layer<?> layer = new Layer<>(Operation.class, "downscale2x", true, Pair.of(w, h));
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                Image image = images.get(0);
                int xx = x << 1;
                int yy = y << 1;
                return (image.getInt(xx,yy,c) + image.getInt(xx+1,yy,c) + image.getInt(xx,yy+1,c) + image.getInt(xx+1,yy+1,c)) >> 2;
            }
        };
    }
    /**
     * Doubles the width and height of the {@link Image}. Optimised for this specific use case.
     * @param image The {@link Image} to resize.
     * @return A new {@link Image}.
     */
    public static Image upscale2x(Image image) {
        int w = image.getWidth() << 1;
        int h = image.getHeight() << 1;
        Layer<?> layer = new Layer<>(Operation.class, "upscale2x", false, Pair.of(w, h));
        return new Operation(image, layer, w, h, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                Image image = images.get(0);
                int xx = x >> 1;
                int yy = y >> 1;
                return image.getInt(xx,yy,c);
            }
        };
    }
    /**
     * Resize this image to the width and height of the given image.
     * @param shape The image whose width and height should be copied
     */
    public static Image resizeTo(Image imageToResize, Image shape) {
        return resize(imageToResize, shape.getWidth(), shape.getHeight());
    }
    /**
     * Resize this image to the given width/height.
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
     * Resize this image to the given width.
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
            return new Operation(image, layer, w, image.getHeight(), image.getDepth()) {
                @Override
                public int applyOp(List<Image> images, int x, int y, int c) {
                    double realX = x * scaleFactor;
                    int lowerX = (int)realX;
                    int upperX = (realX - lowerX < 0.00001) ? lowerX : lowerX+1;
                    int lowerXVal = images.get(0).getInt(lowerX, y, c);
                    int upperXVal = images.get(0).getInt(upperX, y, c);

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
     * Resize this image to the given height.
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
            return new Operation(image, layer, image.getWidth(), h, image.getDepth()) {
                @Override
                public int applyOp(List<Image> images, int x, int y, int c) {
                    double realY = y * scaleFactor;
                    int lowerY = (int)realY;
                    int upperY = (realY - lowerY < 0.00001) ? lowerY : lowerY+1;
                    int lowerXVal = images.get(0).getInt(x, lowerY, c);
                    int upperXVal = images.get(0).getInt(x, upperY, c);

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
     * Adjust the size of the image by a ratio.
     * @param w the ratio to adjust the width by
     * @param h the ratio to adjust the height by
     */
    public static Image rescale(Image image, double w, double h) {
        return resize(image, (int)(image.getWidth()*w), (int)(image.getHeight()*h));
    }
    private static double interpolate(double a, double b, double ratioFromA) {
        return (b-a) * ratioFromA + a; // todo remove this duplicate
    }
    /**
     * Crops an area of the image. This is the same as {@link ImageROI}.
     * @param image The image to crop
     * @param x The starting x coordinate
     * @param w The width of the cropped image
     * @param y The starting y coordinate
     * @param h The height of the cropped image
     * @param c The starting channel
     * @param d The depth of the cropped image
     * @return A cropped image
     */
    public static Image crop(Image image, int x, int w, int y, int h, int c, int d) {
        return new ImageROI(image, x, w, y, h, c, d);
    }
    /**
     * Adds padding around the image. The resulting image is
     * square and has a width/height that is big enough to fit the original
     * image but also a power of 2.
     * @return
     */
    public Image squareifyPow2(Image image) {
        int newWH = nextPow2(Math.max(image.getWidth(), image.getHeight()));
        int xPos = (newWH - image.getWidth()) / 2;
        int yPos = (newWH - image.getHeight()) / 2;
        int xPos2 = xPos + image.getWidth();
        int yPos2 = yPos + image.getHeight();

        Layer<Pair<Integer, Integer>> layer = new Layer<>(Operation.class, "squareifyPow2", false, null);
        return new Operation(image, layer, newWH, newWH, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if (xPos < x  && x < xPos2 && yPos < y  && y < yPos2) {
                    return images.get(1).getInt(x-xPos, y-yPos, c);
                } else {
                    return 0;
                }
            }
        };
    }


    /**
     * Rotate the image by a specified amount. Pads the corners to avoid cropping the Image.
     * @param rad the angle in radians to rotate by
     * @return the rotated {@link Image}
     */
    public static Image rotate(Image image, double rad) {
        double theta = normaliseRadians(rad);
        double[] p1 = rotateTransform(0, 0, rad);
        double[] p2 = rotateTransform(image.getWidth(), 0, rad);
        double[] p3 = rotateTransform(0, image.getHeight(), rad);
        double[] p4 = rotateTransform(image.getWidth(), image.getHeight(), rad);
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
            yShift = (int) -(p2[1]-p1[1]);
        }

        Layer<?> layer = new Layer<>(Operation.class, "rotate", true, rad);
        if (image.hasSlowLayer()) {
            image = image.evaluate();
        }

        return new Operation(image, layer, (int)(maxX-minX)-1, (int)(maxY-minY)-1, image.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
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
                public int applyOp(List<Image> images, int x, int y, int c) {
                    images.get(0).getInt(w-x, h-y, c);
                    return 0;
                }
            };
        } else if (!horizontal && vertical) {
            return new Operation(image, layer) {
                @Override
                public int applyOp(List<Image> images, int x, int y, int c) {
                    images.get(0).getInt(x, h-y, c);
                    return 0;
                }
            };
        } else if (horizontal) {
            return new Operation(image, layer) {
                @Override
                public int applyOp(List<Image> images, int x, int y, int c) {
                    images.get(0).getInt(w-x, y, c);
                    return 0;
                }
            };
        }
        return image;
    }

    /**
     * Converts the given {@link Image} into greyscale by averaging the values of all the channels into a single channel.
     * @param image The {@link Image} to convert
     * @return The greyscale {@link Image}
     */
    public static Image toGreyscale(Image image) {
        Layer<?> layer = new Layer<>(Operation.class, "greyscale", false, null);

        switch (image.getDepth()) {
            case 1:
                return image;
            case 2:
                return new Operation(image, layer, image.getWidth(), image.getHeight(), 1) {
                    @Override
                    public int applyOp(List<Image> images, int x, int y, int c) {
                        Image image = images.get(0);
                        return (image.getInt(x, y, 0) + image.getInt(x, y, 1)) >> 1;
                    }
                };
            case 3:
                return new Operation(image, layer, image.getWidth(), image.getHeight(), 1) {
                    @Override
                    public int applyOp(List<Image> images, int x, int y, int c) {
                        Image image = images.get(0);
                        return (image.getInt(x, y, 0) + image.getInt(x, y, 1) + image.getInt(x, y, 2)) / 3;
                    }
                };
            default:
                return new Operation(image, layer, image.getWidth(), image.getHeight(), 1) {
                    @Override
                    public int applyOp(List<Image> images, int x, int y, int c) {
                        Image image = images.get(0);
                        int sum = 0;
                        for (int i = 0; i < image.getDepth(); i++) {
                            sum += image.getInt(x, y, i);
                        }
                        return sum / image.getDepth();
                    }
                };
        }
    }


    /////////////////////////////////////////////////
    //             Subpixel Operations             //
    /////////////////////////////////////////////////
    public static SubpixelOperation add(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "add", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) + val;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) + val;
            }
        };
    }
    public static SubpixelOperation add(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "add", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) + image2.getInt(i);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) + image2.getInt(x,y,c);
            }
        };
    }

    public static SubpixelOperation subtract(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "subtract", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) - val;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) - val;
            }
        };
    }
    public static SubpixelOperation subtract(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "subtract", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) - image2.getInt(i);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) - image2.getInt(x,y,c);
            }
        };
    }

    public static SubpixelOperation multiply(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "multiply", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) * val;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) * val;
            }
        };
    }
    public static SubpixelOperation multiply(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "multiply", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) * image2.getInt(i);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) * image2.getInt(x,y,c);
            }
        };
    }

    public static SubpixelOperation divide(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) / val;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) / val;
            }
        };
    }
    public static SubpixelOperation divide(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) / image2.getInt(i);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) / image2.getInt(x,y,c);
            }
        };
    }

    public static SubpixelOperation abs(Image image) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "absolute", false, null);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return Math.abs(imageRead.getInt(i));
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return Math.abs(imageRead.getInt(x,y,c));
            }
        };
    }

    public static SubpixelOperation min(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return Math.min(imageRead.getInt(i), val);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return Math.min(imageRead.getInt(x,y,c), val);
            }
        };
    }
    public static SubpixelOperation min(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return Math.min(imageRead.getInt(i), image2.getInt(i));
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return Math.min(imageRead.getInt(x,y,c), image2.getInt(x,y,c));
            }
        };
    }

    public static SubpixelOperation max(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return Math.max(imageRead.getInt(i), val);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return Math.max(imageRead.getInt(x,y,c), val);
            }
        };
    }
    public static SubpixelOperation max(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return Math.max(imageRead.getInt(i), image2.getInt(i));
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return Math.max(imageRead.getInt(x,y,c), image2.getInt(x,y,c));
            }
        };
    }

    public static SubpixelOperation equalTo(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) == val ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) == val ? 1 : 0;
            }
        };
    }
    public static SubpixelOperation equalTo(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) == image2.getInt(i) ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) == image2.getInt(x,y,c) ? 1 : 0;
            }
        };
    }

    public static SubpixelOperation greaterThan(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) > val ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) > val ? 1 : 0;
            }
        };
    }
    public static SubpixelOperation greaterThan(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) > image2.getInt(i) ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) > image2.getInt(x,y,c) ? 1 : 0;
            }
        };
    }

    public static SubpixelOperation greaterThanEq(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) >= val ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) >= val ? 1 : 0;
            }
        };
    }
    public static SubpixelOperation greaterThanEq(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) >= image2.getInt(i) ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) >= image2.getInt(x,y,c) ? 1 : 0;
            }
        };
    }

    public static SubpixelOperation lessThan(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) < val ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) < val ? 1 : 0;
            }
        };
    }
    public static SubpixelOperation lessThan(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) < image2.getInt(i) ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) < image2.getInt(x,y,c) ? 1 : 0;
            }
        };
    }

    public static SubpixelOperation lessThanEq(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) <= val ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) <= val ? 1 : 0;
            }
        };
    }
    public static SubpixelOperation lessThanEq(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) <= image2.getInt(i) ? 1 : 0;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) <= image2.getInt(x,y,c) ? 1 : 0;
            }
        };
    }

    public static SubpixelOperation mod(Image image, int val) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) % val;
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) % val;
            }
        };
    }
    public static SubpixelOperation mod(Image image1, Image image2) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return imageRead.getInt(i) % image2.getInt(i);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return imageRead.getInt(x,y,c) % image2.getInt(x,y,c);
            }
        };
    }

    public static SubpixelOperation interpolate(Image image, int val, double ratioFromLeft) {
        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, ""+val);
        return new SubpixelOperation(image, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return (int) interpolate(imageRead.getInt(i), val, ratioFromLeft);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return (int) interpolate(imageRead.getInt(x,y,c), val, ratioFromLeft);
            }
        };
    }
    public static SubpixelOperation interpolate(Image image1, Image image2, double ratioFromLeft) {
        assertImagesAreSameShape(image1, image2);

        Layer<?> layer = new Layer<>(SubpixelOperation.class, "divide", false, "Img");
        return new SubpixelOperation(image1, layer) {
            @Override
            public int applyOp(Image imageRead, int i) {
                return (int) interpolate(imageRead.getInt(i), image2.getInt(i), ratioFromLeft);
            }

            @Override
            public int applyOp(Image imageRead, int x, int y, int c) {
                return (int) interpolate(imageRead.getInt(x,y,c), image2.getInt(x,y,c), ratioFromLeft);
            }
        };
    }

    /////////////////////////////////////////////////
    //          Image Joining Operations           //
    /////////////////////////////////////////////////
    public static Image concatHorizontally(List<Image> images) {
        Set<Integer> heights = images.stream().map(Image::getHeight).collect(Collectors.toSet());
        Set<Integer> depths = images.stream().map(Image::getDepth).collect(Collectors.toSet());

        if (heights.size() != 1 || depths.size() != 1) {
            throw new IllegalArgumentException("Heights and Depths of all input images must match");
        }

        int w = images.stream().mapToInt(Image::getWidth).sum();
        int h = heights.stream().findFirst().get();
        int d = depths.stream().findFirst().get();

        Layer<?> layer = new Layer<>(Operation.class, "concatenate-horizontally", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getWidth).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new Operation(images, layer, w, h, d) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                return images.get(imageIndexMap[x]).getInt(depthIndexMap[x], y, c);
            }
        };
    }

    public static Image concatVertically(List<Image> images) {
        Set<Integer> widths = images.stream().map(Image::getWidth).collect(Collectors.toSet());
        Set<Integer> depths = images.stream().map(Image::getDepth).collect(Collectors.toSet());

        if (widths.size() != 1 || depths.size() != 1) {
            throw new IllegalArgumentException("Widths and Depths of all input images must match");
        }

        int w = widths.stream().findFirst().get();
        int h = images.stream().mapToInt(Image::getHeight).sum();
        int d = depths.stream().findFirst().get();

        Layer<?> layer = new Layer<>(Operation.class, "concatenate-vertically", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getHeight).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new Operation(images, layer, w, h, d) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                return images.get(imageIndexMap[y]).getInt(x, depthIndexMap[y], c);
            }
        };
    }

    public static Image concatChannels(List<Image> images) {
        Set<Integer> widths = images.stream().map(Image::getWidth).collect(Collectors.toSet());
        Set<Integer> heights = images.stream().map(Image::getHeight).collect(Collectors.toSet());

        if (widths.size() != 1 || heights.size() != 1) {
            throw new IllegalArgumentException("Widths and Heights of all input images must match");
        }

        int w = widths.stream().findFirst().get();
        int h = heights.stream().findFirst().get();
        int d = images.stream().mapToInt(Image::getDepth).sum();

        Layer<?> layer = new Layer<>(Operation.class, "concatenate-channels", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getDepth).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new Operation(images, layer, w, h, d) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                return images.get(imageIndexMap[c]).getInt(x, y, depthIndexMap[c]);
            }
        };
    }

    public static Image overlay(Image base, Image overlay, int xPos, int yPos) {
        int xPos2 = xPos + overlay.getWidth();
        int yPos2 = yPos + overlay.getHeight();

        if(xPos2 > base.getWidth() || yPos2 > base.getHeight())
            throw new RuntimeException("Overlayed image too large to fit");

        Layer<Pair<Integer, Integer>> layer = new Layer<>(Operation.class, "overlay", false, Pair.of(xPos, yPos));

        return new Operation(asList(base, overlay), layer, base.getWidth(), base.getHeight(), base.getDepth()) {
            @Override
            public int applyOp(List<Image> images, int x, int y, int c) {
                if (xPos < x  && x < xPos2 && yPos < y  && y < yPos2) {
                    return images.get(1).getInt(x-xPos, y-yPos, c);
                } else {
                    return images.get(0).getInt(x, y, c);
                }
            }
        };
    }

    private static int[][] calculateIndexMappings(List<Integer> sizes) {
        int[] imageIndexMap = new int[sizes.stream().mapToInt(i->i).sum()];
        int[] depthIndexMap = new int[imageIndexMap.length];
        int k = 0;
        for (int i = 0; i < sizes.size(); i++) {
            for (int j = 0; j < sizes.get(i); j++, k++) {
                imageIndexMap[k] = i;
                depthIndexMap[k] = j;
            }
        }
        return new int[][] {imageIndexMap, depthIndexMap};
    }


    /////////////////////////////////////////////////
    //               Utility Methods               //
    /////////////////////////////////////////////////
    /**
     * Calculates the next power of 2 greater than the given number.
     * @param i
     * @return
     */
    private static int nextPow2(int i) {
        if(i < 0) return -1;
        int out = -2147483648;
        while((i&out)==0)
            out = out >>> 1;
        return (((out-1)&i) != 0) ? out<<1 : out;
    }

    private boolean compGT(Integer i) {
        return i!=null && i>0;
    }

    private static void assertImagesAreSameShape(Image image1, Image image2) {
        if (image1.getWidth() != image2.getWidth()) {
            throw new IllegalArgumentException("Unable to subtract images. Widths are different sizes.");
        }
        if (image1.getHeight() != image2.getHeight()) {
            throw new IllegalArgumentException("Unable to subtract images. Heights are different sizes.");
        }
        if (image1.getDepth() != image2.getDepth()) {
            throw new IllegalArgumentException("Unable to subtract images. Depths are different sizes.");
        }
    }

}
