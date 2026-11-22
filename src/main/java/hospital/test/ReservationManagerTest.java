package hospital.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hospital.model.domain.MedicalRecord;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import hospital.model.exception.ExistingMedicalRecordException;
import hospital.model.exception.InvalidStatusChangeException;
import hospital.model.manager.ReservationManager;
import util.JDBCUtil;

public class ReservationManagerTest {

	public static void main(String[] args) throws Exception {
		ReservationManager rsvMng = new ReservationManager();

		// 1. 날짜 없이 조회
		List<Reservation> reservationList = rsvMng.findReservationList(null, null, null);
		System.out.println("날짜 조회: " + reservationList.size());

		System.out.println();

		// 2. 날짜를 넣어 조회
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date reservedDay = dayFormat.parse("2026-11-16");
		Date emptyDay = dayFormat.parse("2026-11-17");

		System.out.println("예약 있는 날: " + rsvMng.findReservationList(reservedDay, null, null).size());
		System.out.println("예약 없는 날: " + rsvMng.findReservationList(emptyDay, null, null).size());

		System.out.println();

		// 3. 예약 한 건 조회
		List<Reservation> reservationList1 = rsvMng.findReservationList(reservedDay, null, null);
		Long reservationId = reservationList1.get(0).getReservationId();

		System.out.println("예약 한 건 조회: " + rsvMng.findReservation(reservationId).getStatus());

		System.out.println();

		// 상태변경
		try {
			rsvMng.changeStatus(reservationId, ReservationStatus.COMPLETED);
			System.out.println("test1 성공");
		} catch (InvalidStatusChangeException ex) {
			System.out.println("test1 실패: " + ex.getMessage());
		}
		try {
			rsvMng.changeStatus(reservationId, ReservationStatus.NOSHOW);
			System.out.println("test2 성공");
		} catch (InvalidStatusChangeException ex) {
			System.out.println("test2 실패: " + ex.getMessage());
		}
		try {
			rsvMng.changeStatus(reservationId, ReservationStatus.CANCELED);
			System.out.println("test3 성공");
		} catch (InvalidStatusChangeException ex) {
			System.out.println("test3 실패: " + ex.getMessage());
		}
		try {
			rsvMng.changeStatus(-1L, ReservationStatus.COMPLETED);
			System.out.println("test4 성공");
		} catch (InvalidStatusChangeException ex) {
			System.out.println("test4 실패: " + ex.getMessage());
		}

		restoreStatus(reservationId);

		// 진료기록 등록
		Reservation reservation = new Reservation();
		reservation.setReservationId(reservationId);
		MedicalRecord record = new MedicalRecord();
		record.setReservation(reservation);
		record.setDiagnosis("테스트진단");
		record.setPrescription("테스트처방");

		try {
			rsvMng.createMedicalRecord(record);
			System.out.println("test5-1 성공");

		} catch (ExistingMedicalRecordException ex) {
			System.out.println("test5-1 실패: " + ex.getMessage());
		}

		rsvMng.changeStatus(reservationId, ReservationStatus.COMPLETED);

		try {
			rsvMng.createMedicalRecord(record);
			System.out.println("test5-2 성공");

		} catch (ExistingMedicalRecordException ex) {
			System.out.println("test5-2 실패: " + ex.getMessage());
		}

		try {
			rsvMng.createMedicalRecord(record);
			System.out.println("test5-3 성공");

		} catch (ExistingMedicalRecordException ex) {
			System.out.println("test5-3 실패: " + ex.getMessage());
		}

		deleteTestRecord(reservationId);
		restoreStatus(reservationId);

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
