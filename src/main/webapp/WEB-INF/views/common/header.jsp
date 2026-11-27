<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 사용자 화면 공통 헤더. 사용: <jsp:include page="/WEB-INF/views/common/header.jsp" /> --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<header class="site-header">
  <div class="container-xl inner d-flex align-items-center justify-content-between gap-4">
    <a href="${ctx}/" class="brand"><img src="${ctx}/resources/img/logo.svg" alt="" width="30" height="30">딱맞닥</a>
    <nav class="main-nav d-flex gap-4" aria-label="주 메뉴">
      <a href="${ctx}/department">진료과·의사 찾기</a>
      <a href="${ctx}/symptom">증상으로 찾기</a>
      <a href="${ctx}${empty sessionScope.memberId ? '/login' : '/reservation?action=listReservations'}">내 예약</a>
    </nav>
    <c:choose>
      <c:when test="${empty sessionScope.memberId}">
        <div class="d-flex align-items-center gap-2">
          <a href="${ctx}/login" class="btn btn-line btn-sm">로그인</a>
          <a href="${ctx}/members/new" class="btn btn-primary btn-sm">회원가입</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="d-flex align-items-center gap-3">
          <c:choose>
            <c:when test="${sessionScope.role eq 'ADMIN'}">
              <a href="${ctx}/admin/reservation?action=listReservations" class="fw-semibold text-decoration-none" style="color:var(--text)">관리자 페이지</a>
            </c:when>
            <c:otherwise>
              <a href="${ctx}/members/me" class="fw-semibold text-decoration-none" style="color:var(--text)">마이페이지</a>
            </c:otherwise>
          </c:choose>
          <form action="${ctx}/logout" method="post" class="m-0">
            <button type="submit" class="btn btn-line btn-sm">로그아웃</button>
          </form>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</header>
