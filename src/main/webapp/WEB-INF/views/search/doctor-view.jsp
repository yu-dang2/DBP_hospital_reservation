<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="hospital.model.domain.Doctor, hospital.model.domain.DoctorSchedule, hospital.model.domain.DoctorOff" %>
<%-- 연결 확인용 테스트 화면. 정식 View 작업 때 교체 예정 --%>
<%
    Doctor doctor = (Doctor) request.getAttribute("doctor");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>의사 상세</title>
</head>
<body>
    <h1><%= doctor.getName() %> 상세 연결 테스트</h1>
    <p>진료과: <%= doctor.getDepartmentName() %></p>
    <p>전문 분야: <%= doctor.getSpecialty() %></p>
    <p>약력: <%= doctor.getBio() %></p>

    <h2>진료 시간표</h2>
    <ul>
    <% for (DoctorSchedule s : doctor.getSchedules()) { %>
        <li><%= s.getDayOfWeek() %> : <%= s.isDayOff() ? "휴무" : s.getTimeRange() %></li>
    <% } %>
    </ul>

    <h2>휴진 안내</h2>
    <ul>
    <% for (DoctorOff off : doctor.getOffs()) { %>
        <li><%= off.getOffDate() %> - <%= off.getReason() %></li>
    <% } %>
    </ul>

    <%-- 예약 버튼은 ReservationController의 예약 등록 화면으로 연결 --%>
    <p><a href="${pageContext.request.contextPath}/reservation?action=showReservationForm&doctorId=<%= doctor.getDoctorId() %>">이 의사에게 예약하기</a></p>
    <p><a href="${pageContext.request.contextPath}/doctor?action=listDoctors&deptId=<%= doctor.getDeptId() %>">의사 목록으로</a></p>
</body>
</html>
