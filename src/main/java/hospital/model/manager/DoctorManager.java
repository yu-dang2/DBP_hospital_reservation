package hospital.model.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hospital.model.dao.DepartmentDAO;
import hospital.model.dao.DoctorDAO;
import hospital.model.dao.SymptomKeywordDAO;
import hospital.model.domain.Department;
import hospital.model.domain.Doctor;
import hospital.model.domain.SymptomKeyword;
import hospital.model.domain.SymptomRecommendation;
import hospital.model.exception.InvalidSymptomException;

// 진료과·의사 조회 및 증상 기반 진료과 추천 업무 처리
public class DoctorManager {

    private static final int RECOMMEND_LIMIT = 3;

    private static DoctorManager instance = new DoctorManager();

    private DepartmentDAO departmentDAO;
    private DoctorDAO doctorDAO;
    private SymptomKeywordDAO symptomKeywordDAO;

    private DoctorManager() {
        departmentDAO = new DepartmentDAO();
        doctorDAO = new DoctorDAO();
        symptomKeywordDAO = new SymptomKeywordDAO();
    }

    public static DoctorManager getInstance() {
        return instance;
    }

    // 진료과·의사 조회 및 증상 기반 진료과 추천

    // 전체 진료과 목록 조회
    public List<Department> findDepartmentList() throws SQLException {
        return departmentDAO.findAll();
    }

    // 진료과 조회
    public Department findDepartment(Long deptId) throws SQLException {
        if (deptId == null) {
            return null;
        }
        return departmentDAO.findById(deptId);
    }

    // 의사 목록 조회
    public List<Doctor> findDoctorList(Long deptId) throws SQLException {
        if (deptId == null) {
            return new ArrayList<Doctor>();
        }
        return doctorDAO.findByDeptId(deptId);
    }

    // 검색어가 포함된 의사 목록 조회
    public List<Doctor> searchDoctorList(String keyword) throws SQLException {
        if (isBlank(keyword)) {
            return new ArrayList<Doctor>();
        }
        return doctorDAO.findByKeyword(keyword.trim());
    }

    // 증상 문장에 포함된 키워드의 가중치를 진료과별로 합산해 점수 내림차순 상위 3개 반환
    // 동점 시 진료과 가나다 순
    public List<SymptomRecommendation> recommendDepartments(String symptom)
            throws SQLException, InvalidSymptomException {
        if (isBlank(symptom)) {
            throw new InvalidSymptomException();
        }
        String sentence = symptom.trim();

        // 입력 문장과 일치하는 증상 키워드를 찾아 진료과별로 점수&키워드 누적
        Map<Long, SymptomRecommendation> byDept = new HashMap<Long, SymptomRecommendation>();
        for (SymptomKeyword keyword : symptomKeywordDAO.findAll()) {
            if (!keyword.matches(sentence)) {
                continue;
            }
            SymptomRecommendation rec = byDept.get(keyword.getDeptId());
            if (rec == null) {
                rec = new SymptomRecommendation();
                byDept.put(keyword.getDeptId(), rec);
            }
            rec.addMatch(keyword.getKeyword(), keyword.getWeight());
        }

        if (byDept.isEmpty()) {
            return new ArrayList<SymptomRecommendation>();
        }

        // 추천 진료과 정보 채우기&반환
        for (Department dept : departmentDAO.findAll()) {
            SymptomRecommendation rec = byDept.get(dept.getDeptId());
            if (rec != null) {
                rec.setDepartment(dept);
            }
        }

        List<SymptomRecommendation> result = new ArrayList<SymptomRecommendation>(byDept.values());
        Collections.sort(result);
        if (result.size() > RECOMMEND_LIMIT) {
            result = new ArrayList<SymptomRecommendation>(result.subList(0, RECOMMEND_LIMIT));
        }
        return result;
    }

    // 입력값이 null or 공백이면 true 반환
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
