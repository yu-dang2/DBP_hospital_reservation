package hospital.model.domain;

/**
 * 일반 회원(ROLE 기준) 구현 클래스. Member가 추상 클래스이므로 DAO 조회 결과를 담을 때 사용한다.
 * isAdmin()은 Member의 추상 메서드이므로 반드시 구현해야 한다.
 */
public class GeneralMember extends Member {

    public GeneralMember() {
        super();
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
}
