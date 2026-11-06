package hospital.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import hospital.model.dao.MedicalRecordDAO;
import hospital.model.domain.MedicalRecord;
import hospital.model.domain.Reservation;
import util.JDBCUtil;

public class MedicalRecordDAOTest {
    public static void main(String[] args) throws Exception {
        MedicalRecordDAO recordDAO = new MedicalRecordDAO();
        Long reservationId = findTestReservationId();
        deleteTestRecord(reservationId);   // 이전 실행에서 남은 기록 정리

        // 1. 등록 전 확인
        System.out.println("1. 등록 전 존재 여부: "
                + recordDAO.existsByReservationId(reservationId));

        // 2. 진료기록 등록
        Reservation reservation = new Reservation();
        reservation.setReservationId(reservationId);

        MedicalRecord record = new MedicalRecord();
        record.setReservation(reservation);
        record.setDiagnosis("테스트진단");
        record.setPrescription("테스트처방");
        System.out.println("2. 등록된 행 수: " + recordDAO.create(record));

        // 3. 등록 후 확인
        System.out.println("3. 등록 후 존재 여부: "
                + recordDAO.existsByReservationId(reservationId));

        // 4. 같은 예약에 다시 등록: DB 제약조건 오류가 나야 정상
        try {
            recordDAO.create(record);
            System.out.println("4. 실패: 오류가 나지 않았습니다.");
        } catch (SQLException ex) {
            System.out.println("4. 예상한 오류 코드: ORA-" + ex.getErrorCode());
        }

        deleteTestRecord(reservationId);   // 테스트 기록 정리
        System.out.println("5. 정리 후 존재 여부: "
                + recordDAO.existsByReservationId(reservationId));
    }

    // 테스트 예약 번호 찾기
    private static Long findTestReservationId() throws Exception {
        JDBCUtil jdbcUtil = new JDBCUtil();
        jdbcUtil.setSqlAndParameters("SELECT MIN(RESERVATION_ID) AS ID FROM RESERVATION", null);
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            rs.next();
            return rs.getLong("ID");
        } finally {
            jdbcUtil.close();
        }
    }

    // 테스트 기록 삭제 (DAO에 삭제 메서드가 없어 테스트에서만 직접 실행)
    private static void deleteTestRecord(Long reservationId) throws Exception {
        JDBCUtil jdbcUtil = new JDBCUtil();
        jdbcUtil.setSqlAndParameters("DELETE FROM MEDICAL_RECORD WHERE RESERVATION_ID = ?",
                new Object[] { reservationId });
        try {
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        } finally {
            jdbcUtil.close();
        }
    }
}
