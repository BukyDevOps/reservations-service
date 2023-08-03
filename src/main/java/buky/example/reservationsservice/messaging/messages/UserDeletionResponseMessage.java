package buky.example.reservationsservice.messaging.messages;

import buky.example.reservationsservice.model.enumerations.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDeletionResponseMessage implements Serializable {
    Long userId;
    Role role;
    boolean permitted;
}