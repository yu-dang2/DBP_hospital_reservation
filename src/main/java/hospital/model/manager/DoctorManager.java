package hospital.model.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hospital.model.dao.DepartmentDAO;
import hospital.model.dao.DoctorDAO;
import hospital.model.dao.DoctorOffDAO;
import hospital.model.dao.DoctorScheduleDAO;
import hospital.model.dao.SymptomKeywordDAO;
import hospital.model.domain.Department;
import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorOff;
import hospital.model.domain.DoctorSchedule;
import hospital.model.domain.OffSource;
import hospital.model.domain.SymptomKeyword;
import hospital.model.domain.SymptomRecommendation;
import hospital.model.exception.DoctorOffNotDeletableException;
import hospital.model.exception.ExistingDoctorOffException;
import hospital.model.exception.InvalidScheduleException;
import hospital.model.exception.InvalidStatusChangeException;
import hospital.model.exception.InvalidSymptomException;

// 진료과·의사 조회 및 증상 기반 진료과 추천 업무 처리
public class DoctorManager {

    private static final int RECOMMEND_LIMIT = 3;

    private static DoctorManager instance = new DoctorManager();

    private DepartmentDAO departmentDAO;
    private DoctorDAO doctorDAO;
    private SymptomKeywordDAO symptomKeywordDAO;
    private DoctorScheduleDAO doctorScheduleDAO;
    private DoctorOffDAO doctorOffDAO;
    

    private DoctorManager() {
        departmentDAO = new DepartmentDAO();
        doctorDAO = new DoctorDAO();
        symptomKeywordDAO = new SymptomKeywordDAO();
        doctorScheduleDAO = new DoctorScheduleDAO();
        doctorOffDAO = new DoctorOffDAO();
        
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
    
    
    /*========관리자 기능 추가 =========*/
    //전체 의사 조회(오버로딩)
    public List<Doctor> findDoctorList()throws SQLException{
    	return doctorDAO.findAll();
    	
    }
    
    public Doctor findDoctor(Long doctorId) throws SQLException{
    	Doctor doctor = doctorDAO.findById(doctorId);
    	//의사 없음
    	if(doctor == null) {
    		return null;
    	}
    	
    	//일정목록
    	List<DoctorSchedule> doctorSchedules= doctorScheduleDAO.findByDoctorId(doctorId);
    	
    	//휴진 목록
    	List<DoctorOff> doctorOffs = doctorOffDAO.findByDoctorId(doctorId);
    	
    	doctor.setSchedules(doctorSchedules);
    	doctor.setOffs(doctorOffs);
    	return doctor;
    }
    
    public void updateSchedule(DoctorSchedule schedule)
            throws SQLException, InvalidScheduleException{
    	
    	// 1. 휴무가 아니면 시간 순서 확인
    	if(!schedule.isDayOff() && schedule.getStartTime().compareTo(schedule.getEndTime()) >= 0){
    		throw new InvalidScheduleException("진료 시작 시간은 종료 시간보다 빨라야 합니다.");
    	}
    	
        // 2. 수정
    	int result = doctorScheduleDAO.update(schedule);
 
    	
        // 3. 0이면 실패		
		if (result == 0)
			throw new InvalidScheduleException("수정할 진료 일정이 없습니다.");

    }  
    
    public void createDoctorOff(DoctorOff off)
            throws SQLException, ExistingDoctorOffException{
    	//같은 날에 휴진이 있는지 확인
    	if(doctorOffDAO.existsByDoctorIdAndDate(off.getDoctorId(), off.getOffDate())){
    		throw new ExistingDoctorOffException("이미 휴진으로 등록된 날짜입니다.");
    	}
    	//등록 경로 정하기
    	off.setSource(OffSource.MANUAL);
    	//등록
    	doctorOffDAO.create(off);   	
    }
    
    public void deleteDoctorOff(Long offId)
            throws SQLException, DoctorOffNotDeletableException{
    	
    	DoctorOff off = doctorOffDAO.findById(offId);
    	
    	if(off == null) {
    		throw new DoctorOffNotDeletableException("존재하지 않는 휴진입니다.");
    	}
    	
    	if(!off.isDeletable()) {
    		throw new DoctorOffNotDeletableException("공휴일로 등록된 휴진은 삭제할 수 없습니다.");
    	}
    	
    	int delete = doctorOffDAO.delete(offId);
    	
    	if(delete == 0) {
    		throw new DoctorOffNotDeletableException("휴진 삭제에 실패했습니다.");
    	}
    }
}
