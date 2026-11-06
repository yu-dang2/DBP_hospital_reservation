package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorSchedule {

    private Long scheduleId;
    private Long doctorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean dayOff;

    // 주요 메서드 1: 진료하는 요일인지
    public boolean isWorkingDay() {
        return !dayOff;
    }

    // 주요 메서드 2:  특정 시간에 진료 가능한지
    public boolean isAvailableAt(String time) {
        if (dayOff || time == null) {
            return false;
        }

        return time.compareTo(startTime) >= 0
                && time.compareTo(endTime) < 0;
    }

    // 진료 시간 범위: 시작 타임 - 끝 타임
    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}