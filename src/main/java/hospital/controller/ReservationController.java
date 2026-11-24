package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import hospital.model.manager.ReservationManager;
import hospital.model.domain.ReservationFormData;
import hospital.model.domain.GeneralMember;
import hospital.model.domain.Doctor;

@WebServlet("/reservation/*")
public class ReservationController extends HttpServlet {
    //private static final long serialVersionUID = 1L; (추후 필요시 추가)
    private ReservationManager reservationManager;

    @Override
    public void init() throws ServletException {
    	//참고: View 연결 테스트 중에는 Manager 초기화를 생략합니다(일단 DB 제외하고 연결 테스트할때)
        reservationManager = new ReservationManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
        	//해당 부분에 View 테스트용 코드 들어감(하단 예시)
        	/*
        	if ("testView".equals(action)) {
        	    request.getRequestDispatcher(
        	        "/WEB-INF/views/ReservationViewTest.jsp"
        	    ).forward(request, response);
        	}
        	*/
        	
            if ("showReservationForm".equals(action)) {
                showReservationForm(request, response);

            } else if ("listReservations".equals(action)) {
                listReservations(request, response);

            } else if ("showReservationDetail".equals(action)) {
                showReservationDetail(request, response);

            } else if ("showUpdateForm".equals(action)) {
                showUpdateForm(request, response);

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            if ("createReservation".equals(action)) {
                createReservation(request, response);
            } else if ("updateReservation".equals(action)) {
                updateReservation(request, response);
            } else if ("cancelReservation".equals(action)) {
                cancelReservation(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 1. 예약 등록 화면
    private void showReservationForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

    	Long doctorId = parseIdParameter(request, response, "doctorId");

    	if (doctorId == null) {
    	    return;
    	}

        ReservationFormData formData =
                reservationManager.findReservationFormData(doctorId);

        if (formData == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("formData", formData);
        request.getRequestDispatcher("/WEB-INF/views/reservation/reservationForm.jsp")
        	.forward(request, response);
    }

    // 2. 예약 등록
    private void createReservation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
    	
        Long memberId = (Long) request.getSession().getAttribute("memberId");
        Long doctorId = parseIdParameter(request, response, "doctorId");

        if (doctorId == null) {
            return;
        }

        java.util.Date reservationDate =
                parseReservationDate(request.getParameter("reservationDate"), response);

        if (reservationDate == null) {
            return;
        }

        GeneralMember member = new GeneralMember();
        member.setMemberId(memberId);

        Doctor doctor = new Doctor();
        doctor.setDoctorId(doctorId);

        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setDoctor(doctor);
        reservation.setReservationDate(reservationDate);
        reservation.setStatus(ReservationStatus.RESERVED);

        int result = reservationManager.createReservation(reservation);

        // 등록 결과 처리
        if (result > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/reservation?action=listReservations");

        } else {
            request.setAttribute("errorMessage", "예약 등록에 실패했습니다.");

            // 의사 정보, 진료 일정 재조회 -> 등록 성공하면 예약 목록, 실패하면 예약 폼으로
            ReservationFormData formData =
                    reservationManager.findReservationFormData(doctorId);

            if (formData == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("formData", formData);

            // 예약 등록 화면으로 이동하기
            request.getRequestDispatcher("/WEB-INF/views/reservation/reservationForm.jsp")
            		.forward(request, response);
        }
    }

    // 3. 예약 목록
    private void listReservations(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long memberId = (Long) request.getSession().getAttribute("memberId");

        // 회원 예약 내역 조회
        List<Reservation> reservationList =
                reservationManager.findReservationsByMemberId(memberId);
        request.setAttribute("reservationList", reservationList); //결과 전달

        // 예약 목록 화면으로 이동하기
        request.getRequestDispatcher("/WEB-INF/views/reservation/reservationList.jsp")
        	.forward(request, response);
    }

    // 4. 예약 상세
    private void showReservationDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long memberId = (Long) request.getSession().getAttribute("memberId");
        Long reservationId = parseIdParameter(request, response, "reservationId");

        if (reservationId == null) {
            return;
        }

        // 본인의 예약인지 확인 + 상세 조회
        Reservation reservation =
                reservationManager.findReservationById(reservationId, memberId);

        if (reservation == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("reservation", reservation);

        // 예약 상세 화면으로 이동하기
        request.getRequestDispatcher("/WEB-INF/views/reservation/reservationDetail.jsp")
        	.forward(request, response);
    }

    // 5. 예약 변경 화면
    private void showUpdateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long memberId = (Long) request.getSession().getAttribute("memberId");
        Long reservationId = parseIdParameter(request, response, "reservationId");

        if (reservationId == null) {
            return;
        }

        // 본인의 예약인지 확인 + 예약 정보 조회
        Reservation reservation =
                reservationManager.findReservationById(reservationId, memberId);

        // 예약 정보가 없거나 본인의 예약이 아닌 경우
        if (reservation == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 변경 가능한 예약인지
        if (!reservation.isCancelable()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        request.setAttribute("reservation", reservation);

        // 예약 변경 화면으로 이동하기
        request.getRequestDispatcher("/WEB-INF/views/reservation/reservationUpdateForm.jsp")
        	.forward(request, response);
    }

    // 6. 예약 변경
    private void updateReservation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long memberId = (Long) request.getSession().getAttribute("memberId");
        Long reservationId = parseIdParameter(request, response, "reservationId");

        if (reservationId == null) {
            return;
        }

        java.util.Date reservationDate =
                parseReservationDate(request.getParameter("reservationDate"), response);

        if (reservationDate == null) {
            return;
        }

        // 예약 변경 요청
        int result = reservationManager.updateReservation(
                reservationId, memberId, reservationDate);

        if (result > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/reservation?action=listReservations");
        } else {
        	request.setAttribute("errorMessage", "예약 변경에 실패했습니다.");

        	Reservation reservation =
        	        reservationManager.findReservationById(reservationId, memberId);

        	if (reservation == null) {
        	    response.sendError(HttpServletResponse.SC_NOT_FOUND);
        	    return;
        	}

        	request.setAttribute("reservation", reservation);

        	request.getRequestDispatcher("/WEB-INF/views/reservation/reservationUpdateForm.jsp")
            	.forward(request, response);
        }
    }

    // 7. 예약 취소
    private void cancelReservation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long memberId = (Long) request.getSession().getAttribute("memberId");
        Long reservationId = parseIdParameter(request, response, "reservationId");

        if (reservationId == null) {
            return;
        }

        // 예약 취소 요청
        int result = reservationManager.cancelReservation(reservationId, memberId);

        if (result > 0) {
            response.sendRedirect(
                    request.getContextPath() + "/reservation?action=listReservations");
        } else {
            request.setAttribute("errorMessage", "예약 취소에 실패했습니다.");

            // 예약 상세 정보 재조회 = 취소에 실패하여 다시 상세화면으로 돌아감
            Reservation reservation =
                    reservationManager.findReservationById(reservationId, memberId);

            if (reservation == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("reservation", reservation);

            request.getRequestDispatcher("/WEB-INF/views/reservation/reservationDetail.jsp")
            	.forward(request, response);
        }	
    }
    
    // 예약 일시 문자열을 Date 객체로 변환 -> 코드 단순화를 위해 추가함
    private java.util.Date parseReservationDate(
            String dateTime, HttpServletResponse response) throws IOException {

        if (dateTime == null || dateTime.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "예약 일시가 누락되었습니다."
            );
            return null;
        }

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

        sdf.setLenient(false);

        try {
            java.text.ParsePosition position = new java.text.ParsePosition(0);
            java.util.Date date = sdf.parse(dateTime.trim(), position);

            if (date == null || position.getIndex() != dateTime.trim().length()) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "올바르지 않은 예약 일시입니다."
                );
                return null;
            }

            return date;

        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "올바르지 않은 예약 일시입니다."
            );
            return null;
        }
    }
    
    // 요청 파라미터의 ID 검증
    private Long parseIdParameter(
            HttpServletRequest request,
            HttpServletResponse response,
            String parameterName) throws IOException {

        String value = request.getParameter(parameterName);

        // 파라미터 누락 또는 빈 값 검사
        if (value == null || value.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    parameterName + "이(가) 누락되었습니다."
            );
            return null;
        }

        // 숫자 변환 검사
        try {
            Long id = Long.parseLong(value.trim());

            if (id <= 0) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        parameterName + "은(는) 양수여야 합니다."
                );
                return null;
            }

            return id;

        } catch (NumberFormatException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    parameterName + " 형식이 올바르지 않습니다."
            );
            return null;
        }
    }
    
}