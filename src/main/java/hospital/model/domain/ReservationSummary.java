package hospital.model.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationSummary {

    private Long reservationId;
    private String doctorName;
    private Date reservationDate;
    private ReservationStatus status;
}