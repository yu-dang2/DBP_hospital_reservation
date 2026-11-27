package hospital.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import hospital.model.domain.SymptomRecommendation;
import hospital.model.exception.InvalidSymptomException;
import hospital.model.manager.DoctorManager;

@WebServlet("/symptom/*")
public class SymptomController extends HttpServlet {
    private DoctorManager doctorManager;

    @Override
    public void init() throws ServletException {
        doctorManager = DoctorManager.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // action 없이 /symptom 으로 들어와도 증상 입력 화면을 보여줌
        if (action == null || "showSymptomForm".equals(action)) {
            showSymptomForm(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 한글이 깨지지 않도록 파라미터를 읽기 전에 설정
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("recommendDepartments".equals(action)) {
                recommendDepartments(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 증상 입력 화면
    private void showSymptomForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/search/symptom-form.jsp")
            .forward(request, response);
    }

    // 증상 기반 진료과 추천
    private void recommendDepartments(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String symptom = request.getParameter("symptom");

        try {
            List<SymptomRecommendation> recommendList =
                    doctorManager.recommendDepartments(symptom);

            request.setAttribute("symptom", symptom.trim());
            request.setAttribute("recommendList", recommendList);

            boolean noMatch = recommendList.isEmpty();
            request.setAttribute("noMatch", noMatch);

            if (noMatch) {
                request.setAttribute("departmentList", doctorManager.findDepartmentList());
            }

            request.getRequestDispatcher("/WEB-INF/views/search/symptom-result.jsp")
                .forward(request, response);

        } catch (InvalidSymptomException e) {
            request.setAttribute("errorMessage", e.getMessage());
            showSymptomForm(request, response);
        }
    }
}
