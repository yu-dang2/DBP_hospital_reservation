package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.DoctorSchedule;
import util.JDBCUtil;

public class DoctorScheduleDAO {
    private JDBCUtil jdbcUtil = null;

    public DoctorScheduleDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 의사의 진료 일정 조회 (월요일부터 정렬, 결과가 없으면 빈 리스트 반환)
    public List<DoctorSchedule> findByDoctorId(Long doctorId) throws SQLException{
        List<DoctorSchedule> scheduleList = new ArrayList<DoctorSchedule>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT SCHEDULE_ID, DOCTOR_ID, DAY_OF_WEEK, START_TIME, END_TIME, DAY_OFF ");
        query.append("FROM DOCTOR_SCHEDULE ");
        query.append("WHERE DOCTOR_ID = ? ");
        query.append("ORDER BY DAY_OF_WEEK, START_TIME ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { doctorId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs == null) {
                throw new SQLException("진료 일정 조회 실패: doctorId=" + doctorId);
            }

            while (rs.next()) {
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setScheduleId(rs.getLong("SCHEDULE_ID"));
                schedule.setDoctorId(rs.getLong("DOCTOR_ID"));
                schedule.setDayOfWeek(rs.getString("DAY_OF_WEEK"));
                schedule.setStartTime(rs.getString("START_TIME"));
                schedule.setEndTime(rs.getString("END_TIME"));
                schedule.setDayOff(rs.getInt("DAY_OFF") == 1);   // DB의 1/0을 true/false로 변환

                scheduleList.add(schedule);
            } 
        return scheduleList;
        } finally {
            jdbcUtil.close();
        }
    }

    // 진료 시간과 휴무 여부 수정 (수정된 행 수 반환)
    // 잘못된 시간 순서 같은 DB 오류는 Manager가 처리할 수 있도록 SQLException으로 전달
    public int update(DoctorSchedule schedule) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("UPDATE DOCTOR_SCHEDULE ");
        query.append("SET START_TIME = ?, END_TIME = ?, DAY_OFF = ? ");
        query.append("WHERE SCHEDULE_ID = ? AND DOCTOR_ID = ? ");

        Object[] params = new Object[] {
            schedule.getStartTime(),
            schedule.getEndTime(),
            schedule.isDayOff() ? 1 : 0,   // true/false를 DB의 1/0으로 변환
            schedule.getScheduleId(),
            schedule.getDoctorId()
        };
        jdbcUtil.setSqlAndParameters(query.toString(), params);

        try {
            int result = jdbcUtil.executeUpdate();
            jdbcUtil.commit();
            return result;
        } catch (SQLException ex) {
            jdbcUtil.rollback();
            throw ex;
        } catch (Exception ex) {
            jdbcUtil.rollback();
            throw new SQLException(ex);
        } finally {
            jdbcUtil.close();
        }
    }
}
