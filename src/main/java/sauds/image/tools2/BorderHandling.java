package sauds.image.tools2;

public enum BorderHandling {

    /**
     * Behaves like the unchecked get(). Mainly used for optimising the convolution function.
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
