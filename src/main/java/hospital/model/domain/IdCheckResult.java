package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter

// 아이디 중복 확인
public class IdCheckResult {

    private boolean available;   // 사용 가능 여부
    private String message;      // 화면에 표시할 메시지

    //기본 생성자
    public IdCheckResult() { 
    }

    public IdCheckResult(boolean available, String message) {
        this.available = available;
        this.message = message;
    }
}