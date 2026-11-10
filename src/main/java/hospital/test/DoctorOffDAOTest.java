package hospital.test;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hospital.model.dao.DoctorDAO;
import hospital.model.dao.DoctorOffDAO;
import hospital.model.domain.DoctorOff;
import hospital.model.domain.OffSource;

public class DoctorOffDAOTest {
    public static void main(String[] args) throws Exception {
        DoctorOffDAO offDAO = new DoctorOffDAO();
        Long doctorId = new DoctorDAO().findAll().get(0).getDoctorId();
        Date offDate = new SimpleDateFormat("yyyy-MM-dd").parse("2026-12-25");

        // 1. 등록 전 중복 확인
        System.out.println("1. 등록 전 중복 확인: "
                + offDAO.existsByDoctorIdAndDate(doctorId, offDate));

        // 2. 휴진 등록
        DoctorOff off = new DoctorOff();
        off.setDoctorId(doctorId);
        off.setOffDate(offDate);
        off.setReason("테스트휴진");
        off.setSource(OffSource.MANUAL);
        System.out.println("2. 등록된 행 수: " + offDAO.create(off));

        // 3. 등록 후 중복 확인
        System.out.println("3. 등록 후 존재 여부: "
                + offDAO.existsByDoctorIdAndDate(doctorId, offDate));

        // 4. 같은 날짜 다시 등록: DB 제약조건 오류가 나야 정상
        try {
            offDAO.create(off);
            System.out.println("4. 실패: 오류가 나지 않았습니다.");
        } catch (SQLException ex) {
            System.out.println("4. 예상한 오류 코드: ORA-" + ex.getErrorCode());
        }

        // 5. 목록 조회
        DoctorOff saved = null;
        for (DoctorOff item : offDAO.findByDoctorId(doctorId)) {
            if (item.isOn(offDate)) {
                saved = item;
            }
        }
        System.out.println("5. 목록에서 찾은 휴진 사유: " + saved.getReason());

        // 6. 한 건 조회와 도메인 메서드 확인
        DoctorOff found = offDAO.findById(saved.getOffId());
        System.out.println("6. 삭제 가능 여부: " + found.isDeletable()
                + ", 날짜 일치: " + found.isOn(offDate));

        // 7. 삭제 후 다시 조회
        System.out.println("7. 삭제된 행 수: " + offDAO.delete(saved.getOffId()));
        System.out.println("8. 삭제 후 조회 결과: " + offDAO.findById(saved.getOffId()));
    }
}
