package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RangePeriodDto extends PeriodDto {

    private Long id;
    private Date startDate;
    private Date endDate;

}
