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
public class AccommodationDto {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private int minGuestNum = 0;
    private int maxGuestNum;
    private boolean autoApproveReservation = false;
    private Set<String> tags;
    private Set<String> images;
    private LocationDto location;
    private AccommodationAvailabilityDto availability;


}
