package com.example.loans.finance;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalSummaryStatisticsTest {

    @Test
    public void testSum() {
        List<BigDecimal> input = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            input.add(BigDecimal.valueOf(123.01 + i));
        }

        Optional<BigDecimal> minCheck = input.stream().min(BigDecimal::compareTo);
        Optional<BigDecimal> maxCheck = input.stream().max(BigDecimal::compareTo);
        BigDecimal sumCheck = input.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimalSummaryStatistics result = input.stream().collect(BigDecimalSummaryStatistics.statistics());
        BigDecimal avgCheck = sumCheck.divide(BigDecimal.valueOf(20), RoundingMode.HALF_EVEN);

        assertEquals(sumCheck, result.getSum());
        assertEquals(avgCheck, result.getAvg());
        assertEquals(minCheck.get(), result.getMin());
        assertEquals(maxCheck.get(), result.getMax());
    }
}
