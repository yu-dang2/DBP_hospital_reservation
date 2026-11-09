package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorSchedule;
import hospital.model.domain.Member;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import util.JDBCUtil;

public class ReservationDAO {
    private JDBCUtil jdbcUtil = null;

    public ReservationDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 예약 한 건 조회 (결과가 없으면 null 반환)
    public Reservation findById(Long reservationId) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("SELECT R.RESERVATION_ID, R.RESERVATION_DATE, R.STATUS, R.CREATED_AT, R.CANCELED_AT, ");
        query.append("       M.MEMBER_ID, M.NAME AS MEMBER_NAME, ");
        query.append("       D.DOCTOR_ID, D.NAME AS DOCTOR_NAME, ");
        query.append("       S.SCHEDULE_ID, S.START_TIME, S.END_TIME ");
        query.append("FROM RESERVATION R ");
        query.append("JOIN MEMBER M          ON R.MEMBER_ID = M.MEMBER_ID ");
        query.append("JOIN DOCTOR D          ON R.DOCTOR_ID = D.DOCTOR_ID ");
        query.append("JOIN DOCTOR_SCHEDULE S ON R.SCHEDULE_ID = S.SCHEDULE_ID ");
        query.append("WHERE R.RESERVATION_ID = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { reservationId });

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            
            if(rs == null) {
            	throw new SQLException ("예약 조회 실패: reservationId=" + reservationId);
            }

            if (rs.next()) {
                Reservation reservation = toReservation(rs);
                reservation.setCreatedAt(toDate(rs.getTimestamp("CREATED_AT")));
                reservation.setCanceledAt(toDate(rs.getTimestamp("CANCELED_AT")));

                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setScheduleId(rs.getLong("SCHEDULE_ID"));
                schedule.setStartTime(rs.getString("START_TIME"));
                schedule.setEndTime(rs.getString("END_TIME"));
                reservation.setDoctorSchedule(schedule);

                return reservation;
            }
            return null;     
        } finally {
            jdbcUtil.close();
        }

    }

    // 예약 상태 변경 (RESERVED 상태인 예약만 변경, 변경된 행 수 반환)
    public int updateStatus(Long reservationId, ReservationStatus status) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append("UPDATE RESERVATION ");
        query.append("SET STATUS = ? ");
        query.append("WHERE RESERVATION_ID = ? AND STATUS = 'R' ");

        jdbcUtil.setSqlAndParameters(query.toString(),
                new Object[] { status.getCode(), reservationId });

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

    // 관리자 예약 목록 조회 (null이 아닌 조건만 적용, 결과가 없으면 빈 리스트 반환)
    // 목록용 조회라 createdAt, canceledAt, doctorSchedule은 채우지 않음
    public List<Reservation> search(Date date, Long doctorId, ReservationStatus status) throws SQLException{
        List<Reservation> reservationList = new ArrayList<Reservation>();
        List<Object> params = new ArrayList<Object>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT R.RESERVATION_ID, R.RESERVATION_DATE, R.STATUS, ");
        query.append("       M.MEMBER_ID, M.NAME AS MEMBER_NAME, ");
        query.append("       D.DOCTOR_ID, D.NAME AS DOCTOR_NAME ");
        query.append("FROM RESERVATION R ");
        query.append("JOIN MEMBER M ON R.MEMBER_ID = M.MEMBER_ID ");
        query.append("JOIN DOCTOR D ON R.DOCTOR_ID = D.DOCTOR_ID ");
        query.append("WHERE 1 = 1 ");   // 뒤의 조건을 항상 AND로 이어 붙이기 위한 조건

        if (date != null) {
            query.append("AND R.RESERVATION_DATE >= ? AND R.RESERVATION_DATE < ? ");
            params.add(toDayStart(date, 0));
            params.add(toDayStart(date, 1));
        }
        if (doctorId != null) {
            query.append("AND R.DOCTOR_ID = ? ");
            params.add(doctorId);
        }
        if (status != null) {
            query.append("AND R.STATUS = ? ");
            params.add(status.getCode());
        }
        query.append("ORDER BY R.RESERVATION_DATE, R.RESERVATION_ID ");

        jdbcUtil.setSqlAndParameters(query.toString(), params.toArray());

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs == null) {
                throw new SQLException("예약 목록 조회 실패");
            }

            while (rs.next()) {
                reservationList.add(toReservation(rs));
            }
            return reservationList;
     
        } finally {
            jdbcUtil.close();
        }

    }

    // TODO: 예약이 있는 날짜의 휴진 등록 정책이 정해지면 사용 여부 확정 (FR-34)
    // 해당 날짜에 처리되지 않은 예약이 있는지 확인
    public boolean existsReservedByDoctorIdAndDate(Long doctorId, Date date) throws SQLException{
        StringBuffer query = new StringBuffer();
        query.append("SELECT COUNT(*) AS CNT ");
        query.append("FROM RESERVATION ");
        query.append("WHERE DOCTOR_ID = ? ");
        query.append("  AND RESERVATION_DATE >= ? AND RESERVATION_DATE < ? ");
        query.append("  AND STATUS = 'R' ");

        jdbcUtil.setSqlAndParameters(query.toString(),
                new Object[] { doctorId, toDayStart(date, 0), toDayStart(date, 1) });
        try{        	
        	ResultSet rs = jdbcUtil.executeQuery();
        	if (rs == null) {                         
        		throw new SQLException("휴진일 예약 확인 실패: doctorId=" + doctorId);
        	}
        	rs.next();
        	return rs.getInt("CNT") > 0;
        
        	}finally {                                            
        		jdbcUtil.close();
        	}
    	}

    // findById와 search가 함께 조회하는 컬럼을 Reservation 객체로 변환
    private Reservation toReservation(ResultSet rs) throws SQLException {
        // TODO: 일반 회원 클래스가 정해지면 교체 (Member가 추상 클래스라 임시로 익명 클래스 사용)
        Member member = new Member() {
            @Override
            public boolean isAdmin() {
                return false;
            }
        };
        member.setMemberId(rs.getLong("MEMBER_ID"));
        member.setName(rs.getString("MEMBER_NAME"));

        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getLong("DOCTOR_ID"));
        doctor.setName(rs.getString("DOCTOR_NAME"));

        Reservation reservation = new Reservation();
        reservation.setReservationId(rs.getLong("RESERVATION_ID"));
        reservation.setReservationDate(toDate(rs.getTimestamp("RESERVATION_DATE")));
        reservation.setStatus(ReservationStatus.fromCode(rs.getString("STATUS")));
        reservation.setMember(member);
        reservation.setDoctor(doctor);
        return reservation;
    }

    // DB 값을 java.util.Date로 변환 (값이 없으면 null)
    private Date toDate(Timestamp timestamp) {
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    // 날짜를 00:00:00으로 맞춘 값 반환 (plusDays = 1이면 다음 날 00:00:00)
    private Timestamp toDayStart(Date date, int plusDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, plusDays);
        return new Timestamp(calendar.getTimeInMillis());
    }
}
