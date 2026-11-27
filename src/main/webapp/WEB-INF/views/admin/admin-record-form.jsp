<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 예약 폼</title>
</head>
<body>
	<h1>관리자 예약 폼 테스트</h1>
	<p>환자 이름: ${reservation.member.name}</p>
	<p>의사 이름: ${reservation.doctor.name}</p>
	<p>상태: ${reservation.status}</p>
	
	<form method="post" action = "${pageContext.request.contextPath}/admin/reservation/">
		<input type = "hidden" name="action" value="createRecord">
		<input type = "hidden" name="reservationId" value="${reservation.reservationId}">
		
		<textarea name="diagnosis" placeholder="진단 내용">${diagnosis}</textarea>
		<textarea name="prescription" placeholder="처방 내용">${prescription}</textarea>
		
		<button type="submit">완료</button>
		
	</form>
	
	<p style="color:red">${errorMessage}</p>
</body>
</html>