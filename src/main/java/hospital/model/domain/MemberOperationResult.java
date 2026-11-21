package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter

// 가입·수정·탈퇴 등 회원 업무의 성공 여부와, 사용자 표시 메시지를 전달
public class MemberOperationResult {

    private boolean success;
    private String message;

    //기본 생성자
    public MemberOperationResult() {
    }

    public MemberOperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 성공 + 성공 메시지
    public static MemberOperationResult ok(String message) {
        return new MemberOperationResult(true, message);
    }

    // 실패 + 실패 메시지
    public static MemberOperationResult fail(String message) {
        return new MemberOperationResult(false, message);
    }
}