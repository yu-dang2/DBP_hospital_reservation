package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.Doctor;
import util.JDBCUtil;

public class DoctorDAO {
    // 의사 정보 + 소속 진료과명 (DEPT_ID는 NOT NULL FK라 INNER JOIN)
    private static final String SELECT_DOCTOR_COLUMNS =
            "SELECT d.DOCTOR_ID, d.NAME, d.SPECIALTY, d.BIO, d.DEPT_ID, dept.DEPT_NAME "
            + "FROM DOCTOR d "
            + "JOIN DEPARTMENT dept ON d.DEPT_ID = dept.DEPT_ID ";

    private JDBCUtil jdbcUtil = null;
    
    public DoctorDAO() {
        jdbcUtil = new JDBCUtil();
    }
    
    // 전체 의사 목록 조회 (없으면 빈 리스트 반환)
    public List<Doctor> findAll() throws SQLException {
        List<Doctor> doctorList = new ArrayList<Doctor>();
        
        StringBuffer query = new StringBuffer();
        query.append(SELECT_DOCTOR_COLUMNS);
        query.append("ORDER BY d.NAME ");
        
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
        } finally {
            jdbcUtil.close();
        }
    }
        
    // 의사 한 명 조회하기
    public Doctor findById(Long doctorId) throws SQLException {
        StringBuffer query = new StringBuffer();
        query.append(SELECT_DOCTOR_COLUMNS);
        query.append("WHERE d.DOCTOR_ID = ? ");
        
        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { doctorId });
        
        try {
            ResultSet rs = jdbcUtil.executeQuery(); 
            if(rs == null) {
                throw new SQLException ("의사 조회 실패: " + doctorId);
            }
            if(rs.next()) {
                return toDoctor(rs);
            }
            return null;
        } finally {
            jdbcUtil.close();                 
        }
    }

    // 의사 이름 또는 진료과명으로 의사 검색
    public List<Doctor> findByKeyword(String keyword) throws SQLException {
        List<Doctor> doctorList = new ArrayList<Doctor>();
        
        StringBuffer query = new StringBuffer();
        query.append(SELECT_DOCTOR_COLUMNS);
        query.append("WHERE d.NAME LIKE ? OR dept.DEPT_NAME LIKE ? ");
        query.append("ORDER BY d.NAME ");
        
        String searchKeyword = "%" + keyword + "%";
        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { searchKeyword, searchKeyword });
        
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if(rs == null) {
                throw new SQLException("의사 검색 실패 (키워드: " + keyword + ")");
            }
            while(rs.next()) {
                doctorList.add(toDoctor(rs));
            }
            return doctorList;
        } finally {
            jdbcUtil.close();
        }
    }

    // 진료과별 의사 목록 조회
    public List<Doctor> findByDeptId(Long deptId) throws SQLException {
        List<Doctor> doctorList = new ArrayList<Doctor>();
        
        StringBuffer query = new StringBuffer();
        query.append(SELECT_DOCTOR_COLUMNS);
        query.append("WHERE d.DEPT_ID = ? ");
        query.append("ORDER BY d.NAME ");
        
        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { deptId });
        
        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if(rs == null) {
                throw new SQLException("진료과별 의사 목록 조회 실패 (부서 ID: " + deptId + ")");
            }
            while(rs.next()) {
                doctorList.add(toDoctor(rs));
            }
            return doctorList;
        } finally {
            jdbcUtil.close();
        }
    }

    // 조회 결과 -> Doctor 객체로 바꾸기
    private Doctor toDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getLong("DOCTOR_ID"));
        doctor.setName(rs.getString("NAME"));
        doctor.setSpecialty(rs.getString("SPECIALTY"));
        doctor.setBio(rs.getString("BIO"));
        doctor.setDeptId(rs.getLong("DEPT_ID"));
        doctor.setDepartmentName(rs.getString("DEPT_NAME"));
        
        return doctor;
    } 
}