package hospital.model.domain;

public enum ReservationStatus {
    RESERVED("R"),
    COMPLETED("C"),
    CANCELED("X"),
    NOSHOW("N");
    
    private final String code;   // DB에 저장할 코드값

    ReservationStatus(String code) {
        this.code = code;
    }
    
 // enum → DB 코드 (저장)
    public String getCode() {
        return code;
    }

    // DB 코드 → enum (조회)
    public static ReservationStatus fromCode(String code) {
        for (ReservationStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("알 수 없는 예약상태: " + code);
    }
}