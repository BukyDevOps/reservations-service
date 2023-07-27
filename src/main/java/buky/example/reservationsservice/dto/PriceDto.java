package buky.example.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceDto {
    private Long id;
    private double basePrice;
    private boolean byPerson;
    Set<PriceRuleDto> priceRules;

}
