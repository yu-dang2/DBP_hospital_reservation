package hospital.test;

import java.sql.SQLException;
import java.util.List;

import hospital.model.dao.DoctorDAO;
import hospital.model.dao.DoctorScheduleDAO;
import hospital.model.domain.DoctorSchedule;

public class DoctorScheduleDAOTest {
    public static void main(String[] args) throws SQLException {
        DoctorScheduleDAO scheduleDAO = new DoctorScheduleDAO();
        Long doctorId = new DoctorDAO().findAll().get(0).getDoctorId();

        // 1. 일정 조회
        DoctorSchedule schedule = scheduleDAO.findByDoctorId(doctorId).get(0);
        System.out.println("1. 처음 일정: " + toText(schedule));

        // 2. 정상 수정
        schedule.setStartTime("10:00");
        schedule.setEndTime("17:00");
        System.out.println("2. 수정된 행 수: " + scheduleDAO.update(schedule));

        List<DoctorSchedule> afterUpdate = scheduleDAO.findByDoctorId(doctorId);
        System.out.println("3. 수정 후 일정: " + toText(afterUpdate.get(0)));

        // 3. 잘못된 시간 순서: DB 제약조건 오류가 나야 정상
        schedule.setStartTime("18:00");
        schedule.setEndTime("09:00");
        try {
            scheduleDAO.update(schedule);
            System.out.println("4. 실패: 오류가 나지 않았습니다.");
        } catch (SQLException ex) {
            System.out.println("4. 예상한 오류 코드: ORA-" + ex.getErrorCode());
        }

        // 4. 원래 값으로 복구
        schedule.setStartTime("09:00");
        schedule.setEndTime("18:00");
        System.out.println("5. 복구된 행 수: " + scheduleDAO.update(schedule));
    }

    private static String toText(DoctorSchedule schedule) {
        return schedule.getDayOfWeek() + " " + schedule.getTimeRange()
                + ", 휴무=" + schedule.isDayOff();
    }
}
