package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.MedicalRecord;
import hospital.model.domain.MedicalRecordSummary;
import hospital.model.domain.ReservationStatus;
import util.JDBCUtil;

public class MedicalRecordDAO {
    private static final int RECENT_LIMIT = 5;   // 마이페이지에 보여줄 최근 기록 수

    private JDBCUtil jdbcUtil = null;

    public MedicalRecordDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 해당 예약에 진료기록이 이미 있는지 확인
    public boolean existsByReservationId(Long reservationId) throws SQLException{
        StringBuffer query = new StringBuffer();
        query.append("SELECT COUNT(*) AS CNT ");
        query.append("FROM MEDICAL_RECORD ");
        query.append("WHERE RESERVATION_ID = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { reservationId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            if (rs == null) {
                throw new SQLException("진료기록 존재 확인 실패: reservationId=" + reservationId);
            }
            rs.next();
            return rs.getInt("CNT") > 0;
        } finally {
            jdbcUtil.close();
        }
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

    // [마이페이지] 회원 본인의 진료완료 예약에 연결된 진료기록을 최근 순으로 최대 5건 조회
    // 다른 회원의 기록은 MEMBER_ID 조건으로 제외, 결과가 없으면 빈 리스트 반환
    // 진료일은 실제 진료한 날인 예약 일시를 사용 (RECORD_DATE는 기록 작성일이며 NULL 가능)
    public List<MedicalRecordSummary> findRecentByMemberId(Long memberId) throws SQLException{
        List<MedicalRecordSummary> recordList = new ArrayList<MedicalRecordSummary>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ( ");
        query.append("    SELECT MR.RECORD_ID, R.RESERVATION_DATE, D.NAME AS DOCTOR_NAME, MR.DIAGNOSIS ");
        query.append("    FROM MEDICAL_RECORD MR ");
        query.append("    JOIN RESERVATION R ON R.RESERVATION_ID = MR.RESERVATION_ID ");
        query.append("    JOIN DOCTOR D      ON D.DOCTOR_ID = R.DOCTOR_ID ");
        query.append("    WHERE R.MEMBER_ID = ? AND R.STATUS = ? ");
        query.append("    ORDER BY R.RESERVATION_DATE DESC, MR.RECORD_ID DESC ");
        query.append(") WHERE ROWNUM <= ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] {
            memberId, ReservationStatus.COMPLETED.getCode(), RECENT_LIMIT });

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            while (rs.next()) {
                recordList.add(new MedicalRecordSummary(
                        rs.getLong("RECORD_ID"),
                        rs.getTimestamp("RESERVATION_DATE").toLocalDateTime().toLocalDate(),
                        rs.getString("DOCTOR_NAME"),
                        rs.getString("DIAGNOSIS")));
            }
            return recordList;    
        } finally {
            jdbcUtil.close();
        }

    }
}
