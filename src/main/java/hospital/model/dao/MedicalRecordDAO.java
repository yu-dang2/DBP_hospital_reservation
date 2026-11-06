package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import hospital.model.domain.MedicalRecord;
import util.JDBCUtil;

public class MedicalRecordDAO {
    private JDBCUtil jdbcUtil = null;

    public MedicalRecordDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 해당 예약에 진료기록이 이미 있는지 확인
    public boolean existsByReservationId(Long reservationId) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT COUNT(*) AS CNT ");
        query.append("FROM MEDICAL_RECORD ");
        query.append("WHERE RESERVATION_ID = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { reservationId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            if (rs.next()) {
                return rs.getInt("CNT") > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            jdbcUtil.close();
        }

        return false;
    }

    // 진료기록 등록 (등록된 행 수 반환, 기록 일자는 오늘)
    // 예약 1건에 기록 1건 위반(ORA-00001) 등의 DB 오류는 Manager가 처리할 수 있도록 전달
    // 진료완료 상태 확인은 ReservationManager.createMedicalRecord()에서 처리
    public int create(MedicalRecord record) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO MEDICAL_RECORD ");
        query.append("(RECORD_ID, RESERVATION_ID, DIAGNOSIS, PRESCRIPTION, RECORD_DATE) ");
        query.append("VALUES (SEQ_MEDICAL_RECORD.NEXTVAL, ?, ?, ?, SYSDATE) ");

        Object[] params = new Object[] {
            record.getReservation().getReservationId(),
            record.getDiagnosis(),
            record.getPrescription()
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
