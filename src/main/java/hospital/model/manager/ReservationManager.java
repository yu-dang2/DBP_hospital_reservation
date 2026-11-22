package hospital.model.manager;

import java.sql.SQLException;
import java.util.List;
import java.util.Date;
import hospital.model.domain.ReservationStatus;

import hospital.model.dao.ReservationDAO;
import hospital.model.domain.Reservation;
import hospital.model.dao.DoctorDAO;
import hospital.model.dao.DoctorScheduleDAO;

import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorSchedule;
import hospital.model.domain.ReservationFormData;

public class ReservationManager {

    private ReservationDAO reservationDAO;
    private DoctorDAO doctorDAO;
    private DoctorScheduleDAO doctorScheduleDAO;

    public ReservationManager() {
        reservationDAO = new ReservationDAO();
        doctorDAO = new DoctorDAO();
        doctorScheduleDAO = new DoctorScheduleDAO();
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
        boolean exists = reservationDAO.existsActiveReservation(
            reservation.getDoctor().getDoctorId(),
            reserveDateTime
        );

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
}