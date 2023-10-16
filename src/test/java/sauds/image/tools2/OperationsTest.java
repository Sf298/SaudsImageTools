package sauds.image.tools2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OperationsTest {

    /////////////////////////////////////////////////
    //             General Operations              //
    /////////////////////////////////////////////////
    @Test
    public void testTemp() {

    }

    @Test
    public void testConvolution_separable() {
        Image img = ImageRaster.create(3,3,2, new byte[] {
                10, 20, 30,
                40, 50, 60,
                70, 80, 90,

                20, 30, 40,
                50, 60, 70,
                80, 90, 100
        });
        Image output = Operations.convolve(img, BorderHandling.IGNORE, Kernel.boxBlur(5), Aggregator.MEAN, 1, 1);
        int[] actual = output.evaluate().getValuesInt();
        int[] expected = new int[] {
                50, 50, 50,
                50, 50, 50,
                50, 50, 50,

                60, 60, 60,
                60, 60, 60,
                60, 60, 60,
        };
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testConvolution_notSeparable() {
        Image img = ImageRaster.create(3,3,2, new byte[] {
                10, 20, 30,
                40, 50, 60,
                70, 80, 90,

                20, 30, 40,
                50, 60, 70,
                80, 90, 100
        });
        Image output = Operations.convolve(img, BorderHandling.IGNORE, Kernel.gaussian3x3(), Aggregator.MEAN, 1, 1);
        int[] actual = output.evaluate().getValuesInt();
        int[] expected = new int[] {
                52, 60, 82,
                86, 88, 113,
                142, 140, 172,

                75, 80, 105,
                106, 106, 133,
                165, 160, 195
        };
        assertThat(actual).isEqualTo(expected);
    }


    /////////////////////////////////////////////////
    //            Subpixel Operations              //
    /////////////////////////////////////////////////
    @Test
    public void testAddInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)1);
        Image output = Operations.add(img, 23);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(23+1));
    }

    @Test
    public void testAddImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)4);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)1);
        Image output = Operations.add(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(4+1));
    }

    @Test
    public void testSubtractInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = Operations.subtract(img, 23);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4);
    }

    @Test
    public void testSubtractImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)23);
        Image output = Operations.subtract(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24-23));
    }

    @Test
    public void testMultiplyInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = Operations.multiply(img, 3);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24*3));
    }

    @Test
    public void testMultiplyImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)3);
        Image output = Operations.multiply(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24*3));
    }

    @Test
    public void testDivideInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = Operations.divide(img, 3);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24/3));
    }

    @Test
    public void testDivideImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)3);
        Image output = Operations.divide(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24/3));
    }

    @Test
    public void testAbs() {
        Image img1 = ImageRaster.create(2,3,4, t -> (byte)1);
        Image img2 = Operations.subtract(img1, 4);
        Image output = Operations.abs(img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*3);
    }


    /////////////////////////////////////////////////
    //          Concatenation Operations           //
    /////////////////////////////////////////////////
    @Test
    public void testConcatenateHorizontally() {
        Image img1 = ImageRaster.create(2,2,1, new byte[] {1,1, 1,1});
        Image img2 = ImageRaster.create(2,2,1, new byte[] {2,2, 2,2});
        Image img3 = ImageRaster.create(2,2,1, new byte[] {3,3, 3,3});
        List<Image> images = new ArrayList<>();
        images.add(img1);
        images.add(img2);
        images.add(img3);

        Image output = Operations.concatHorizontally(images);

        assertThat(output.getWidth()).isEqualTo(6);
        assertThat(output.getHeight()).isEqualTo(2);
        assertThat(output.getDepth()).isEqualTo(1);
        assertThat(output.sumAllValues()).isEqualTo(24);
    }

    @Test
    public void testConcatenateVertically() {
        Image img1 = ImageRaster.create(2,2,1, new byte[] {1,1, 1,1});
        Image img2 = ImageRaster.create(2,2,1, new byte[] {2,2, 2,2});
        Image img3 = ImageRaster.create(2,2,1, new byte[] {3,3, 3,3});
        List<Image> images = new ArrayList<>();
        images.add(img1);
        images.add(img2);
        images.add(img3);

        Image output = Operations.concatVertically(images);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(6);
        assertThat(output.getDepth()).isEqualTo(1);
        assertThat(output.sumAllValues()).isEqualTo(24);
    }

    @Test
    public void testConcatenateChannels() {
        Image img1 = ImageRaster.create(2,2,1, new byte[] {1,1, 1,1});
        Image img2 = ImageRaster.create(2,2,1, new byte[] {2,2, 2,2});
        Image img3 = ImageRaster.create(2,2,1, new byte[] {3,3, 3,3});
        List<Image> images = new ArrayList<>();
        images.add(img1);
        images.add(img2);
        images.add(img3);

        Image output = Operations.concatChannels(images);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(2);
        assertThat(output.getDepth()).isEqualTo(3);
        assertThat(output.sumAllValues()).isEqualTo(24);
    }

}