
package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hospital.model.domain.Doctor;
import hospital.model.domain.DoctorOff;
import hospital.model.domain.DoctorSchedule;
import hospital.model.exception.DoctorOffNotDeletableException;
import hospital.model.exception.ExistingDoctorOffException;
import hospital.model.exception.InvalidScheduleException;
import hospital.model.manager.DoctorManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//관리자 진료 스케줄 요청 처리(FR-34)
@WebServlet("/admin/schedule/*")
public class AdminScheduleController extends HttpServlet {
	private DoctorManager doctorManager;

	// 서블릿 초기화: 처음 요청 시 한번 실행되어 Manager 준비
	@Override
	public void init() throws ServletException {
		doctorManager = DoctorManager.getInstance();
	}

	// GET 요청: 화면 조회(목록, 진료기록 입력 화면)
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");

		try {
			if ("showSchedule".equals(action)) {
				showSchedule(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	// POST 요청: 데이터 변경(상태변경, 진료기록 등록)
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");

		try {
			if ("updateSchedule".equals(action)) {
				updateSchedule(request, response);
			} else if ("createOff".equals(action)) {
				createOff(request, response);
			} else if ("deleteOff".equals(action)) {
				deleteOff(request, response);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}

		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	// 1. 의사 일정 조회 (GET, action=showSchedule&doctorId=)
	private void showSchedule(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		List<Doctor> doctorList = doctorManager.findDoctorList();
		request.setAttribute("doctorList", doctorList);

		String doctorIdParam = request.getParameter("doctorId");

		if (doctorIdParam != null && !doctorIdParam.trim().isEmpty()) {
			Long doctorId = parseIdParameter(request, response, "doctorId");
			if (doctorId == null) {
				return;
			}

			Doctor doctor = doctorManager.findDoctor(doctorId);

			if (doctor == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			request.setAttribute("doctor", doctor);
		}
		request.getRequestDispatcher("/WEB-INF/views/admin/admin-schedule.jsp").forward(request, response);
	}

	// 2. 정기 진료시간 수정 (POST, action=updateSchedule)
	private void updateSchedule(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		Long scheduleId = parseIdParameter(request, response, "scheduleId");
		if(scheduleId == null) {
			return;
		}
		
		Long doctorId = parseIdParameter(request, response, "doctorId");
		if(doctorId == null) {
			return;
		}
		
		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");
		
		boolean dayOff = request.getParameter("dayOff") != null;
		
		DoctorSchedule doctorSchedule = new DoctorSchedule();
		
		doctorSchedule.setScheduleId(scheduleId);
		doctorSchedule.setDoctorId(doctorId);
		doctorSchedule.setStartTime(startTime);
		doctorSchedule.setEndTime(endTime);
		doctorSchedule.setDayOff(dayOff);
		
		try {
			doctorManager.updateSchedule(doctorSchedule);
			response.sendRedirect(request.getContextPath() + "/admin/schedule/?action=showSchedule&doctorId=" + doctorId);
		}catch(InvalidScheduleException ex) {
			request.setAttribute("errorMessage", ex.getMessage());
    		showSchedule(request, response);
		}
	}
	
	// 3. 휴진 등록 (POST, action=createOff)
	private void createOff(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException, SQLException {
		Long doctorId = parseIdParameter(request, response, "doctorId");
		if(doctorId == null) {
			return;
		}
		
		Date offDate;
		try {
			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
			dayFormat.setLenient(false);
			offDate = dayFormat.parse(request.getParameter("offDate"));
		} catch(ParseException | NullPointerException e) {
			request.setAttribute("errorMessage", "올바르지 않은 날짜입니다.");
    	    showSchedule(request, response);
    	    return;
		}
		
		String reason  = request.getParameter("reason");
		DoctorOff doctorOff = new DoctorOff();
		
		doctorOff.setDoctorId(doctorId);
		doctorOff.setOffDate(offDate);
		doctorOff.setReason(reason);
		
		try {
			doctorManager.createDoctorOff(doctorOff);
			response.sendRedirect(request.getContextPath() + "/admin/schedule/?action=showSchedule&doctorId=" + doctorId);
		}catch (ExistingDoctorOffException ex) {
			request.setAttribute("errorMessage", ex.getMessage());
    		showSchedule(request, response);
		}
	}
	
	// 4. 휴진 삭제 (POST, action=deleteOff)
	private void deleteOff(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, SQLException {
		Long offId = parseIdParameter(request, response, "offId");
		if(offId == null) {
			return;
		}
		Long doctorId = parseIdParameter(request, response, "doctorId");
		if(doctorId == null) {
			return;
		}
		try {
			doctorManager.deleteDoctorOff(offId);
			response.sendRedirect(request.getContextPath() + "/admin/schedule/?action=showSchedule&doctorId=" + doctorId);
		}catch (DoctorOffNotDeletableException ex) {
			request.setAttribute("errorMessage", ex.getMessage());
    		showSchedule(request, response);
		}
	}


	// 요청 파라미터의 ID 검증
	// ReservationController의 parseIdParameter와 동일 (추후 공통 유틸로 분리 예정)
	private Long parseIdParameter(HttpServletRequest request, HttpServletResponse response, String parameterName)
			throws IOException {

		String value = request.getParameter(parameterName);

		// 파라미터 누락 또는 빈 값 검사
		if (value == null || value.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, parameterName + "이(가) 누락되었습니다.");
			return null;
		}

		// 숫자 변환 검사
		try {
			Long id = Long.parseLong(value.trim());

			if (id <= 0) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, parameterName + "은(는) 양수여야 합니다.");
				return null;
			}

			return id;

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, parameterName + " 형식이 올바르지 않습니다.");
			return null;
		}
	}

}
