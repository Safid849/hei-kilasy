package hei.school;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class GradeCalculatorTest {

    private static final BigDecimal RAW_POINTS = new BigDecimal("16");
    private static final BigDecimal MAX_POINTS = new BigDecimal("20");
    private static final LocalDate DEADLINE = LocalDate.of(2026, 11, 26);

    @Test
    void submissionBeforeEndOfDeadlineDayIsNotLate() {
        LocalDateTime submittedAt = DEADLINE.atTime(23, 59, 59);

        long lateDays = GradeCalculator.lateDays(DEADLINE, submittedAt);

        assertEquals(0, lateDays);
        assertEquals(new BigDecimal("16.0"), GradeCalculator.calculate(RAW_POINTS, MAX_POINTS, lateDays));
    }

    @Test
    void twoHoursAfterDeadlineIncursOneStartedDayPenalty() {
        LocalDateTime submittedAt = DEADLINE.plusDays(1).atTime(2, 0);

        long lateDays = GradeCalculator.lateDays(DEADLINE, submittedAt);

        assertEquals(1, lateDays);
        assertEquals(new BigDecimal("14.4"), GradeCalculator.calculate(RAW_POINTS, MAX_POINTS, lateDays));
    }

    @Test
    void eachStartedTwentyFourHourPeriodCountsAtItsBoundary() {
        LocalDateTime cutoff = DEADLINE.plusDays(1).atStartOfDay();

        assertEquals(1, GradeCalculator.lateDays(DEADLINE, cutoff));
        assertEquals(1, GradeCalculator.lateDays(DEADLINE, cutoff.plus(Duration.ofDays(1)).minusNanos(1)));
        assertEquals(2, GradeCalculator.lateDays(DEADLINE, cutoff.plusDays(1)));
        assertEquals(2, GradeCalculator.lateDays(DEADLINE, cutoff.plusDays(1).plusNanos(1)));
    }

    @Test
    void threeDaysLateIncursThirtyPercentPenalty() {
        long lateDays = GradeCalculator.lateDays(DEADLINE, DEADLINE.plusDays(3).atStartOfDay());

        assertEquals(3, lateDays);
        assertEquals(new BigDecimal("11.2"), GradeCalculator.calculate(RAW_POINTS, MAX_POINTS, lateDays));
    }

    @Test
    void normalizesDifferentExerciseScalesBeforeApplyingPenalty() {
        assertEquals(new BigDecimal("14.4"),
                GradeCalculator.calculate(new BigDecimal("8"), new BigDecimal("10"), 1));
    }

    @Test
    void penaltyIsCappedAtFiftyPercent() {
        long lateDays = GradeCalculator.lateDays(DEADLINE, DEADLINE.plusDays(8).atStartOfDay());

        assertEquals(8, lateDays);
        assertEquals(new BigDecimal("8.0"), GradeCalculator.calculate(RAW_POINTS, MAX_POINTS, lateDays));
    }

    @Test
    void zeroRawPointsNeverProducesANegativeGrade() {
        assertEquals(new BigDecimal("0.0"), GradeCalculator.calculate(BigDecimal.ZERO, MAX_POINTS, 8));
    }
}
