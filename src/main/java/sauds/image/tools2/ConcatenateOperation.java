package sauds.image.tools2;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A type of {@link Image} that concatenates several images together.
 */
public class ConcatenateOperation implements Image {

    private final List<Image> images;
    private final Layer<?> layer;
    private final int width;
    private final int height;
    private final int depth;

    public ConcatenateOperation(List<Image> images, Layer<?> layer, int width, int height, int depth) {
        this.images = images;
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.depth = depth;

        /*Set<Integer> widths = images.stream().map(Image::getWidth).collect(Collectors.toSet());
        Set<Integer> heights = images.stream().map(Image::getHeight).collect(Collectors.toSet());
        Set<Integer> depths = images.stream().map(Image::getDepth).collect(Collectors.toSet());
        if (widths.size() != 1 || depths.size() != 1) {
            throw new IllegalArgumentException("Widths and Depths of all input images must match");
        }
        if (heights.size() != 1 || depths.size() != 1) {
            throw new IllegalArgumentException("Heights and Depths of all input images must match");
        }*/
    }

    public static Image concatHorizontally(List<Image> images) {
        Set<Integer> heights = images.stream().map(Image::getHeight).collect(Collectors.toSet());
        Set<Integer> depths = images.stream().map(Image::getDepth).collect(Collectors.toSet());

        if (heights.size() != 1 || depths.size() != 1) {
            throw new IllegalArgumentException("Heights and Depths of all input images must match");
        }

        int w = images.stream().mapToInt(Image::getWidth).sum();
        int h = heights.stream().findFirst().get();
        int d = depths.stream().findFirst().get();

        Layer<?> layer = new Layer<>(ConcatenateOperation.class, "concatenate-horizontally", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getWidth).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new ConcatenateOperation(images, layer, w, h, d) {
            @Override
            public int getInt(int x, int y, int c) {
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

        Layer<?> layer = new Layer<>(ConcatenateOperation.class, "concatenate-vertically", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getHeight).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new ConcatenateOperation(images, layer, w, h, d) {
            @Override
            public int getInt(int x, int y, int c) {
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

        Layer<?> layer = new Layer<>(ConcatenateOperation.class, "concatenate-channels", false, images.size());

        // calculate channel index map
        int[][] mappings = calculateIndexMappings(images.stream().map(Image::getDepth).collect(Collectors.toList()));
        int[] imageIndexMap = mappings[0];
        int[] depthIndexMap = mappings[1];

        return new ConcatenateOperation(images, layer, w, h, d) {
            @Override
            public int getInt(int x, int y, int c) {
                return images.get(imageIndexMap[c]).getInt(x, y, depthIndexMap[c]);
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

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public List<Layer<?>> getLayers() {
        List<Layer<?>> layers = images.stream().flatMap(i->i.getLayers().stream()).collect(Collectors.toList());
        layers.add(layer);
        return layers;
    }

}
