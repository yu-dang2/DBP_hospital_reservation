<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- 테스트용 화면 (AI 도움을 받아 작성, 정식 View 작업 시 교체 예정) --%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>관리자 진료 스케줄</title>
</head>
<body>
	<h1>관리자 진료 스케줄 연결 테스트</h1>
	<p style="color:red">${errorMessage}</p>

	<p>전체 의사 수: ${doctorList.size()}</p>

	<h2>선택한 의사</h2>
	<p>의사 번호: ${doctor.doctorId} / 이름: ${doctor.name}</p>
	<p>일정 수: ${doctor.schedules.size()} / 휴진 수: ${doctor.offs.size()}</p>
	<p>첫 일정: 번호 ${doctor.schedules[0].scheduleId}, 요일 ${doctor.schedules[0].dayOfWeek},
		${doctor.schedules[0].startTime} ~ ${doctor.schedules[0].endTime}, 휴무 ${doctor.schedules[0].dayOff}</p>
	<p>첫 휴진: 번호 ${doctor.offs[0].offId}, 날짜 ${doctor.offs[0].offDate},
		사유 ${doctor.offs[0].reason}, 출처 ${doctor.offs[0].source}</p>

	<h2>진료시간 수정</h2>
	<form method="post" action="${pageContext.request.contextPath}/admin/schedule/">
		<input type="hidden" name="action" value="updateSchedule">
		<input type="hidden" name="scheduleId" value="${doctor.schedules[0].scheduleId}">
		<input type="hidden" name="doctorId" value="${doctor.doctorId}">
		시작 <input type="time" name="startTime">
		종료 <input type="time" name="endTime">
		<label><input type="checkbox" name="dayOff"> 휴무</label>
		<button type="submit">수정</button>
	</form>

	<h2>휴진 등록</h2>
	<form method="post" action="${pageContext.request.contextPath}/admin/schedule/">
		<input type="hidden" name="action" value="createOff">
		<input type="hidden" name="doctorId" value="${doctor.doctorId}">
		날짜 <input type="date" name="offDate">
		사유 <input type="text" name="reason">
		<button type="submit">등록</button>
	</form>

	<h2>휴진 삭제</h2>
	<form method="post" action="${pageContext.request.contextPath}/admin/schedule/">
		<input type="hidden" name="action" value="deleteOff">
		<input type="hidden" name="doctorId" value="${doctor.doctorId}">
		휴진 번호 <input type="text" name="offId">
		<button type="submit">삭제</button>
	</form>
</body>
</html>
