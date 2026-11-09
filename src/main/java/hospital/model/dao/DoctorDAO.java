package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.Doctor;
import util.JDBCUtil;

public class DoctorDAO {
	private JDBCUtil jdbcUtil = null;
	
	public DoctorDAO() {
		jdbcUtil = new JDBCUtil();
		
	}
	
	//전체 의사 목록 조회(없으면 빈 리스트 반환)
	public List<Doctor> findAll() throws SQLException{
		List<Doctor> doctorList = new ArrayList<Doctor>();
		
		StringBuffer query = new StringBuffer();
		query.append("SELECT DOCTOR_ID, NAME, SPECIALTY, BIO, DEPT_ID ");
		query.append("FROM DOCTOR ");
		query.append("ORDER BY NAME ");
		
		jdbcUtil.setSqlAndParameters(query.toString(), null);
		
		try {
			ResultSet rs = jdbcUtil.executeQuery();
			if(rs == null) {
				throw new SQLException("의사 목록 조회 실패함");
			}
			while(rs.next()) {
				doctorList.add(toDoctor(rs));
			}
			return doctorList;
		}finally {
			jdbcUtil.close();
		}
		
	}
		
	//의사 한 명 조회하기
	public Doctor findById(Long doctorId) throws SQLException{
		StringBuffer query = new StringBuffer();
		query.append("SELECT DOCTOR_ID, NAME, SPECIALTY, BIO, DEPT_ID ");
		query.append("FROM DOCTOR ");
		query.append("WHERE DOCTOR_ID = ? ");
		
		jdbcUtil.setSqlAndParameters(query.toString(),new Object[] {doctorId });
		
		ResultSet rs = jdbcUtil.executeQuery();
		
		try {
			if(rs == null) {
				throw new SQLException ("의사 조회 실패: " + doctorId);
			}
			if(rs.next()) {
				return toDoctor(rs);
			}
			return null;
		}finally {
			jdbcUtil.close();					
		}
	}

	//조회 결과 -> Doctor객체로 바꾸기
	private Doctor toDoctor(ResultSet rs) throws SQLException{
		Doctor doctor = new Doctor();
		doctor.setDoctorId(rs.getLong("DOCTOR_ID"));
	doctor.setName(rs.getString("NAME"));
	doctor.setSpecialty(rs.getString("SPECIALTY"));
	doctor.setBio(rs.getString("BIO"));
	doctor.setDeptId(rs.getLong("DEPT_ID"));
	
	return doctor;
	}	
}