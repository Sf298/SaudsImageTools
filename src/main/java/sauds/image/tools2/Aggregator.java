package sauds.image.tools2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A collection of functions that can be used with the convolution operation
 */
public abstract class Aggregator {

    public static final Function<Kernel, Aggregator> SUM = kernel -> new Aggregator() {
        private int sum = 0;
        @Override
        public void addValue(int value) {
            sum += value;
        }
        @Override
        public int getResult() {
            return sum;
        }
    };
    public static final Function<Kernel, Aggregator> MEAN = kernel -> new Aggregator() {
        private int sum = 0;
        private int count = 0;
        @Override
        public void addValue(int value) {
            sum += value;
            count++;
        }
        @Override
        public int getResult() {
            return sum / count;
        }
    };
    public static final Function<Kernel, Aggregator> MEDIAN = kernel -> new Aggregator() {
        private final List<Integer> values = new ArrayList<>();
        @Override
        public void addValue(int value) {
            values.add(value);
        }
        @Override
        public int getResult() {
            Collections.sort(values);
            return values.get(values.size() / 2);
        }
    };

    public abstract void addValue(int value);
    public abstract int getResult();

}
