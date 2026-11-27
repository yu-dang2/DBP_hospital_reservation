<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- <head> 안에 넣는 공통 부분. 사용: <jsp:include page="/WEB-INF/views/common/head.jsp"><jsp:param name="title" value="화면 이름" /></jsp:include> --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${param.title}" /> | 딱맞닥</title>
<link rel="icon" href="${ctx}/resources/img/logo.svg" type="image/svg+xml">
<link rel="icon" href="${ctx}/resources/img/favicon-32.png" sizes="32x32">
<link rel="apple-touch-icon" href="${ctx}/resources/img/apple-touch-icon.png">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="${ctx}/resources/css/style.css">
