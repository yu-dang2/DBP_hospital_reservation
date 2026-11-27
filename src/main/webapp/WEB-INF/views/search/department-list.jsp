<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, hospital.model.domain.Department" %>
<%-- 연결 확인용 테스트 화면. 정식 View 작업 때 교체 예정 --%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>진료과 목록</title>
</head>
<body>
    <h1>진료과 목록 연결 테스트</h1>

    <form method="get" action="${pageContext.request.contextPath}/doctor">
        <input type="hidden" name="action" value="searchDoctors">
        <input type="text" name="keyword" placeholder="진료과 또는 의사 이름">
        <button type="submit">검색</button>
    </form>

    <p>진료과 수: ${departmentList.size()}</p>
    <ul>
    <%
        List<Department> departmentList = (List<Department>) request.getAttribute("departmentList");
        for (Department d : departmentList) {
    %>
        <li><a href="${pageContext.request.contextPath}/doctor?action=listDoctors&deptId=<%= d.getDeptId() %>"><%= d.getDeptName() %></a></li>
    <% } %>
    </ul>

    <p><a href="${pageContext.request.contextPath}/symptom">증상으로 찾기</a></p>
</body>
</html>
