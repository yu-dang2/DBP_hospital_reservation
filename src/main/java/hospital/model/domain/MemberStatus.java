package hospital.model.domain;

public enum MemberStatus {
    ACTIVE("A"),  
    WITHDRAWN("W");   
    
    private final String code;   // DB에 저장할 코드값

    MemberStatus(String code) {
        this.code = code;
    }
    
 // enum → DB 코드 (저장할 때)
    public String getCode() {
        return code;
    }

    // DB 코드 → enum (조회할 때)
    public static MemberStatus fromCode(String code) {
        for (MemberStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("알 수 없는 회원상태: " + code);
    }
}