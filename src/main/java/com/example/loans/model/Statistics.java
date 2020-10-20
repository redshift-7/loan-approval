package com.example.loans.model;

import com.example.loans.finance.BigDecimalSummaryStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collection;

@Builder
@AllArgsConstructor
public class Statistics {

    private final BigDecimalSummaryStatistics bigDecimalSummaryStatistics;
    @Getter
    private final long count;
    @Getter
    private final BigDecimal sum;
    @Getter
    private final BigDecimal avg;
    @Getter
    private final BigDecimal max;
    @Getter
    private final BigDecimal min;

    public static Statistics calculate(Collection<BigDecimal> amounts) {
        BigDecimalSummaryStatistics statistics = amounts.stream().collect(BigDecimalSummaryStatistics.statistics());
        return Statistics.builder()
                .count(statistics.getCount())
                .sum(statistics.getSum())
                .avg(statistics.getAvg())
                .max(statistics.getMax())
                .min(statistics.getMin())
                .build();
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "count=" + count +
                ", sum=" + sum +
                ", avg=" + avg +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}
