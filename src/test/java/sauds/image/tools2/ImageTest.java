package sauds.image.tools2;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageTest {

    @Test
    public void testTo1D() {
        Image image = ImageRaster.create(3, 4, 5);
        int counter = 0;
        for (int k = 0; k < image.getDepth(); k++) {
            for (int j = 0; j < image.getHeight(); j++) {
                for (int i = 0; i < image.getWidth(); i++) {
                    int actual = image.to1D(i,j,k);
                    System.out.printf("(%d,%d,%d) E: %d, A: %d\n", i,j,k, counter, actual);
                    assertThat(actual).isEqualTo(counter);
                    counter++;
                }
            }
        }
    }

    @Test
    public void testTo3D() {
        Image image = ImageRaster.create(3, 4, 5);
        for (int i = 0; i < image.getSubpixelCount(); i++) {
            int[] coord = image.to3D(i);
            int x=coord[0], y=coord[1], z=coord[2];
            int actual = image.to1D(x,y,z);
            System.out.printf("(%d,%d,%d) E: %d, A: %d\n", x,y,z, i, actual);
            assertThat(actual).isEqualTo(i);
        }
    }

}