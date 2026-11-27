<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 예약 현황</title>
</head>
<body>
	<h1>관리자 예약 현황 연결 테스트</h1>
	<p>예약 수: ${reservationList.size()}</p>
	<p>의사 수: ${doctorList.size()}</p>
	<p style="color:red">${errorMessage}</p>
	
	<form method="post" action = "${pageContext.request.contextPath}/admin/reservation/">
		<input type = "hidden" name="action" value="changeStatus">
		<input type = "text" name="reservationId">
		<select name="status">
		<option value="COMPLETED">진료완료</option>
		<option value="NOSHOW">노쇼</option>
		</select>
		<button type="submit">변경</button>
		
	</form>

</body>
</html>