<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%-- 연결 확인용 테스트 화면. 정식 View 작업 때 교체 예정 --%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>증상으로 찾기</title>
</head>
<body>
    <h1>증상 입력 연결 테스트</h1>
    <p style="color:red">${errorMessage}</p>

    <form method="post" action="${pageContext.request.contextPath}/symptom">
        <input type="hidden" name="action" value="recommendDepartments">
        <textarea name="symptom" rows="3" cols="40" maxlength="200" placeholder="예: 머리가 아프고 어지러워요"></textarea>
        <button type="submit">진료과 찾기</button>
    </form>

    <p><a href="${pageContext.request.contextPath}/department">진료과 목록으로</a></p>
</body>
</html>
