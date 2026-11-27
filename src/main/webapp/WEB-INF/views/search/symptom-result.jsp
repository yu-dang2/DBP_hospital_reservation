<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, hospital.model.domain.SymptomRecommendation, hospital.model.domain.Department" %>
<%-- 연결 확인용 테스트 화면. 정식 View 작업 때 교체 예정 --%>
<%!
    // 사용자가 입력한 증상을 화면에 그대로 출력하지 않도록 HTML 특수문자 변환
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    String symptom = (String) request.getAttribute("symptom");
    boolean noMatch = (Boolean) request.getAttribute("noMatch");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>추천 진료과</title>
</head>
<body>
    <h1>진료과 추천 결과 연결 테스트</h1>
    <p>입력한 증상: <%= escape(symptom) %></p>

    <% if (noMatch) {
           List<Department> departmentList = (List<Department>) request.getAttribute("departmentList");
    %>
        <p>입력하신 증상과 일치하는 진료과를 찾지 못했어요. 아래 전체 진료과에서 선택해 주세요.</p>
        <ul>
        <% for (Department d : departmentList) { %>
            <li><a href="<%= ctx %>/doctor?action=listDoctors&deptId=<%= d.getDeptId() %>"><%= d.getDeptName() %></a></li>
        <% } %>
        </ul>
    <% } else {
           List<SymptomRecommendation> recommendList = (List<SymptomRecommendation>) request.getAttribute("recommendList");
    %>
        <ol>
        <% for (SymptomRecommendation rec : recommendList) { %>
            <li>
                <a href="<%= ctx %>/doctor?action=listDoctors&deptId=<%= rec.getDepartment().getDeptId() %>"><%= rec.getDepartment().getDeptName() %></a>
                - <%= rec.getScore() %>점 (일치 증상: <%= rec.getMatchedKeywordText() %>)
            </li>
        <% } %>
        </ol>
        <p>추천 결과는 참고용이며, 정확한 진단은 의료진 상담이 필요합니다.</p>
    <% } %>

    <p><a href="<%= ctx %>/symptom">다시 입력하기</a></p>
</body>
</html>
