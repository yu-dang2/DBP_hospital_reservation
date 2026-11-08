package hospital.model.domain;

public enum MemberRole {
    MEMBER("M"),
    ADMIN("A");
    
    private final String code;   // DB에 저장할 코드값

    MemberRole(String code) {
        this.code = code;
    }
    
    // enum → DB 코드 (저장할 때)
    public String getCode() {
        return code;
    }

    // DB 코드 → enum (조회할 때)
    public static MemberRole fromCode(String code) {
        for (MemberRole status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("알 수 없는 회원 권한: " + code);
    }
}