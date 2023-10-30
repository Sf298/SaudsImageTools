package sauds.image.tools;

import static java.util.Objects.nonNull;

/**
 * An Object that contains the information representing a layer in the calculation call stack.
 * @param <T> A generic object that contains the parameters used to configure the layer.
 */
public class Layer<T> {

    private final Class<? extends Image> clazz;
    private final String operationName;
    private final boolean isSlowLayer;
    private final T options;

    /**
     * Create a definition of a layer.
     * @param clazz The class containing the configuration of this layer.
     * @param operationName The name of the operation.
     * @param isSlowLayer A layer is considered slow if it accesses the same pixel in the sub-layer multiple times,
     *                    during a single render. E.g. in convolution, a pixel is accessed multiple times by the
     *                    neighbouring pixels. This will stop pixel calculation cascades.
     * @param options The values used to configure this layer.
     */
    public Layer(Class<? extends Image> clazz, String operationName, boolean isSlowLayer, T options) {
        this.clazz = clazz;
        this.operationName = operationName;
        this.isSlowLayer = isSlowLayer;
        this.options = options;
    }

    public Class<? extends Image> getClazz() {
        return clazz;
    }

    public String getOperationName() {
        return operationName;
    }

    public boolean isSlowLayer() {
        return isSlowLayer;
    }

    public T getOptions() {
        return options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clazz.getName()).append(": ");

        if (nonNull(operationName)) {
            sb.append(operationName);
        }

        if (nonNull(options)) {
            sb.append(options);
        }

        return sb.toString();
    }

}
