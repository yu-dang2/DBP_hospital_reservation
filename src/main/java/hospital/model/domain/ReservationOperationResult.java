package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationOperationResult {

    private boolean success;
    private String message;
}
