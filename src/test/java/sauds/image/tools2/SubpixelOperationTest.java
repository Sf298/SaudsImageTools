package sauds.image.tools2;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubpixelOperationTest {

    @Test
    public void testAddInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)1);
        Image output = SubpixelOperation.add(img, 23);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(23+1));
    }

    @Test
    public void testAddImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)4);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)1);
        Image output = SubpixelOperation.add(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(4+1));
    }

    @Test
    public void testSubInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = SubpixelOperation.sub(img, 23);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4);
    }

    @Test
    public void testSubImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)23);
        Image output = SubpixelOperation.sub(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24-23));
    }

    @Test
    public void testMultInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = SubpixelOperation.mult(img, 3);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24*3));
    }

    @Test
    public void testMultImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)3);
        Image output = SubpixelOperation.mult(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24*3));
    }

    @Test
    public void testDivInt() {
        Image img = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image output = SubpixelOperation.div(img, 3);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24/3));
    }

    @Test
    public void testDivImage() {
        Image img1 = ImageRaster.create(2,3,4, triple -> (byte)24);
        Image img2 = ImageRaster.create(2,3,4, triple -> (byte)3);
        Image output = SubpixelOperation.div(img1, img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*(24/3));
    }

    @Test
    public void testAbs() {
        Image img1 = ImageRaster.create(2,3,4, t -> (byte)1);
        Image img2 = SubpixelOperation.sub(img1, 4);
        Image output = SubpixelOperation.abs(img2);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(3);
        assertThat(output.getDepth()).isEqualTo(4);
        assertThat(output.sumAllValues()).isEqualTo(2*3*4*3);
    }

}