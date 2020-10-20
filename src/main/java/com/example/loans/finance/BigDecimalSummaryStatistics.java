package com.example.loans.finance;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;

public class BigDecimalSummaryStatistics implements Consumer<BigDecimal> {

    public static Collector<BigDecimal, BigDecimalSummaryStatistics, BigDecimalSummaryStatistics> statistics() {
        return Collector.of(BigDecimalSummaryStatistics::new,
                            BigDecimalSummaryStatistics::accept,
                            BigDecimalSummaryStatistics::merge);
    }

    @Getter
    private BigDecimal sum = BigDecimal.ZERO;
    @Getter
    private BigDecimal min = BigDecimal.ZERO;
    @Getter
    private BigDecimal max = BigDecimal.ZERO;
    @Getter
    private long count;

    public void accept(BigDecimal t) {
        if (count == 0) {
            Objects.requireNonNull(t);
            count = 1;
            sum = t;
            min = t;
            max = t;
        } else {
            sum = sum.add(t);
            if (min.compareTo(t) > 0) min = t;
            if (max.compareTo(t) < 0) max = t;
            count++;
        }
    }

    public BigDecimalSummaryStatistics merge(BigDecimalSummaryStatistics s) {
        if (s.count > 0) {
            if (count == 0) {
                count = s.count;
                sum = s.sum;
                min = s.min;
                max = s.max;
            } else {
                sum = sum.add(s.sum);
                if (min.compareTo(s.min) > 0) min = s.min;
                if (max.compareTo(s.max) < 0) max = s.max;
                count += s.count;
            }
        }
        return this;
    }

    public BigDecimal getAvg() {
        return count < 2 ? sum : sum.divide(BigDecimal.valueOf(count), MathContext.DECIMAL128);
    }

    @Override
    public String toString() {
        return count == 0 ? "empty" : (count + " elements between " + min + " and " + max + ", sum=" + sum);
    }
}
