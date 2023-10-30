package sauds.image.tools;

public interface WriteableImage extends Image {

    default void setInt(int i, int val) {
        int[] coord = to3D(i);
        setInt(coord[0], coord[1], coord[2], val);
    }

    default void setInt(int x, int y, int c, int val) {
        setInt(to1D(x, y, c), val);
    }

}
