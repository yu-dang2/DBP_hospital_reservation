package hospital.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hospital.model.dao.ReservationDAO;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import util.JDBCUtil;

public class ReservationDAOTest {
    public static void main(String[] args) throws Exception {
        ReservationDAO reservationDAO = new ReservationDAO();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date reservedDay = dayFormat.parse("2026-11-16");
        Date emptyDay = dayFormat.parse("2026-11-17");

        // 1. 조건 없이 목록 조회
        List<Reservation> allList = reservationDAO.search(null, null, null);
        System.out.println("1. 조건 없는 목록 수: " + allList.size());

        Long reservationId = allList.get(0).getReservationId();
        Long doctorId = allList.get(0).getDoctor().getDoctorId();

        // 2. 조건을 넣어 목록 조회
        System.out.println("2. 조건 3개 일치 목록 수: "
                + reservationDAO.search(reservedDay, doctorId, ReservationStatus.RESERVED).size());
        System.out.println("3. 없는 상태 목록 수: "
                + reservationDAO.search(reservedDay, doctorId, ReservationStatus.CANCELED).size());

        // 3. 한 건 조회
        Reservation found = reservationDAO.findById(reservationId);
        System.out.println("4. 예약 정보: " + found.getMember().getName()
                + " / " + found.getDoctor().getName()
                + " / " + timeFormat.format(found.getReservationDate())
                + " / " + found.getDoctorSchedule().getStartTime()
                + " / " + found.getStatus());
        System.out.println("5. 없는 예약 조회: " + reservationDAO.findById(-1L));

        // 4. 날짜별 예약 존재 확인
        System.out.println("6. 예약 있는 날: "
                + reservationDAO.existsReservedByDoctorIdAndDate(doctorId, reservedDay));
        System.out.println("7. 예약 없는 날: "
                + reservationDAO.existsReservedByDoctorIdAndDate(doctorId, emptyDay));

        // 5. 상태 변경
        System.out.println("8. 상태 변경 행 수: "
                + reservationDAO.updateStatus(reservationId, ReservationStatus.COMPLETED));
        System.out.println("9. 다시 변경 행 수: "
                + reservationDAO.updateStatus(reservationId, ReservationStatus.NOSHOW));
        System.out.println("10. 변경 후 상태: " + reservationDAO.findById(reservationId).getStatus());

        // 테스트가 끝나면 상태 복구
        restoreStatus(reservationId);
        System.out.println("11. 복구 후 상태: " + reservationDAO.findById(reservationId).getStatus());
    }

    // 상태 복구 (DAO에 없는 동작이라 테스트에서만 직접 실행)
    private static void restoreStatus(Long reservationId) throws Exception {
        JDBCUtil jdbcUtil = new JDBCUtil();
        jdbcUtil.setSqlAndParameters("UPDATE RESERVATION SET STATUS = 'R' WHERE RESERVATION_ID = ?",
                new Object[] { reservationId });
        try {
            jdbcUtil.executeUpdate();
            jdbcUtil.commit();
        } finally {
            jdbcUtil.close();
        }
    }
}
