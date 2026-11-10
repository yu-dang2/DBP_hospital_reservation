package hospital.model.domain;

public enum OffSource {
    MANUAL("M"),   
    HOLIDAY_API("H");
    
    private final String code;   // DB에 저장할 코드값

    OffSource(String code) {
        this.code = code;
    }
    
    // enum → DB 코드 (저장할 때)
    public String getCode() {
        return code;
    }

    // DB 코드 → enum (조회할 때)
    public static OffSource fromCode(String code) {
        for (OffSource status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("알 수 없는 휴진 등록 경로: " + code);
    }
}