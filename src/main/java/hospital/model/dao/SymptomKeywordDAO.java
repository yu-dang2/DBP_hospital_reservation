package hospital.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hospital.model.domain.SymptomKeyword;
import util.JDBCUtil;

public class SymptomKeywordDAO {
    private JDBCUtil jdbcUtil = null;

    public SymptomKeywordDAO() {
        jdbcUtil = new JDBCUtil();
    }

    // 증상 키워드 전체를 진료과 ID, 가중치와 함께 조회 (결과가 없으면 빈 리스트 반환)
    // 증상 문장과 비교 & 진료과별 점수 합산은 DoctorManager.recommendDepartments()에서 처리
    public List<SymptomKeyword> findAll() throws SQLException{
        List<SymptomKeyword> keywordList = new ArrayList<SymptomKeyword>();

        StringBuffer query = new StringBuffer();
        query.append("SELECT KEYWORD_ID, KEYWORD, WEIGHT, DEPT_ID ");
        query.append("FROM SYMPTOM_KEYWORD ");
        query.append("ORDER BY KEYWORD_ID ");

        jdbcUtil.setSqlAndParameters(query.toString(), null);

        try {
            ResultSet rs = jdbcUtil.executeQuery();

            while (rs.next()) {
                keywordList.add(toSymptomKeyword(rs));
            }
         return keywordList;
        } finally {
            jdbcUtil.close();
        }
    }

    // 조회한 증상 키워드 데이터를 SymptomKeyword 객체로 변환
    private SymptomKeyword toSymptomKeyword(ResultSet rs) throws SQLException {
        SymptomKeyword keyword = new SymptomKeyword();
        keyword.setKeywordId(rs.getLong("KEYWORD_ID"));
        keyword.setKeyword(rs.getString("KEYWORD"));
        keyword.setWeight(rs.getInt("WEIGHT"));
        keyword.setDeptId(rs.getLong("DEPT_ID"));
        return keyword;
    }
}
