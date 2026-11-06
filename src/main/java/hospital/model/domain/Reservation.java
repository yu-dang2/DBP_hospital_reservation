package hospital.model.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reservation {

    private Long reservationId;
    private Member member;
    private Doctor doctor;
    private DoctorSchedule doctorSchedule;
    private Date reservationDate;
    private ReservationStatus status;
    private Date createdAt;
    private Date canceledAt;

    // 주요 메서드1: 예약 취소
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = new Date();
    }

    // 주요 메서드2: 예약 일정 변경
    public void changeSchedule(DoctorSchedule doctorSchedule, Date reservationDate) {
        this.doctorSchedule = doctorSchedule;
        this.reservationDate = reservationDate;
    }

    // 주요 메서드3: 예약 취소 가능 여부 확인
    public boolean isCancelable() {
        return this.status == ReservationStatus.RESERVED;
    }
}