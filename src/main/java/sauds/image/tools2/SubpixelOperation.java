package sauds.image.tools2;

public abstract class SubpixelOperation extends Operation {

    protected SubpixelOperation(Image image, Layer<?> layer) {
        super(image, layer);
    }

    public abstract int applyOp(Image imageRead, int i);

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

    public static SubpixelOperation sub(Image image, int val) {
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
    public static SubpixelOperation sub(Image image1, Image image2) {
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

    public static SubpixelOperation mult(Image image, int val) {
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
    public static SubpixelOperation mult(Image image1, Image image2) {
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

    public static SubpixelOperation div(Image image, int val) {
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
    public static SubpixelOperation div(Image image1, Image image2) {
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

    @Override
    public int getInt(int i) {
        return applyOp(image, i);
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
