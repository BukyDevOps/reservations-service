package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatternPeriodDto extends PeriodDto {

    private Long id;
    private Set<DayOfWeek> dayOfWeek;

    /**
     * for start and end date (dates included), return all dates which pass trough the day name filter...
     */
    public Set<LocalDate> getPeriodDates(LocalDate start, LocalDate end) {
        Set<LocalDate> resultDates = new HashSet<>();
        if (dayOfWeek.isEmpty() || start.isAfter(end))
            return resultDates;

        while (!start.atStartOfDay().equals(end.atStartOfDay())) {
            if (dayOfWeek.contains(start.getDayOfWeek())) {
                resultDates.add(LocalDate.from(start));
            }
            start = start.plusDays(1);
        }
        return resultDates;
    }
}
