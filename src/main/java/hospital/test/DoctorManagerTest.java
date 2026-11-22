package hospital.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import hospital.model.dao.DoctorOffDAO;
import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorOff;
import hospital.model.domain.DoctorSchedule;
import hospital.model.domain.OffSource;
import hospital.model.exception.DoctorOffNotDeletableException;
import hospital.model.exception.ExistingDoctorOffException;
import hospital.model.exception.InvalidScheduleException;
import hospital.model.manager.DoctorManager;

// DoctorManager 관리자 메서드 테스트 (테스트 데이터: 의사 1명, 일정 1개(09:00~18:00))
public class DoctorManagerTest {

    public static void main(String[] args) throws Exception {
        DoctorManager doctorMng = DoctorManager.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 1. 전체 의사 조회
        System.out.println("1. 전체 의사 수: " + doctorMng.findDoctorList().size());
        Long doctorId = doctorMng.findDoctorList().get(0).getDoctorId();

        System.out.println();

        // 2. 의사 상세 조회 (일정, 휴진 포함)
        Doctor doctor = doctorMng.findDoctor(doctorId);
        System.out.println("2. 의사 이름: " + doctor.getName()
                + " / 일정 수: " + doctor.getSchedules().size()
                + " / 휴진 수: " + doctor.getOffs().size());
        System.out.println("3. 없는 의사 조회: " + doctorMng.findDoctor(-1L));

        System.out.println();

        // 3. 정기 진료시간 수정
        DoctorSchedule schedule = doctor.getSchedules().get(0);

        schedule.setStartTime("18:00");
        schedule.setEndTime("09:00");
        try {
            doctorMng.updateSchedule(schedule);
            System.out.println("test1 성공");
        } catch (InvalidScheduleException ex) {
            System.out.println("test1 실패: " + ex.getMessage());
        }

        schedule.setStartTime("10:00");
        schedule.setEndTime("17:00");
        try {
            doctorMng.updateSchedule(schedule);
            System.out.println("test2 성공 → "
                    + doctorMng.findDoctor(doctorId).getSchedules().get(0).getTimeRange());
        } catch (InvalidScheduleException ex) {
            System.out.println("test2 실패: " + ex.getMessage());
        }

        DoctorSchedule wrongSchedule = new DoctorSchedule();
        wrongSchedule.setScheduleId(-1L);
        wrongSchedule.setDoctorId(doctorId);
        wrongSchedule.setStartTime("09:00");
        wrongSchedule.setEndTime("18:00");
        try {
            doctorMng.updateSchedule(wrongSchedule);
            System.out.println("test3 성공");
        } catch (InvalidScheduleException ex) {
            System.out.println("test3 실패: " + ex.getMessage());
        }

        // 일정 복구
        schedule.setStartTime("09:00");
        schedule.setEndTime("18:00");
        doctorMng.updateSchedule(schedule);

        System.out.println();

        // 4. 휴진 등록
        Date offDate = dayFormat.parse("2026-12-25");
        deleteOffOn(doctorId, offDate);   // 이전 실행에서 남은 휴진 정리

        DoctorOff off = new DoctorOff();
        off.setDoctorId(doctorId);
        off.setOffDate(offDate);
        off.setReason("테스트휴진");

        try {
            doctorMng.createDoctorOff(off);
            System.out.println("test4 성공 → 등록 경로: " + findOffOn(doctorId, offDate).getSource());
        } catch (ExistingDoctorOffException ex) {
            System.out.println("test4 실패: " + ex.getMessage());
        }
        try {
            doctorMng.createDoctorOff(off);
            System.out.println("test5 성공");
        } catch (ExistingDoctorOffException ex) {
            System.out.println("test5 실패: " + ex.getMessage());
        }

        System.out.println();

        // 5. 휴진 삭제
        Long offId = findOffOn(doctorId, offDate).getOffId();
        try {
            doctorMng.deleteDoctorOff(offId);
            System.out.println("test6 성공 → 삭제 후 조회: " + findOffOn(doctorId, offDate));
        } catch (DoctorOffNotDeletableException ex) {
            System.out.println("test6 실패: " + ex.getMessage());
        }
        try {
            doctorMng.deleteDoctorOff(-1L);
            System.out.println("test7 성공");
        } catch (DoctorOffNotDeletableException ex) {
            System.out.println("test7 실패: " + ex.getMessage());
        }

        // 공휴일 휴진은 Manager로 등록할 수 없어서 DAO로 직접 등록
        Date holiday = dayFormat.parse("2026-12-31");
        deleteOffOn(doctorId, holiday);
        DoctorOff holidayOff = new DoctorOff();
        holidayOff.setDoctorId(doctorId);
        holidayOff.setOffDate(holiday);
        holidayOff.setReason("테스트공휴일");
        holidayOff.setSource(OffSource.HOLIDAY_API);
        new DoctorOffDAO().create(holidayOff);

        try {
            doctorMng.deleteDoctorOff(findOffOn(doctorId, holiday).getOffId());
            System.out.println("test8 성공");
        } catch (DoctorOffNotDeletableException ex) {
            System.out.println("test8 실패: " + ex.getMessage());
        }

        // 테스트 데이터 정리
        deleteOffOn(doctorId, holiday);
    }

    // 해당 날짜의 휴진 찾기 (없으면 null)
    private static DoctorOff findOffOn(Long doctorId, Date date) throws Exception {
        for (DoctorOff item : new DoctorOffDAO().findByDoctorId(doctorId)) {
            if (item.isOn(date)) {
                return item;
            }
        }
        return null;
    }

    // 해당 날짜의 휴진 삭제 (공휴일 휴진도 지우기 위해 DAO로 직접 삭제)
    private static void deleteOffOn(Long doctorId, Date date) throws Exception {
        DoctorOff item = findOffOn(doctorId, date);
        if (item != null) {
            new DoctorOffDAO().delete(item.getOffId());
        }
    }
}
