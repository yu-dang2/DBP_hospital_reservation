package hospital.test;

import java.util.List;

import hospital.model.dao.DoctorDAO;
import hospital.model.domain.Doctor;

public class DoctorDAOTest {
    public static void main(String[] args) {
        DoctorDAO doctorDAO = new DoctorDAO();

        // 1. 전체 의사 조회
        List<Doctor> doctorList = doctorDAO.findAll();
        System.out.println("전체 의사 수: " + doctorList.size());

        for (Doctor doctor : doctorList) {
            System.out.println(doctor.getDoctorId() + " / " + doctor.getName());
        }

        // 2. 있는 의사 한 명 조회
        if (!doctorList.isEmpty()) {
            Long doctorId = doctorList.get(0).getDoctorId();
            Doctor found = doctorDAO.findById(doctorId);
            System.out.println("조회한 의사: " + found.getName());
        }

        // 3. 없는 의사 조회
        Doctor notFound = doctorDAO.findById(-1L);
        System.out.println("없는 의사 조회 결과: " + notFound);
    }
}