package hospital.model.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

// 마이페이지의 최근 진료기록 한 건을 담는 클래스 (MVC 설계서 Domain Class 기준)
@Getter
@Setter
public class MedicalRecordSummary {
    private Long recordId;        // 진료기록 ID
    private LocalDate recordDate; // 진료일
    private String doctorName;    // 담당 의사 이름
    private String diagnosis;     // 진단명

    public MedicalRecordSummary() {
    }

    public MedicalRecordSummary(Long recordId, LocalDate recordDate, String doctorName, String diagnosis) {
        this.recordId = recordId;
        this.recordDate = recordDate;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
    }
}
