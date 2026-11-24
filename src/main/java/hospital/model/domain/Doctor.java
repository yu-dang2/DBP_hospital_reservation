package hospital.model.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Doctor {

    private Long doctorId;
    private String name;
    private String specialty;
    private String bio;
    private Long deptId;
    private String departmentName;   // 소속 진료과명 (DoctorDAO에서 DEPARTMENT JOIN으로 채움)

    private List<DoctorSchedule> schedules = new ArrayList<>();
    private List<DoctorOff> offs = new ArrayList<>();

    // 주요 메서드1: 진료 일정 추가
    public void addSchedule(DoctorSchedule schedule) {
        schedules.add(schedule);
    }

    // 주요 메서드2: 휴진 정보 추가
    public void addOff(DoctorOff off) {
        offs.add(off);
    }

    // 주요 메서드3: 특정 요일의 진료 일정 조회
    public List<DoctorSchedule> getSchedulesOf(String dayOfWeek) {
        List<DoctorSchedule> result = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {
            if (dayOfWeek.equals(schedule.getDayOfWeek())) {
                result.add(schedule);
            }
        }

        return result;
    }

    // 주요 메서드 5: 특정 날짜에 휴진하는지
    public boolean isOffOn(Date date) {
        for (DoctorOff off : offs) {
            if (off.isOn(date)) {
                return true;
            }
        }

        return false;
    }

    // 주요 메서드 6: 진료과 이름 조회 -> departmentName 필드의 Lombok getter로 대체
}