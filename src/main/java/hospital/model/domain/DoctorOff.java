package hospital.model.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorOff {

    private Long offId;
    private Long doctorId;
    private Date offDate;
    private String reason;
    private OffSource source;

    // 주요 메서드1: 특정 날짜에 휴진하는지
    public boolean isOn(Date date) {
        if (date == null || offDate == null) {
            return false;
        }

        return offDate.equals(date);
    }

    // 수동 등록한 휴진인지 확인 (관리자가 등록합니다, 제은님 자료 바탕으로 수정)
    public boolean isDeletable() {
        return this.source == OffSource.MANUAL;
    }
}