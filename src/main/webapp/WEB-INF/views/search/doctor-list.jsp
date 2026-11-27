<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, hospital.model.domain.Doctor, hospital.model.domain.Department" %>
<%-- 연결 확인용 테스트 화면. 진료과별 목록과 검색 결과에 함께 사용. 정식 View 작업 때 교체 예정 --%>
<%!
    // 사용자가 입력한 검색어를 화면에 그대로 출력하지 않도록 HTML 특수문자 변환
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    Department department = (Department) request.getAttribute("department");
    String keyword = (String) request.getAttribute("keyword");
    List<Doctor> doctorList = (List<Doctor>) request.getAttribute("doctorList");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>의사 목록</title>
</head>
<body>
    <% if (department != null) { %>
        <h1><%= department.getDeptName() %> 의사 목록 연결 테스트</h1>
    <% } else { %>
        <h1>'<%= escape(keyword) %>' 검색 결과 연결 테스트</h1>
    <% } %>

    <p>의사 수: <%= doctorList.size() %></p>
    <p style="color:red">${message}</p>

    <ul>
    <% for (Doctor doc : doctorList) { %>
        <li>
            <a href="${pageContext.request.contextPath}/doctor?action=viewDoctor&doctorId=<%= doc.getDoctorId() %>"><%= doc.getName() %></a>
            (<%= doc.getDepartmentName() %>) - <%= doc.getSpecialty() %>
        </li>
    <% } %>
    </ul>

    <p><a href="${pageContext.request.contextPath}/department">진료과 목록으로</a></p>
</body>
</html>
