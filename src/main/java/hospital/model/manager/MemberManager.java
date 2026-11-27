package hospital.model.manager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import hospital.model.dao.MedicalRecordDAO;
import hospital.model.dao.MemberDAO;
import hospital.model.dao.ReservationDAO;
import hospital.model.domain.AuthenticationResult;
import hospital.model.domain.Member;
import hospital.model.domain.MemberOperationResult;
import hospital.model.domain.MemberRole;
import hospital.model.domain.MemberStatus;
import hospital.model.domain.MyPageResult;

// 회원 가입·인증·조회·수정·탈퇴의 업무 규칙 처리
public class MemberManager {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int ORACLE_UNIQUE_VIOLATION = 1; 

    private final MemberDAO memberDAO = new MemberDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();

    // 아이디 등록 확인 및 사용 가능 여부 확인
    public boolean isUserIdAvailable(String userId) throws SQLException {
        if (isBlank(userId)) {
            return false;
        }
        return !memberDAO.existsByUserId(userId);
    }
    // 회원가입 요청. 아이디 중복 검사 + 비밀번호를 해시 형태로 저장함.
    public MemberOperationResult signUp(Member member) throws SQLException {
        String error = validateSignUp(member);
        if (error != null) {
            return MemberOperationResult.fail(error);
        }
        if (!isUserIdAvailable(member.getUserId())) {
            return MemberOperationResult.fail("이미 사용 중인 아이디입니다.");
        }

        member.setRole(MemberRole.MEMBER);
        member.setStatus(MemberStatus.ACTIVE);
        member.setPassword(hash(member.getPassword()));

        try {
            memberDAO.insert(member);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == ORACLE_UNIQUE_VIOLATION) {
                return MemberOperationResult.fail("이미 사용 중인 아이디입니다.");
            }
            throw ex;
        }
        return MemberOperationResult.ok("회원가입이 완료되었습니다.");
    }

    // 로그인 인증. 계정 상태를 확인함.
    public AuthenticationResult authenticate(String userId, String password) throws SQLException {
        final String failMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        if (isBlank(userId) || isBlank(password)) {
            return AuthenticationResult.fail(failMessage);
        }

        Member member = memberDAO.findByUserId(userId);
        if (member == null || !matches(password, member.getPassword())) {
            return AuthenticationResult.fail(failMessage);
        }
        if (!member.isActive()) {
            return AuthenticationResult.fail("탈퇴한 계정입니다.");
        }
        return AuthenticationResult.success(member.getMemberId(), member.getRole());
    }

    // 마이페이지 회원 조회시 사용
    public Member getMember(Long memberId) throws SQLException {
        Member member = memberDAO.findById(memberId);
        if (member != null) {
            member.setPassword(null);
        }
        return member;
    }

    // 마이페이지에서 회원정보, 예약·진료기록 조회
    public MyPageResult getMyPage(Long memberId) throws SQLException {
        Member member = getMember(memberId);
        if (member == null || !member.isActive()) {
            return null;
        }
        return new MyPageResult(
                member,
                reservationDAO.findRecentByMemberId(memberId),
                medicalRecordDAO.findRecentByMemberId(memberId));
    }

    // 회원 정보 수정
    public MemberOperationResult updateMember(Long memberId, Member input) throws SQLException {
        Member current = memberDAO.findById(memberId);
        if (current == null || !current.isActive()) {
            return MemberOperationResult.fail("회원 정보를 찾을 수 없습니다.");
        }

        String error = validateProfile(input);
        if (error != null) {
            return MemberOperationResult.fail(error);
        }

        boolean changePassword = !isBlank(input.getPassword());
        if (changePassword) {
            String passwordError = validatePassword(input.getPassword());
            if (passwordError != null) {
                return MemberOperationResult.fail(passwordError);
            }
        }

        current.updateProfile(input.getName(), input.getBirth(), input.getPhone(), input.getEmail());
        // null인 경우 기존 비밀번호 유지
        current.setPassword(changePassword ? hash(input.getPassword()) : null);

        if (memberDAO.update(current) != 1) {
            return MemberOperationResult.fail("회원 정보 수정에 실패했습니다. 다시 시도해 주세요.");
        }
        return MemberOperationResult.ok("회원 정보가 수정되었습니다.");
    }

    // 회원 탈퇴 
    // 예정된 예약이 없을 경우만 탈퇴로 변경
    public MemberOperationResult withdraw(Long memberId, String password) throws SQLException {
        Member current = memberDAO.findById(memberId);
        if (current == null || !current.isActive()) {
            return MemberOperationResult.fail("회원 정보를 찾을 수 없습니다.");
        }
        if (!matches(password, current.getPassword())) {
            return MemberOperationResult.fail("비밀번호가 일치하지 않습니다.");
        }
        if (reservationDAO.existsUpcomingByMemberId(memberId)) {
            return MemberOperationResult.fail("예정된 예약이 있어 탈퇴할 수 없습니다. 예약을 먼저 취소해 주세요.");
        }
        if (memberDAO.markWithdrawn(memberId) != 1) {
            return MemberOperationResult.fail("탈퇴 처리에 실패했습니다. 다시 시도해 주세요.");
        }
        return MemberOperationResult.ok("회원 탈퇴가 완료되었습니다.");
    }


    // 가입시 입력한 값 검사
    private String validateSignUp(Member member) {
        if (isBlank(member.getUserId())) {
            return "아이디를 입력해 주세요.";
        }
        String passwordError = validatePassword(member.getPassword());
        if (passwordError != null) {
            return passwordError;
        }
        if (isBlank(member.getName())) {
            return "이름을 입력해 주세요.";
        }
        if (member.getBirth() == null) {
            return "생년월일을 입력해 주세요.";
        }
        return null;
    }

    // 수정 시 입력한 값 검사 
    // 이름과 생년월일만 확인
    private String validateProfile(Member member) {
        if (isBlank(member.getName())) {
            return "이름을 입력해 주세요.";
        }
        if (member.getBirth() == null) {
            return "생년월일을 입력해 주세요.";
        }
        return null;
    }

    //비밀번호 길이 검사
    private String validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.";
        }
        return null;
    }

    // 입력한 비밀번호를 해시된 값과 비교
    // 로그인 · 탈퇴 시 사용
    private boolean matches(String rawPassword, String storedHash) {
        return rawPassword != null && storedHash != null && storedHash.equals(hash(rawPassword));
    }


    // 비밀번호 해시 처리
    // 가입 및 수정 시 사용
    private String hash(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    // 입력값이 null 또는 공백 문자열인지 확인
    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}