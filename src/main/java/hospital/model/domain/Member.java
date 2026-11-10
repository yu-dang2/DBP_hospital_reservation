package hospital.model.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Member {

    private Long memberId;
    private String userId;
    private String password;
    private String name;
    private Date birth;
    private String phone;
    private String email;
    private MemberRole role;
    private MemberStatus status;
    private Date createdAt;

    // 주요 메서드1: 회원의 기본 정보 수정
    public void updateProfile(String name, Date birth, String phone, String email) {
        this.name = name;
        this.birth = birth;
        this.phone = phone;
        this.email = email;
    }

    // 주요 메서드2: 해시된 새 비밀번호로 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 주요 메서드3: 회원 상태를 탈퇴로 변경
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    // 주요 메서드4: 회원 계정이 활성 상태인지 확인
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }
    
    // 담당자 확인 메서드: 회원인지 담당자인지 확인하기
    public abstract boolean isAdmin();
}