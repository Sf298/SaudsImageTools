package maintest;

import sauds.image.tools2.Kernel;
import sauds.image.tools2.*;

import java.io.File;
import java.io.IOException;

public class Test2 {

    public static void main(String[] args) throws IOException {
        long start;

        sauds.image.tools.Img im1 = sauds.image.tools.Img.createNew(new File("/home/saud/Pictures/IMG_20220504_205722__01.jpg"));
        sauds.image.tools.Kernel kern = sauds.image.tools.Kernel.gaussian3x3();
        start = System.currentTimeMillis();
        im1 = im1.convolve(kern, sauds.image.tools.Img.BORDER_EXTEND, 1, sauds.image.tools.Img.CONV_MEAN, true, 0);
        im1 = im1.convolve(kern, sauds.image.tools.Img.BORDER_EXTEND, 1, sauds.image.tools.Img.CONV_MEAN, true, 0);
        im1 = im1.convolve(kern, sauds.image.tools.Img.BORDER_EXTEND, 1, sauds.image.tools.Img.CONV_MEAN, true, 0);
        im1.toBufferedImage();
        System.out.println("Old time: " + (System.currentTimeMillis() - start));
        sauds.image.tools.ImgViewer.showAll("OLD", im1);

        IImgRead newImg = Img.create(new File("/home/saud/Pictures/IMG_20220504_205722__01.jpg"));
        Kernel kernel = Kernel.gaussian3x3();
        start = System.currentTimeMillis();
        newImg = ImgOp.convolve(newImg, BorderHandling.EXTEND, Aggregator.MEAN, kernel);
        newImg = ImgOp.convolve(newImg, BorderHandling.EXTEND, Aggregator.MEAN, kernel);
        newImg = ImgOp.convolve(newImg, BorderHandling.EXTEND, Aggregator.MEAN, kernel);
        newImg = ImgSubpixelOp.abs(newImg);
        newImg.getLayers().forEach(System.out::println);
        newImg.toBufferedImage();
        System.out.println("New time: " + (System.currentTimeMillis() - start));
        ImgViewer.showAll("new", newImg);
    }

}
