package hospital.model.exception;

// 증상 입력이 비어 있을 때 DoctorManager.recommendDepartments()에서 발생
public class InvalidSymptomException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidSymptomException() {
        super("증상을 입력해 주세요.");
    }

    public InvalidSymptomException(String message) {
        super(message);
    }
}
