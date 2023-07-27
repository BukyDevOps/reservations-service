package buky.example.reservationsservice.dto;

import buky.example.reservationsservice.enumerations.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Set<Date> getPeriodDates(Date start, Date end, Set<DayOfWeek> days) {
        Set<Date> resultDates = new HashSet<>();
        if (days.isEmpty())
            return resultDates;

        Set<Integer> targetDayOfWeekSet = days.stream().map(DayOfWeek::ordinal).collect(Collectors.toSet());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        while (!calendar.getTime().after(end)) {
            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 - Sunday, 2 - Monday, 6 - Saturday
            if (targetDayOfWeekSet.contains(currentDayOfWeek)) {
                resultDates.add(calendar.getTime());
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return resultDates;
    }
}
