package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import hospital.model.domain.Doctor;
import hospital.model.domain.Reservation;
import hospital.model.manager.DoctorManager;
import hospital.model.manager.ReservationManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/reservation/*")
public class AdminReservationController extends HttpServlet {
    //private static final long serialVersionUID = 1L; (추후 필요시 추가)
    private ReservationManager reservationManager;
    private DoctorManager doctorManager;

    @Override
    public void init() throws ServletException {
        reservationManager = new ReservationManager();
        doctorManager = DoctorManager.getInstance();
    }

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
    
    private void listReservations(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	List<Reservation> reservationList = reservationManager.findReservationList(null, null, null);
    	List<Doctor> doctorList = doctorManager.findDoctorList();
    	
    	request.setAttribute("reservationList", reservationList);
    	request.setAttribute("doctorList", doctorList);
    	request.getRequestDispatcher("/WEB-INF/views/admin/admin-reservation-list.jsp").forward(request, response);
    }
    
    private void showRecordForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	//todo
    }
    
    private void changeStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	//todo
    }
    
    private void createRecord(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException{
    	//todo
    }
            
    
}
