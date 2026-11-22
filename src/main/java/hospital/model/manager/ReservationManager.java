package hospital.model.manager;

import java.sql.SQLException;
import java.util.List;

import hospital.model.dao.MedicalRecordDAO;
import hospital.model.dao.ReservationDAO;
import hospital.model.domain.MedicalRecord;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import hospital.model.exception.ExistingMedicalRecordException;
import hospital.model.exception.InvalidStatusChangeException;

import java.util.Date;

public class ReservationManager {
	
	
	
	
	
	
	/*==================관리자 파트========================*/
	private ReservationDAO rsvDao = new ReservationDAO();
	private MedicalRecordDAO mdDao = new MedicalRecordDAO();
	
	
	//예약리스트
	public List<Reservation> findReservationList(Date date, Long doctorId, ReservationStatus status) throws SQLException{
		if(date == null)
			date = new Date();
		return rsvDao.search(date, doctorId, status);
	}
	//예약 한 건 조회
	public Reservation findReservation(Long reservationId)throws SQLException {
		return rsvDao.findById(reservationId);
	}
	
	//예약 상태변경
	public void changeStatus(Long reservationId, ReservationStatus status)throws SQLException, InvalidStatusChangeException {
		//예약 찾기
		 Reservation reservation = findReservation(reservationId);
		
		//없으면 실패안내
		 if(reservation == null)
			 throw new InvalidStatusChangeException("존재하지 않는 예약입니다.");
		
		//바꿀 상태 확인
		 if((status != ReservationStatus.COMPLETED) && (status != ReservationStatus.NOSHOW))
			 throw new InvalidStatusChangeException("진료완료 또는 노쇼만 변경 가능합니다.");
		
		//지금 상태
		 ReservationStatus nowStatus = reservation.getStatus();
		 if(nowStatus != ReservationStatus.RESERVED)
			 throw new InvalidStatusChangeException("이미 처리된 예약입니다.");
		
		//변경
		 int result = rsvDao.updateStatus(reservationId, status);
		 
	
		//0이면 실패 -> 예외 던지기
		 if(result == 0)
			 throw new InvalidStatusChangeException("상태 변경에 실패했습니다.");
	
	}
	
	
	
	public void createMedicalRecord(MedicalRecord record) throws SQLException, ExistingMedicalRecordException{
		
		
		
		
	}

}
