package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hospital.model.domain.DoctorOff;
import hospital.model.domain.OffSource;
import util.JDBCUtil;

public class DoctorOffDAO {
    private JDBCUtil jdbcUtil = null;

    public DoctorOffDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 의사의 휴진 목록 조회 (날짜순, 결과가 없으면 빈 리스트 반환)
    public List<DoctorOff> findByDoctorId(Long doctorId) throws SQLException{
        List<DoctorOff> offList = new ArrayList<DoctorOff>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT OFF_ID, DOCTOR_ID, OFF_DATE, REASON, SOURCE ");
        query.append("FROM DOCTOR_OFF ");
        query.append("WHERE DOCTOR_ID = ? ");
        query.append("ORDER BY OFF_DATE ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { doctorId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            while (rs.next()) {
                offList.add(toDoctorOff(rs));
            }
           return offList;
        } finally {
            jdbcUtil.close();
        }

    }

    // 같은 의사의 같은 날짜에 휴진이 있는지 확인
    public boolean existsByDoctorIdAndDate(Long doctorId, Date offDate) throws SQLException{
        StringBuffer query = new StringBuffer();
        query.append("SELECT COUNT(*) AS CNT ");
        query.append("FROM DOCTOR_OFF ");
        query.append("WHERE DOCTOR_ID = ? AND OFF_DATE = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(),
                new Object[] { doctorId, toDayStart(offDate) });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            if (rs.next()) {
            	throw new SQLException("의사 확인 실패: doctorId=" + doctorId);
            }
            rs.next();
        } finally {
            jdbcUtil.close();
        }

        return false;
    }

    // 휴진 등록 (등록된 행 수 반환)
    // 같은 날짜 중복(ORA-00001) 등의 DB 오류는 Manager가 처리할 수 있도록 전달
    public int create(DoctorOff off) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO DOCTOR_OFF (OFF_ID, DOCTOR_ID, OFF_DATE, REASON, SOURCE) ");
        query.append("VALUES (SEQ_DOCTOR_OFF.NEXTVAL, ?, ?, ?, ?) ");

        Object[] params = new Object[] {
            off.getDoctorId(),
            toDayStart(off.getOffDate()),
            off.getReason(),
            off.getSource().getCode()
        };

        return executeDml(query.toString(), params);
    }

    // 휴진 한 건 조회 (결과가 없으면 null 반환)
    public DoctorOff findById(Long offId) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("SELECT OFF_ID, DOCTOR_ID, OFF_DATE, REASON, SOURCE ");
        query.append("FROM DOCTOR_OFF ");
        query.append("WHERE OFF_ID = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { offId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            if (rs.next()) {
                return toDoctorOff(rs);
            }
            return null;
        
        } finally {
            jdbcUtil.close();
        }
    }

    // 휴진 삭제 (삭제된 행 수 반환)
    // MANUAL 휴진만 삭제하는 확인은 DoctorManager.deleteDoctorOff()에서 처리
    public int delete(Long offId) throws SQLException {
        return executeDml("DELETE FROM DOCTOR_OFF WHERE OFF_ID = ? ", new Object[] { offId });
    }

    // INSERT, DELETE를 실행하고 commit (실패하면 rollback 후 오류 전달)
    private int executeDml(String sql, Object[] params) throws SQLException {
        jdbcUtil.setSqlAndParameters(sql, params);

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

    // 조회 결과의 현재 행을 DoctorOff 객체로 변환
    private DoctorOff toDoctorOff(ResultSet rs) throws SQLException {
        DoctorOff off = new DoctorOff();
        off.setOffId(rs.getLong("OFF_ID"));
        off.setDoctorId(rs.getLong("DOCTOR_ID"));
        // DoctorOff.isOn()의 equals 비교가 동작하도록 java.util.Date로 변환
        off.setOffDate(new Date(rs.getTimestamp("OFF_DATE").getTime()));
        off.setReason(rs.getString("REASON"));
        off.setSource(OffSource.fromCode(rs.getString("SOURCE")));
        return off;
    }

    // 날짜의 시간을 00:00:00으로 맞춰 DB에 넘길 값으로 변환
    private Timestamp toDayStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }
}
