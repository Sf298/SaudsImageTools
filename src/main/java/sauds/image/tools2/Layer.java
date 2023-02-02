package sauds.image.tools2;

import static java.util.Objects.nonNull;

public class Layer<T> {

    private final Class<? extends IImgRead> clazz;
    private final String operationName;
    private final boolean isSlowLayer;
    private final T options;

    public Layer(Class<? extends IImgRead> clazz, String operationName, boolean isSlowLayer, T options) {
        this.clazz = clazz;
        this.operationName = operationName;
        this.isSlowLayer = isSlowLayer;
        this.options = options;
    }

    public Class<? extends IImgRead> getClazz() {
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
