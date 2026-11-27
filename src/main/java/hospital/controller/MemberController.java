package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import hospital.model.domain.GeneralMember;
import hospital.model.domain.IdCheckResult;
import hospital.model.domain.Member;
import hospital.model.domain.MemberOperationResult;
import hospital.model.domain.MemberRole;
import hospital.model.domain.MemberStatus;
import hospital.model.domain.MyPageResult;
import hospital.model.manager.MemberManager;

//회원 계정 관련 요청 담당

@WebServlet({
        "/members",
        "/members/new",
        "/members/idcheck",
        "/members/me",
        "/members/me/edit",
        "/members/me/withdraw"
})
public class MemberController extends HttpServlet {

    private static final String VIEW_SIGNUP = "/WEB-INF/views/signup.jsp";
    private static final String VIEW_MYPAGE = "/WEB-INF/views/mypage.jsp";
    private static final String SESSION_FLASH = "flashMessage";

    //입력값 검증 상수. 입력 항목의 최대 · 최소 길이 제한
    private static final int USER_ID_MAX = 50;
    private static final int NAME_MAX = 50;
    private static final int PHONE_MAX = 20;
    private static final int EMAIL_MAX = 100;
    private static final int PASSWORD_MIN = 8;

    private final MemberManager memberManager = new MemberManager();

    
    @Override //GET 요청 처리
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/members/new":         showSignUpForm(req, resp); break; //회원가입 화면
            case "/members/idcheck":    checkUserId(req, resp); break; //아이디 중복 확인
            case "/members/me":          showMyPage(req, resp); break; //마이페이지 표시
            case "/members/me/edit":     showEditForm(req, resp); break; //회원정보 수정 화면
            case "/members/me/withdraw": showWithdrawForm(req, resp); break; //회원 탈퇴 화면
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/members":             signUp(req, resp); break; //회원가입 처리
            case "/members/me":          updateMyInfo(req, resp); break; //회원정보 수정
            case "/members/me/withdraw": withdraw(req, resp); break; //회원 탈퇴 처리
            default: resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    // 회원가입 화면 표시
    private void showSignUpForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        forward(req, resp, VIEW_SIGNUP);
    }

    // 아이디 중복 확인 처리
    private void checkUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = trim(req.getParameter("userId"));

        //결과 저장 객체 생성
        IdCheckResult result = new IdCheckResult();
        boolean available = false;
        String message;

        if (isBlank(userId) || userId.length() > USER_ID_MAX) {
            message = "아이디는 1~50자로 입력해 주세요.";
        } else {
            try {
                available = memberManager.isUserIdAvailable(userId);
                message = available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.";
            } catch (SQLException e) {
                message = "중복 확인 중 오류가 발생했습니다.";
            }
        }

        result.setAvailable(available);
        result.setMessage(message);

        //JSON 응답
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().print("{\"available\":" + result.isAvailable()
                + ",\"message\":\"" + escapeJson(result.getMessage()) + "\"}");
    }

    // 회원가입 처리.
    private void signUp(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String userId = trim(req.getParameter("userId"));
        String password = req.getParameter("password"); //비밀번호 공백 허용              
        String passwordConfirm = req.getParameter("passwordConfirm");
        String name = trim(req.getParameter("name"));
        String birthText = trim(req.getParameter("birth"));
        String phone = trim(req.getParameter("phone"));
        String email = trim(req.getParameter("email"));

        // 실패 시 입력값 유지
        req.setAttribute("userId", userId);
        req.setAttribute("name", name);
        req.setAttribute("birth", birthText);
        req.setAttribute("phone", phone);
        req.setAttribute("email", email);

        String error = validateSignUp(userId, password, passwordConfirm,
                name, birthText, phone, email);
        if (error != null) {
            req.setAttribute("errorMessage", error);
            forward(req, resp, VIEW_SIGNUP);
            return;
        }

        GeneralMember member = new GeneralMember();
        member.setUserId(userId);
        member.setPassword(password);         
        member.setName(name);
        member.setBirth(parseBirth(birthText));
        member.setPhone(nullIfBlank(phone));
        member.setEmail(nullIfBlank(email));

        // 회원가입 시 기본 역할과 상태 설정
        member.setRole(MemberRole.MEMBER);
        member.setStatus(MemberStatus.ACTIVE);

        try {
            MemberOperationResult result = memberManager.signUp(member);
            if (result.isSuccess()) {
                resp.sendRedirect(req.getContextPath() + "/login?signup=success");
                return;
            }
            req.setAttribute("errorMessage", result.getMessage());
        } catch (SQLException e) {
            throw new ServletException("회원가입 처리 실패", e);
        }

        forward(req, resp, VIEW_SIGNUP);
    }

    // 마이페이지 조회
    private void showMyPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long memberId = requireLogin(req, resp);
        if (memberId == null) {
            return;
        }

        try {
            MyPageResult page = memberManager.getMyPage(memberId);
            if (page == null || page.getMember() == null) {
                redirectToLogin(req, resp);
                return;
            }   

            req.setAttribute("mode", "view"); // 현재 화면 모드
            req.setAttribute("member", page.getMember()); //회원 정보
            req.setAttribute("recentReservations", page.getRecentReservations()); //최근 예약
            req.setAttribute("recentRecords", page.getRecentRecords()); //최근 진료기록
            applyFlash(req); //성공 메시지 flash 처리
        } catch (SQLException e) {
            throw new ServletException("마이페이지 조회 실패", e);
        }

        forward(req, resp, VIEW_MYPAGE);
    }

    // 회원 정보 수정 화면 표시
    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showMemberMode(req, resp, "edit");
    }

    // 회원 탈퇴 화면 표시
    private void showWithdrawForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        showMemberMode(req, resp, "withdraw");
    }

    // 회원 정보 조회 
    private void showMemberMode(HttpServletRequest req, HttpServletResponse resp, String mode)
            throws ServletException, IOException {
        Long memberId = requireLogin(req, resp);
        if (memberId == null) {
            return;
        }

        try {
            Member member = memberManager.getMember(memberId);
            if (member == null) {
                redirectToLogin(req, resp);
                return;
            }
            req.setAttribute("mode", mode);
            req.setAttribute("member", member);
        } catch (SQLException e) {
            throw new ServletException("회원 정보 조회 실패", e);
        }

        forward(req, resp, VIEW_MYPAGE);
    }

    // 자신의 회원 정보 수정 처리
    private void updateMyInfo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long memberId = requireLogin(req, resp);
        if (memberId == null) {
            return;
        }

        String name = trim(req.getParameter("name"));
        String birthText = trim(req.getParameter("birth"));
        String phone = trim(req.getParameter("phone"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String passwordConfirm = req.getParameter("passwordConfirm");

        //입력값 검증
        String error = validateProfile(name, birthText, phone, email, password, passwordConfirm);

        try {
            if (error == null) {
                GeneralMember update = new GeneralMember();
                update.setMemberId(memberId);       // 본인의 회원 ID만 수정 가능
                update.setName(name);
                update.setBirth(parseBirth(birthText));
                update.setPhone(nullIfBlank(phone));
                update.setEmail(nullIfBlank(email));
                update.setPassword(isBlank(password) ? null : password); // null 이면 기존 비밀번호 유지

                MemberOperationResult result = memberManager.updateMember(memberId, update);
                if (result.isSuccess()) {
                    setFlash(req, "회원정보가 수정되었습니다.");
                    resp.sendRedirect(req.getContextPath() + "/members/me");
                    return;
                }
                error = result.getMessage();
            }

            // 실패 시 입력값 유지. 로그인 화면으로 이동함.
            Member current = memberManager.getMember(memberId);
            if (current == null) {
                redirectToLogin(req, resp);
                return;
            }

            // 생년월일이 잘못 입력된 경우 기존 값을 유지
            Date birth = parseBirth(birthText);
            if (birth == null && !isBlank(birthText)) {
                birth = current.getBirth();
            }
            current.updateProfile(name, birth, nullIfBlank(phone), nullIfBlank(email));

            req.setAttribute("mode", "edit");
            req.setAttribute("member", current);
            req.setAttribute("errorMessage", error);
        } catch (SQLException e) {
            throw new ServletException("회원정보 수정 실패", e);
        }

        forward(req, resp, VIEW_MYPAGE);
    }

    // 회원 탈퇴 처리
    private void withdraw(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long memberId = requireLogin(req, resp);
        if (memberId == null) {
            return;
        }

        String password = req.getParameter("password");

        try {
            MemberOperationResult result = memberManager.withdraw(memberId, password);

            if (result.isSuccess()) { //탈퇴 성공 처리
                HttpSession session = req.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                resp.sendRedirect(req.getContextPath() + "/?withdrawn=true");
                return;
            }

            //탈퇴 실패 처리
            Member member = memberManager.getMember(memberId);
            if (member == null) {
                redirectToLogin(req, resp);
                return;
            }
            req.setAttribute("mode", "withdraw");
            req.setAttribute("member", member);
            req.setAttribute("errorMessage", result.getMessage());
        } catch (SQLException e) {
            throw new ServletException("회원 탈퇴 처리 실패", e);
        }

        forward(req, resp, VIEW_MYPAGE);
    }

    // 회원가입 시 입력값 검증
    private String validateSignUp(String userId, String password, String passwordConfirm,
                                  String name, String birthText, String phone, String email) {
        if (isBlank(userId)) {
            return "아이디를 입력해 주세요.";
        }
        if (userId.length() > USER_ID_MAX) {
            return "아이디는 50자 이하로 입력해 주세요.";
        }
        if (isBlank(password)) {
            return "비밀번호를 입력해 주세요.";
        }
        if (password.length() < PASSWORD_MIN) {
            return "비밀번호는 " + PASSWORD_MIN + "자 이상이어야 합니다.";
        }
        if (!password.equals(passwordConfirm)) {
            return "비밀번호 확인이 일치하지 않습니다.";
        }
        if (isBlank(name)) {
            return "이름을 입력해 주세요.";
        }
        if (name.length() > NAME_MAX) {
            return "이름은 50자 이하로 입력해 주세요.";
        }
        Date birth = parseBirth(birthText);
        if (birth == null) {
            return "생년월일을 올바르게 입력해 주세요.";
        }
        if (birth.after(new Date())) {
            return "생년월일이 올바르지 않습니다.";
        }
        return validateContact(phone, email);
    }

    // 회원 정보 수정 시 입력값 검증
    private String validateProfile(String name, String birthText, String phone, String email,
                                   String password, String passwordConfirm) {
        if (isBlank(name)) {
            return "이름을 입력해 주세요.";
        }
        if (name.length() > NAME_MAX) {
            return "이름은 50자 이하로 입력해 주세요.";
        }
        if (!isBlank(birthText) && parseBirth(birthText) == null) {
            return "생년월일을 올바르게 입력해 주세요.";
        }
        if (!isBlank(password)) {
            if (password.length() < PASSWORD_MIN) {
                return "비밀번호는 " + PASSWORD_MIN + "자 이상이어야 합니다.";
            }
            if (!password.equals(passwordConfirm)) {
                return "비밀번호 확인이 일치하지 않습니다.";
            }
        }
        return validateContact(phone, email);
    }

    private String validateContact(String phone, String email) {
        if (phone != null && phone.length() > PHONE_MAX) {
            return "연락처는 20자 이하로 입력해 주세요.";
        }
        if (!isBlank(email)) {
            if (email.length() > EMAIL_MAX) {
                return "이메일은 100자 이하로 입력해 주세요.";
            }
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return "이메일 형식이 올바르지 않습니다.";
            }
        }
        return null;
    }

   // 공통 유틸리티
   // 로그인 여부 확인 후 회원 ID 반환
    private Long requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long memberId = AuthController.getLoginMemberId(req.getSession(false));
        if (memberId == null) {
            redirectToLogin(req, resp);
        }
        return memberId;
    }
    // 로그인 화면으로 리다이렉트
    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login");
    }
    // 지정한 JSP로 포워딩
    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(view).forward(req, resp);
    }

    // 리다이렉트 이후에 보여줄 메시지를 세션에 저장
    private void setFlash(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(SESSION_FLASH, message);
    }

    // 저장된 메시지를 요청 속성으로 옮기고 세션에서 제거
    private void applyFlash(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return;
        }
        Object message = session.getAttribute(SESSION_FLASH);
        if (message != null) {
            req.setAttribute("successMessage", message);
            session.removeAttribute(SESSION_FLASH);
        }
    }

    // 문자열을 Date 로 변환
    private static Date parseBirth(String text) {
        if (isBlank(text)) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            return format.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }
    // 문자열을 JSON용으로 이스케이프 처리
    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    // 공백 제거
    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
    // 공백 검사
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    // 공백을 null로 변환
    private static String nullIfBlank(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
