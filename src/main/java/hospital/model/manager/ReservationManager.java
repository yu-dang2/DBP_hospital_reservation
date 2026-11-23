package hospital.model.manager;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;
import hospital.model.domain.ReservationStatus;
import hospital.model.exception.ExistingMedicalRecordException;
import hospital.model.exception.InvalidStatusChangeException;
import hospital.model.dao.ReservationDAO;
import hospital.model.domain.Reservation;
import hospital.model.dao.DoctorDAO;
import hospital.model.dao.DoctorScheduleDAO;
import hospital.model.dao.MedicalRecordDAO;
import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorSchedule;
import hospital.model.domain.MedicalRecord;
import hospital.model.domain.ReservationFormData;

public class ReservationManager {

	private ReservationDAO reservationDAO;
	private DoctorDAO doctorDAO;
	private DoctorScheduleDAO doctorScheduleDAO;
	private MedicalRecordDAO medicalRecordDAO;

	public ReservationManager() {
		reservationDAO = new ReservationDAO();
		doctorDAO = new DoctorDAO();
		doctorScheduleDAO = new DoctorScheduleDAO();
		medicalRecordDAO = new MedicalRecordDAO();

	}

	// 회원의 전체 예약 내역 조회
	public List<Reservation> findReservationsByMemberId(Long memberId) throws SQLException {
		return reservationDAO.findByMemberId(memberId);
	}

	// 본인 예약인지 확인 -> 상세조회
	public Reservation findReservationById(Long reservationId, Long memberId) throws SQLException {
		Reservation reservation = reservationDAO.findById(reservationId);

		if (reservation == null) {
			return null;
		}

		if (!reservation.getMember().getMemberId().equals(memberId)) {
			return null;
		}

		return reservation;
	}

	// 특정 의료진의 예약 시간 중복 여부 확인
	public boolean existsActiveReservation(Long doctorId, Date reservationDate) throws SQLException {
		return reservationDAO.existsActiveReservation(doctorId, reservationDate);

	}

	// 예약 등록
	public int createReservation(Reservation reservation) throws SQLException {

		Date reserveDateTime = reservation.getReservationDate();

		if (reserveDateTime == null || !reserveDateTime.after(new Date())) {
			return 0;
		}
		boolean exists = reservationDAO.existsActiveReservation(reservation.getDoctor().getDoctorId(), reserveDateTime);

		if (exists) {
			return 0;
		}

		return reservationDAO.create(reservation);
	}

	// 예약 변경 -> DAO 재사용
	public int updateReservation(Long reservationId, Long memberId, Date reserveDateTime) throws SQLException {
		Reservation reservation = reservationDAO.findById(reservationId);

		if (reservation == null) {
			return 0;
		}

		if (!reservation.getMember().getMemberId().equals(memberId)) {
			return 0;
		}

		if (reservation.getStatus() != ReservationStatus.RESERVED) {
			return 0;
		}

		// 변경하려는 예약 시간이 현재보다 미래인지 확인(추가)
		if (reserveDateTime == null || !reserveDateTime.after(new Date())) {
			return 0;
		}

		reservation.changeSchedule(reserveDateTime);
		return reservationDAO.update(reservation);
	}

	// 예약 취소
	public int cancelReservation(Long reservationId, Long memberId) throws SQLException {

		Reservation reservation = reservationDAO.findById(reservationId);

		if (reservation == null) {
			return 0;
		}

		if (!reservation.getMember().getMemberId().equals(memberId)) {
			return 0;
		}

		if (!reservation.isCancelable()) { // 취소 가능 여부 확인용
			return 0;
		}

		return reservationDAO.cancel(reservationId);
	}

	// 회원의 예정된 예약 내역 조회
	public List<Reservation> getUpcomingReservations(Long memberId) throws SQLException {
		return reservationDAO.findUpcomingByMemberId(memberId);

	}

	public List<Reservation> getRecentReservations(Long memberId) throws SQLException {
		return reservationDAO.findRecentByMemberId(memberId);

	}

	// Doctor와 연결됨 -> 예약 등록에 필요한 의사 정보 및 진료 일정 조회하기
	public ReservationFormData findReservationFormData(Long doctorId) throws SQLException {

		Doctor doctor = doctorDAO.findById(doctorId);

		if (doctor == null) {
			return null;
		}

		// 의사 일정 조회
		List<DoctorSchedule> schedules = doctorScheduleDAO.findByDoctorId(doctorId);

		ReservationFormData formData = new ReservationFormData();
		formData.setDoctor(doctor);
		formData.setSchedules(schedules);

		return formData;
	}

	/* =======관리자 기능======= */

	// 예약리스트
	public List<Reservation> findReservationList(Date date, Long doctorId, ReservationStatus status)
			throws SQLException {
		if (date == null)
			date = new Date();
		return reservationDAO.search(date, doctorId, status);
	}

	// 예약 한 건 조회
	public Reservation findReservation(Long reservationId) throws SQLException {
		return reservationDAO.findById(reservationId);
	}

	// 예약 상태변경
	public void changeStatus(Long reservationId, ReservationStatus status)
			throws SQLException, InvalidStatusChangeException {
		// 예약 찾기
		Reservation reservation = findReservation(reservationId);

		// 없으면 실패안내
		if (reservation == null)
			throw new InvalidStatusChangeException("존재하지 않는 예약입니다.");

		// 바꿀 상태 확인
		if ((status != ReservationStatus.COMPLETED) && (status != ReservationStatus.NOSHOW))
			throw new InvalidStatusChangeException("진료완료 또는 노쇼만 변경 가능합니다.");

		// 지금 상태
		ReservationStatus nowStatus = reservation.getStatus();
		if (nowStatus != ReservationStatus.RESERVED)
			throw new InvalidStatusChangeException("이미 처리된 예약입니다.");

		// 변경
		int result = reservationDAO.updateStatus(reservationId, status);

		// 0이면 실패 -> 예외 던지기
		if (result == 0)
			throw new InvalidStatusChangeException("상태 변경에 실패했습니다.");

	}

	public void createMedicalRecord(MedicalRecord record) throws SQLException, ExistingMedicalRecordException {
		Long reservationId = record.getReservation().getReservationId();
		Reservation reservation = findReservation(reservationId);

		// 예약찾기
		if (reservation == null) {
			throw new ExistingMedicalRecordException("예약이 존재하지 않습니다.");
		}

		// 진료완료 확인
		ReservationStatus nowStatus = reservation.getStatus();

		if (nowStatus != ReservationStatus.COMPLETED) {
			throw new ExistingMedicalRecordException("진료완료된 예약에만 진료기록을 등록할 수 있습니다.");
		}

		// 중복확인
		boolean exists = medicalRecordDAO.existsByReservationId(reservationId);
		if (exists) {
			throw new ExistingMedicalRecordException("이미 진료기록이 등록된 예약입니다.");
		}
		// 등록 (동시에 등록되어 UNIQUE 제약에 걸린 경우도 중복으로 처리)
		try{
			medicalRecordDAO.create(record);
		}catch(SQLException ex) {
			if(ex.getErrorCode() == 1) {
				throw new ExistingMedicalRecordException("이미 진료기록이 등록된 예약입니다.");
			}
			else {
				throw ex;
			}
		}


		// 등록
		medicalRecordDAO.create(record);
	}
}
