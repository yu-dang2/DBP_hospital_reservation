package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import hospital.model.domain.Doctor;
import hospital.model.domain.MedicalRecord;
import hospital.model.domain.Reservation;
import hospital.model.domain.ReservationStatus;
import hospital.model.exception.ExistingMedicalRecordException;
import hospital.model.exception.InvalidStatusChangeException;
import hospital.model.manager.DoctorManager;
import hospital.model.manager.ReservationManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//관리자 예약 현황과 진료기록 요청 처리(FR-31, 32, 33)
@WebServlet("/admin/reservation/*")
public class AdminReservationController extends HttpServlet {
    private ReservationManager reservationManager;
    private DoctorManager doctorManager;

    //서블릿 초기화: 처음 요청 시 한번 실행되어 Manager 준비
    @Override
    public void init() throws ServletException {
        reservationManager = new ReservationManager();
        doctorManager = DoctorManager.getInstance();
    }
    
    //GET 요청: 화면 조회(목록, 진료기록 입력 화면)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	String action = request.getParameter("action");
    	
    	try {
    		if("listReservations".equals(action)) {
    			listReservations(request, response);
    		} else if("showRecordForm".equals(action)) {
    			showRecordForm(request, response);
    			
    		} else {
    			 response.sendError(HttpServletResponse.SC_NOT_FOUND);
    		}
    	} catch(SQLException e) {
    		throw new ServletException(e);
    	}
    }
    
    //POST 요청: 데이터 변경(상태변경, 진료기록 등록)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException{
    	
    	String action = request.getParameter("action");

        try {
            if ("changeStatus".equals(action)) {
                changeStatus(request, response);
            } else if ("createRecord".equals(action)) {
                createRecord(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    
    //1. 예약 현황 목록(GET, action=listReservations)
    private void listReservations(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	List<Reservation> reservationList = reservationManager.findReservationList(null, null, null);
    	List<Doctor> doctorList = doctorManager.findDoctorList();
    	
    	request.setAttribute("reservationList", reservationList);
    	request.setAttribute("doctorList", doctorList);
    	request.getRequestDispatcher("/WEB-INF/views/admin/admin-reservation-list.jsp").forward(request, response);
    }
    
    //2. 진료기록 입력 화면(GET, action=showRecordForm&reservationId=)
    private void showRecordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	
    	Long reservationId = parseIdParameter(request, response, "reservationId");

    	if (reservationId == null) {
    	    return;
    	}
    	
    	Reservation reservation = reservationManager.findReservation(reservationId);

        if (reservation == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("reservation", reservation);
        request.getRequestDispatcher("/WEB-INF/views/admin/admin-record-form.jsp")
        	.forward(request, response);
    }
    
    //3. 예약 상태 변경(POST, action=changeStatus)
    private void changeStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	Long reservationId = parseIdParameter(request, response, "reservationId");
    	
    	if(reservationId == null) {
    		return;
    	}
    	
    	ReservationStatus status;
    	try {
    		status = ReservationStatus.valueOf(request.getParameter("status"));
    	}catch(IllegalArgumentException | NullPointerException e) {
    		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "올바르지 않은 예약 상태입니다.");
    		return;
    	}
    	
    	try {
    		reservationManager.changeStatus(reservationId, status);
    		response.sendRedirect(request.getContextPath() + "/admin/reservation/?action=listReservations");
    	}catch(InvalidStatusChangeException ex) {
    		request.setAttribute("errorMessage", ex.getMessage());
    		listReservations(request, response);
    	}
    }
    
    //4. 진료기록 등록(POST, action=createRecord)
    private void createRecord(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	Long reservationId = parseIdParameter(request, response, "reservationId");

    	if (reservationId == null) {
    	    return;
    	}
    	
    	String diagnosis = request.getParameter("diagnosis");
    	String prescription = request.getParameter("prescription");
    	
    	if (diagnosis == null || diagnosis.trim().isEmpty()) {
    		request.setAttribute("diagnosis", diagnosis);
    	    request.setAttribute("prescription", prescription);
    		request.setAttribute("errorMessage", "진단 내용을 입력해 주세요.");
    	    showRecordForm(request, response);
    	    return;
    	}
    	Reservation reservation = new Reservation();
    	MedicalRecord medicalRecord = new MedicalRecord();
    	
    	reservation.setReservationId(reservationId);
    	medicalRecord.setReservation(reservation);
    	medicalRecord.setDiagnosis(diagnosis);
    	medicalRecord.setPrescription(prescription);
    	
    
    	
    	try {    		
    		reservationManager.createMedicalRecord(medicalRecord);
    		response.sendRedirect(request.getContextPath() + "/admin/reservation/?action=listReservations");
    	}catch (ExistingMedicalRecordException ex) {
    		request.setAttribute("diagnosis", diagnosis);
    		request.setAttribute("prescription", prescription);
    		request.setAttribute("errorMessage", ex.getMessage());
    		showRecordForm(request, response);
    	}
    }
    
    // 요청 파라미터의 ID 검증 
    // ReservationController의 parseIdParameter와 동일 (추후 공통 유틸로 분리 예정)
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
