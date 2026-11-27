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
import hospital.model.domain.Doctor;
import hospital.model.manager.DoctorManager;

@WebServlet("/doctor/*")
public class DoctorController extends HttpServlet {
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
            if ("listDoctors".equals(action)) {
                listDoctors(request, response);
            } else if ("searchDoctors".equals(action)) {
                searchDoctors(request, response);
            } else if ("viewDoctor".equals(action)) {
                viewDoctor(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 진료과별 의사 목록
    private void listDoctors(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long deptId = parseIdParameter(request, response, "deptId");

        if (deptId == null) {
            return;
        }

        Department department = doctorManager.findDepartment(deptId);

        if (department == null) {
            redirectToDepartmentList(request, response);
            return;
        }

        List<Doctor> doctorList = doctorManager.findDoctorList(deptId);

        request.setAttribute("department", department);
        request.setAttribute("doctorList", doctorList);
        request.getRequestDispatcher("/WEB-INF/views/search/doctor-list.jsp")
            .forward(request, response);
    }

    // 진료과명·의사명 검색
    private void searchDoctors(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String keyword = request.getParameter("keyword");

        if (keyword == null || keyword.trim().isEmpty()) {
            redirectToDepartmentList(request, response);
            return;
        }

        List<Doctor> doctorList = doctorManager.searchDoctorList(keyword);

        request.setAttribute("keyword", keyword.trim());
        request.setAttribute("doctorList", doctorList);

        if (doctorList.isEmpty()) {
            request.setAttribute("message", "검색 결과가 없습니다.");
        }

        request.getRequestDispatcher("/WEB-INF/views/search/doctor-list.jsp")
            .forward(request, response);
    }

    // 의사 상세 (진료 시간표·휴진 포함)
    private void viewDoctor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Long doctorId = parseIdParameter(request, response, "doctorId");

        if (doctorId == null) {
            return;
        }

        Doctor doctor = doctorManager.findDoctor(doctorId);

        if (doctor == null) {
            redirectToDepartmentList(request, response);
            return;
        }

        request.setAttribute("doctor", doctor);
        request.getRequestDispatcher("/WEB-INF/views/search/doctor-view.jsp")
            .forward(request, response);
    }

    private void redirectToDepartmentList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/department?action=listDepartments");
    }

    private Long parseIdParameter(
            HttpServletRequest request,
            HttpServletResponse response,
            String parameterName) throws IOException {

        String value = request.getParameter(parameterName);

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
