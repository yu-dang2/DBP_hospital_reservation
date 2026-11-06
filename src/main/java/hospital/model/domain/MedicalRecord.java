package hospital.model.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicalRecord { 
	// 진료 완료된 예약에 대한 진단·처방 내용을 저장한다(관리자가 작성).

    private Long recordId;
    private Reservation reservation;
    private String diagnosis;
    private String prescription;
    private Date recordDate;
}
