package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.Department;
import util.JDBCUtil;

public class DepartmentDAO {
    private JDBCUtil jdbcUtil = null;

    public DepartmentDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 전체 진료과 조회 (없으면 빈 리스트 반환)
    public List<Department> findAll() throws SQLException {
        List<Department> departmentList = new ArrayList<Department>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT DEPT_ID, DEPT_NAME ");
        query.append("FROM DEPARTMENT ");
        query.append("ORDER BY DEPT_ID ");

        jdbcUtil.setSqlAndParameters(query.toString(), null);

        try {
            ResultSet rs = jdbcUtil.executeQuery();
            if (rs == null) {
                throw new SQLException("부서 조회 실패");
            }
            while (rs.next()) {
               departmentList.add(toDepartment(rs));
            }
            return departmentList;
        } finally {
            jdbcUtil.close();
        }
    }

    // 진료과 ID로 특정 진료과 조회 (없으면 null 반환)
    public Department findById(Long deptId) throws SQLException{
        StringBuffer query = new StringBuffer();
        query.append("SELECT DEPT_ID, DEPT_NAME ");
        query.append("FROM DEPARTMENT ");
        query.append("WHERE DEPT_ID = ? ");

        jdbcUtil.setSqlAndParameters(query.toString(), new Object[] { deptId });

        try {
        	ResultSet rs = jdbcUtil.executeQuery();
            if (rs == null) {
                throw new SQLException("진료과 조회 실패: deptId=" + deptId );
            }
            if (rs.next()) {
                return toDepartment(rs);
            }
            return null;
        } finally {
            jdbcUtil.close();
        }
    }

    // 조회한 진료과 데이터를 Department 객체로 변환
    private Department toDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setDeptId(rs.getLong("DEPT_ID"));
        department.setDeptName(rs.getString("DEPT_NAME"));
        return department;
    }
}
