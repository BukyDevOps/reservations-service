package buky.example.reservationsservice.util;

import buky.example.reservationsservice.dto.AccommodationDto;
import buky.example.reservationsservice.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestUtil {

    private final RestTemplate restTemplate;

    @Value(value="${accommodation.BaseURL}")
    private String accommodationURL;
    @Value(value="${user.BaseURL}")
    private String userURL;

    public AccommodationDto getAccommodationById(Long id) {
        String url = accommodationURL + id;
        ResponseEntity<AccommodationDto> responseEntity =
                restTemplate.getForEntity(url, AccommodationDto.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new NotFoundException(String.format("Accommodation with id: %d ,not found", id));
        }
    }

    public Long getHostByAccommodationId(Long id) {
        String url = accommodationURL+"/api/accommodation/host/" + id;
        ResponseEntity<Long> responseEntity =
                restTemplate.getForEntity(url, Long.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new NotFoundException(String.format("Accommodation with id: %d ,not found", id));
        }
    }

    public boolean userExistsById(Long id) {
        String url = userURL+"/api/user/" + id;
        ResponseEntity<Object> responseEntity =
                restTemplate.getForEntity(url, Object.class);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    public List<Long> getAccommodationIdsByOwner(Long ownerId) {
        String url = accommodationURL + "/api/accommodation/ids-by-user/" + ownerId;
        ResponseEntity<List<Long>> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Long>>() {}
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return List.of();
    }
}
