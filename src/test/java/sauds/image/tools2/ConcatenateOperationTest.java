package sauds.image.tools2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ConcatenateOperationTest {

    @Test
    public void testConcatenateHorizontally() {
        Image img1 = ImageRaster.create(2,2,1, new byte[] {1,1, 1,1});
        Image img2 = ImageRaster.create(2,2,1, new byte[] {2,2, 2,2});
        Image img3 = ImageRaster.create(2,2,1, new byte[] {3,3, 3,3});
        List<Image> images = new ArrayList<>();
        images.add(img1);
        images.add(img2);
        images.add(img3);

        Image output = ConcatenateOperation.concatHorizontally(images);

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

        Image output = ConcatenateOperation.concatVertically(images);

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

        Image output = ConcatenateOperation.concatChannels(images);

        assertThat(output.getWidth()).isEqualTo(2);
        assertThat(output.getHeight()).isEqualTo(2);
        assertThat(output.getDepth()).isEqualTo(3);
        assertThat(output.sumAllValues()).isEqualTo(24);
    }

}