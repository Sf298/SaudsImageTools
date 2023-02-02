package sauds.image.tools2;

public abstract class ImgSubpixelOp extends ImgOp {

    protected ImgSubpixelOp(IImgRead img, Layer<?> layer) {
        super(img, layer);
    }

    public abstract int applyOp(IImgRead imgRead, int i);

    public static ImgSubpixelOp add(IImgRead img, int val) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "add", false, ""+val);
        return new ImgSubpixelOp(img, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) + val;
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) + val;
            }
        };
    }
    public static ImgSubpixelOp add(IImgRead img1, IImgRead img2) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "add", false, "Img");
        return new ImgSubpixelOp(img1, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) + img2.getInt(i);
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) + img2.getInt(x,y,c);
            }
        };
    }

    public static ImgSubpixelOp sub(IImgRead img, int val) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "subtract", false, ""+val);
        return new ImgSubpixelOp(img, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) - val;
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) - val;
            }
        };
    }
    public static ImgSubpixelOp sub(IImgRead img1, IImgRead img2) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "subtract", false, "Img");
        return new ImgSubpixelOp(img1, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) - img2.getInt(i);
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) - img2.getInt(x,y,c);
            }
        };
    }

    public static ImgSubpixelOp mult(IImgRead img, int val) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "multiply", false, ""+val);
        return new ImgSubpixelOp(img, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) * val;
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) * val;
            }
        };
    }
    public static ImgSubpixelOp mult(IImgRead img1, IImgRead img2) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "multiply", false, "Img");
        return new ImgSubpixelOp(img1, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) * img2.getInt(i);
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) * img2.getInt(x,y,c);
            }
        };
    }

    public static ImgSubpixelOp div(IImgRead img, int val) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "divide", false, ""+val);
        return new ImgSubpixelOp(img, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) / val;
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) / val;
            }
        };
    }
    public static ImgSubpixelOp div(IImgRead img1, IImgRead img2) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "divide", false, "Img");
        return new ImgSubpixelOp(img1, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return imgRead.getInt(i) / img2.getInt(i);
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return imgRead.getInt(x,y,c) / img2.getInt(x,y,c);
            }
        };
    }

    public static ImgSubpixelOp abs(IImgRead img) {
        Layer<?> layer = new Layer<>(ImgSubpixelOp.class, "absolute", false, null);
        return new ImgSubpixelOp(img, layer) {
            @Override
            public int applyOp(IImgRead imgRead, int i) {
                return Math.abs(imgRead.getInt(i));
            }

            @Override
            public int applyOp(IImgRead imgRead, int x, int y, int c) {
                return Math.abs(imgRead.getInt(x,y,c));
            }
        };
    }

    @Override
    public int getInt(int i) {
        return applyOp(img, i);
    }

}
