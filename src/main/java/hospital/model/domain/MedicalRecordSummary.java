package hospital.model.domain;

import java.util.date;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter

// 마이페이지의 최근 진료기록 정보
public class MedicalRecordSummary {
    private Long recordId;        // 진료기록 ID
    private Date recordDate;      // 진료일
    private String doctorName;    // 담당 의사 이름
    private String diagnosis;     // 진단명


    //기본 생성자
    public MedicalRecordSummary() {
    }

    public MedicalRecordSummary(Long recordId, Date recordDate, String doctorName, String diagnosis) {
        this.recordId = recordId;
        this.recordDate = recordDate;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
    }
}
