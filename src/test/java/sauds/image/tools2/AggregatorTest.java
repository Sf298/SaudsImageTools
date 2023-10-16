package sauds.image.tools2;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregatorTest {

    @Test
    public void testSum() {
        Kernel kernel1 = Kernel.boxBlur(1);
        Aggregator agg1 = Aggregator.SUM.apply(kernel1);
        agg1.addValue(4);
        agg1.addValue(56);
        agg1.addValue(24);
        assertThat(agg1.getResult()).isEqualTo(84);

        Kernel kernel2 = Kernel.boxBlur(2);
        Aggregator agg2 = Aggregator.SUM.apply(kernel2);
        agg2.addValue(+41);
        agg2.addValue(+565);
        agg2.addValue(+52);
        agg2.addValue(+2);
        assertThat(agg2.getResult()).isEqualTo(660);
    }

    @Test
    public void testMean() {
        Kernel kernel1 = Kernel.boxBlur(1);
        Aggregator agg1 = Aggregator.MEAN.apply(kernel1);
        agg1.addValue(4);
        agg1.addValue(56);
        agg1.addValue(24);
        assertThat(agg1.getResult()).isEqualTo(28);

        Kernel kernel2 = Kernel.boxBlur(2);
        Aggregator agg2 = Aggregator.MEAN.apply(kernel2);
        agg2.addValue(+41);
        agg2.addValue(+565);
        agg2.addValue(+52);
        agg2.addValue(+2);
        assertThat(agg2.getResult()).isEqualTo(165);
    }

    @Test
    public void testMedian() {
        Kernel kernel1 = Kernel.boxBlur(1);
        Aggregator agg1 = Aggregator.MEDIAN.apply(kernel1);
        agg1.addValue(4);
        agg1.addValue(56);
        agg1.addValue(24);
        assertThat(agg1.getResult()).isEqualTo(24);

        Kernel kernel2 = Kernel.boxBlur(2);
        Aggregator agg2 = Aggregator.MEDIAN.apply(kernel2);
        agg2.addValue(41);
        agg2.addValue(565);
        agg2.addValue(52);
        agg2.addValue(2);
        assertThat(agg2.getResult()).isEqualTo(47);
    }
}