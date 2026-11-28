package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import hospital.model.domain.Member;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationSummary;
import hospital.model.manager.MemberManager;
import hospital.model.manager.ReservationManager;

//메인 화면 처리 담당 Controller

@WebServlet({"", "/"})
public class HomeController extends HttpServlet {

    private static final String VIEW_MAIN = "/WEB-INF/views/main.jsp";

    private final MemberManager memberManager = new MemberManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

         ReservationManager reservationManager = new ReservationManager();

        // 회원 탈퇴 직후 리다이렉트된 경우 안내 메시지
        if ("true".equals(req.getParameter("withdrawn"))) {
            req.setAttribute("infoMessage", "회원 탈퇴가 완료되었습니다.");
        }

        Long memberId = AuthController.getLoginMemberId(req.getSession(false));
        if (memberId != null) { //로그인 여부 확인
            try {
                Member member = memberManager.getMember(memberId);
                // 회원의 다가오는 예약 내역 조회
                if (member != null && member.isActive()) {
                List<Reservation> upcomingReservations =
                            reservationManager.getUpcomingReservations(memberId);

                List<ReservationSummary> upcoming = toSummaries(upcomingReservations);            

                req.setAttribute("member", member);
                req.setAttribute("upcomingReservations", upcoming);
                }
            } catch (SQLException e) {
                throw new ServletException("메인 화면 데이터 조회 실패", e);
            }
        }

        req.getRequestDispatcher(VIEW_MAIN).forward(req, resp);
    }

     // Reservation 목록을 ReservationSummary 목록으로 변환
    private List<ReservationSummary> toSummaries(List<Reservation> reservations) {
    List<ReservationSummary> summaries = new ArrayList<>();
    if (reservations == null) {
        return summaries;
    }
    for (Reservation r : reservations) {
        ReservationSummary summary = new ReservationSummary();
        summary.setReservationId(r.getReservationId());
        summary.setDoctorName(r.getDoctor().getName());
        summary.setReservationDate(r.getReservationDate());
        summary.setStatus(r.getStatus());
        summaries.add(summary);
    }
    return summaries;
}
}