package hospital.model.dao;

/** DAO 계층의 SQL 실행 실패를 감싸는 unchecked 예외 */
public class DaoException extends RuntimeException {

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
