package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import hospital.model.domain.Department;
import hospital.model.manager.DoctorManager;

@WebServlet("/department/*")
public class DepartmentController extends HttpServlet {
    private DoctorManager doctorManager;

    @Override
    public void init() throws ServletException {
        doctorManager = DoctorManager.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        try {
            // action 없이 /department 로 들어와도 진료과 목록을 보여줌
            if (action == null || "listDepartments".equals(action)) {
                listDepartments(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 전체 진료과 목록
    private void listDepartments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<Department> departmentList = doctorManager.findDepartmentList();

        request.setAttribute("departmentList", departmentList);
        request.getRequestDispatcher("/WEB-INF/views/search/department-list.jsp")
            .forward(request, response);
    }
}
