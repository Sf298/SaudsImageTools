package sauds.image.tools2;

import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

public abstract class ImgOp implements IImgRead {

    protected final IImgRead img;
    protected final Layer<?> layer;

    protected ImgOp(IImgRead img, Layer<?> layer) {
        this.img = img;
        this.layer = layer;
    }

    public abstract int applyOp(IImgRead imgRead, int x, int y, int c);

    public static ImgOp convolve(IImgRead img, BorderHandling bh, Function<Kernel, Aggregator> aggSupplier, Kernel kernel) {
        if (!kernel.isSeparable()) {
            return convolveGeneral(img, bh, aggSupplier, kernel);
        }

        ImgOp opResult = convolveGeneral(img, bh, aggSupplier, kernel.getHComponent());
        return convolveGeneral(opResult, bh, aggSupplier, kernel.getVComponent());
    }
    private static ImgOp convolveGeneral(IImgRead img, BorderHandling bh, Function<Kernel, Aggregator> aggSupplier, Kernel kernel) {
        Layer<?> layer = new Layer<>(ImgOp.class, "Convolution", true, Triple.of(bh, aggSupplier, kernel));
        int kernelW = kernel.getW();
        int kernelH = kernel.getH();
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
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int j = 0; j < kernelH; j++) {
                            for (int i = 0; i < kernelW; i++) {
                                int imgValue = imgRead.getInt(x+i-hkW, y+j-hkH, c);
                                aggregator.addValue(imgValue * kernel.get(i, j));
                            }
                        }
                        return aggregator.getResult();
                    }
                };
            default:
                return new ImgOp(img, layer) {
                    @Override
                    public int applyOp(IImgRead imgRead, int x, int y, int c) {
                        Aggregator aggregator = aggSupplier.apply(kernel);
                        for (int i = 0; i < kernelW; i++) {
                            for (int j = 0; j < kernelH; j++) {
                                Integer imgValue = imgRead.getInt(x+i-hkW, y+j-hkH, c, bh);
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

    @Override
    public int getWidth() {
        return img.getWidth();
    }
    @Override
    public int getHeight() {
        return img.getHeight();
    }
    @Override
    public int getDepth() {
        return img.getDepth();
    }

    @Override
    public List<Layer<?>> getLayers() {
        List<Layer<?>> layers = img.getLayers();
        layers.add(layer);
        return layers;
    }

    @Override
    public int getInt(int x, int y, int c) {
        return applyOp(img, x,y,c);
    }

}
