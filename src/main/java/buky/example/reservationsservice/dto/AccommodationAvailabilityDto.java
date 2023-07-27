package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccommodationAvailabilityDto {

    private Long id;
    List<RangePeriodDto> allRangePeriods;
    List<PatternPeriodDto> allPatternPeriods;
    private PriceDto price;

}
