package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RangePeriodDto extends PeriodDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;

}
