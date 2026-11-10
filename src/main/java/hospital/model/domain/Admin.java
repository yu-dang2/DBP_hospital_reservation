package hospital.model.domain;

public class Admin extends Member {

    @Override
    public boolean isAdmin() {
        return true;
    }
}
