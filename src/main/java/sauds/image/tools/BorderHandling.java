package sauds.image.tools;

public enum BorderHandling {

    /**
     * Behaves like the unchecked get(). Mainly used for optimising the convolution function.
     * Using this will result in an image 2 pixels smaller in each axis than the original.
     */
    INNER,

    /**
     * Requests for pixels outside the bounds return null.
     */
    IGNORE,

    /**
     * Requests for pixels outside the bounds return the value of the nearest
     * pixel within the bounds.
     */
    EXTEND;

    //WRAP;

}
