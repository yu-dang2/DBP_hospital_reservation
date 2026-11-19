package hospital.model.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter

// 마이페이지 화면에 전달할 조회 결과.
// 회원 정보, 최근 예약 목록, 최근 진료기록 목록을 포함

public class MyPageResult {

    private Member member;
    private List<ReservationSummary> recentReservations = new ArrayList<>();
    private List<MedicalRecordSummary> recentRecords = new ArrayList<>();

    //기본 생성자
    public MyPageResult() {
    }

    public MyPageResult(Member member, List<ReservationSummary> recentReservations,
                        List<MedicalRecordSummary> recentRecords) {
        this.member = member;
        this.recentReservations = recentReservations != null ? recentReservations : new ArrayList<>();
        this.recentRecords = recentRecords != null ? recentRecords : new ArrayList<>();
    }
}