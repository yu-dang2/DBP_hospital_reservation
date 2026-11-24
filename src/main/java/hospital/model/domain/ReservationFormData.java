package hospital.model.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationFormData {

    private Doctor doctor;
    private List<DoctorSchedule> schedules;

}