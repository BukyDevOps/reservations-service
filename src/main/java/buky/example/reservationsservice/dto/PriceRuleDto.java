package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceRuleDto {
    private Long id;
    private double specialPrice;
    private RangePeriodDto rangePeriod;
    private PatternPeriodDto patternPeriod;

}
