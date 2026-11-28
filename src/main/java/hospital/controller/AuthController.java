package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import hospital.model.domain.AuthenticationResult;
import hospital.model.manager.MemberManager;

//로그인, 로그아웃 처리 담당 Controller 

@WebServlet({"/login", "/logout"})
public class AuthController extends HttpServlet {

    //세션에 회원 ID와 역할을 저장하는 키
    public static final String SESSION_MEMBER_ID = "memberId";
    public static final String SESSION_ROLE = "role";

    private static final String VIEW_LOGIN = "/WEB-INF/views/login.jsp";

    private final MemberManager memberManager = new MemberManager();

    //로그인한 회원의 ID를 확인하는 메서드
    public static Long getLoginMemberId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_MEMBER_ID);
        return (value instanceof Long) ? (Long) value : null;
    }

    @Override //GET 요청 처리
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/login".equals(req.getServletPath())) {
            showLoginForm(req, resp); 
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    @Override //POST 요청 처리
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login":
                login(req, resp);
                break;
            case "/logout":
                logout(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // GET 요청 시 호출. 로그인 폼을 보여줌. 
    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("success".equals(req.getParameter("signup"))) {
            req.setAttribute("infoMessage", "회원가입이 완료되었습니다. 로그인해 주세요.");
        }
        forward(req, resp, VIEW_LOGIN);
    }

    // POST 요청 시 호출. 로그인 처리 후 세션에 회원 ID와 역할을 저장
    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        //입력값 가져오기
        String userId = trim(req.getParameter("userId"));
        String password = req.getParameter("password");
        req.setAttribute("userId", userId);

        if (isBlank(userId) || isBlank(password)) { //공백 확인 
            req.setAttribute("errorMessage", "아이디와 비밀번호를 입력해 주세요.");
            forward(req, resp, VIEW_LOGIN);
            return;
        }

        try { //사용자 인증 처리
            AuthenticationResult result = memberManager.authenticate(userId, password); 

            if (result != null && result.isSuccess()) {
                //기존 세션 무효화 후 새 세션 생성
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = req.getSession(true);
                session.setAttribute(SESSION_MEMBER_ID, result.getMemberId());
                session.setAttribute(SESSION_ROLE, result.getRole());

                resp.sendRedirect(req.getContextPath() + "/");
                return;
            }

            req.setAttribute("errorMessage",
                    result != null ? result.getMessage() : "로그인에 실패했습니다.");
        } catch (SQLException e) {
            getServletContext().log("로그인 처리 중 DB 오류", e);
            req.setAttribute("errorMessage", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }

        forward(req, resp, VIEW_LOGIN);
    }

    //POST 요청 시 호출. 로그아웃 처리 후 세션 무효화
    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(view).forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}