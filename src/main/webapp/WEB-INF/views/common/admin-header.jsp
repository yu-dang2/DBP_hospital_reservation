<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 관리자 화면 공통 헤더. 현재 메뉴 표시: <jsp:include page="/WEB-INF/views/common/admin-header.jsp"><jsp:param name="menu" value="reservation 또는 schedule" /></jsp:include> --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<header class="site-header admin-header">
  <div class="container-xl inner d-flex align-items-center justify-content-between gap-4">
    <div class="brand text-white"><img src="${ctx}/resources/img/logo.svg" alt="" width="30" height="30">딱맞닥 원무 관리 <span class="admin-badge">관리자</span></div>
    <nav class="main-nav d-flex gap-4" aria-label="관리자 메뉴">
      <a href="${ctx}/admin/reservation?action=listReservations" ${param.menu eq 'reservation' ? 'class="active" aria-current="page"' : ''}>오늘 예약 현황</a>
      <a href="${ctx}/admin/schedule?action=showSchedule" ${param.menu eq 'schedule' ? 'class="active" aria-current="page"' : ''}>진료 스케줄 관리</a>
    </nav>
    <form action="${ctx}/logout" method="post" class="m-0">
      <button type="submit" class="btn btn-sm" style="color:#fff;border:1px solid #6A7A90">로그아웃</button>
    </form>
  </div>
</header>
