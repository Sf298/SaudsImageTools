package sauds.image.tools;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageRasterTest {

    @Test
    public void testCreateBlank() {
        Image img = ImageRaster.create(2,3,4);

        assertThat(img.getWidth()).isEqualTo(2);
        assertThat(img.getHeight()).isEqualTo(3);
        assertThat(img.getDepth()).isEqualTo(4);
        assertThat(img.sumAllValues()).isEqualTo(0);
    }

    @Test
    public void testCreateWithArray() {
        Image img = ImageRaster.create(2,2,2, new byte[] {1,2,3,4,5,6,7,8});

        assertThat(img.getWidth()).isEqualTo(2);
        assertThat(img.getHeight()).isEqualTo(2);
        assertThat(img.getDepth()).isEqualTo(2);
        assertThat(img.sumAllValues()).isEqualTo(1+2+3+4+5+6+7+8);
    }

    @Test
    public void testCreateWithMapper() {
        int w=3, h=4, d=2;
        Image img = ImageRaster.create(w,h,d, t -> (byte)(t.getLeft()+t.getMiddle()+t.getRight()));

        assertThat(img.getWidth()).isEqualTo(w);
        assertThat(img.getHeight()).isEqualTo(h);
        assertThat(img.getDepth()).isEqualTo(d);
        int x=0;
        for (int k = 0; k < d; k++) {
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    assertThat(img.getInt(x)).isEqualTo(i+j+k);
                    x++;
                }
            }
        }
    }

    @Test
    public void testGetIntAndSetInt() {
        ImageRaster img = ImageRaster.create(2,1,1, new byte[] {1,32});

        assertThat(img.getWidth()).isEqualTo(2);
        assertThat(img.getHeight()).isEqualTo(1);
        assertThat(img.getDepth()).isEqualTo(1);

        assertThat(img.getInt(0)).isEqualTo(1);
        assertThat(img.getInt(1,0,0)).isEqualTo(32);

        img.setInt(0, 4);
        assertThat(img.getInt(0)).isEqualTo(4);

        img.setInt(1,0,0, -1);
        assertThat(img.getInt(1,0,0)).isEqualTo(255);
    }

}