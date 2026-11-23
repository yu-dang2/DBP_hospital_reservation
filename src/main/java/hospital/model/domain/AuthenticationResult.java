package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter

// 로그인 인증 결과를 나타냄.
public class AuthenticationResult {

    private boolean success;
    private Long memberId;
    private MemberRole role;
    private String message;

    // 인증 성공 
    public static AuthenticationResult success(Long memberId, MemberRole role) {
        AuthenticationResult result = new AuthenticationResult();
        result.success = true;
        result.memberId = memberId;
        result.role = role;
        return result;
    }

    // 인증 실패
    public static AuthenticationResult fail(String message) {
        AuthenticationResult result = new AuthenticationResult();
        result.success = false;
        result.message = message;
        return result;
    }
}