<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>eGovFrame 공통 컴포넌트</title>
<link href="<c:url value='/css/egovframework/com/cmm/main.css' />" rel="stylesheet" type="text/css">
<style type="text/css">
link { color: #666666; text-decoration: none; }
link:hover { color: #000000; text-decoration: none; }
</style>
</head>
<body>
<div id="lnb">
<ul class="lnb_title">
	<li>
		<strong class="left_title_strong"><strong class="top_title_strong">인증</strong></strong>
	</li>
	<ul class="2depth">
		<c:choose>
			<c:when test="${sessionScope.loginVO == null}">
				<li><a href="<c:url value='/uat/uia/egovLoginUsr.do'/>" target="_content" class="link">로그인</a></li>
				<li><a href="<c:url value='/uss/umt/EgovMberSbscrbSelect.do'/>" target="_content" class="link">회원가입</a></li>
			</c:when>
			<c:otherwise>
				<li><a href="<c:url value='/uat/uia/actionLogout.do'/>" target="_content" class="link">로그아웃</a></li>
			</c:otherwise>
		</c:choose>
	</ul>
	<li>
		<strong class="left_title_strong"><strong class="top_title_strong">프로그램</strong></strong>
	</li>
	<ul class="2depth">
		<c:if test="${sessionScope.loginVO != null && sessionScope.loginVO.userSe == 'USR'}">
			<li><a href="<c:url value='/adm/program/manage.do'/>" target="_content" class="link">프로그램 관리</a></li>
			<li><a href="<c:url value='/edu/program/list.do'/>" target="_content" class="link">프로그램 목록</a></li>
			<li><a href="<c:url value='/sym/ccm/cca/SelectCcmCmmnCodeList.do'/>" target="_content" class="link">공통코드 관리</a></li>
		</c:if>
		<c:if test="${sessionScope.loginVO != null && sessionScope.loginVO.userSe == 'GNR'}">
			<li><a href="<c:url value='/edu/program/list.do'/>" target="_content" class="link">프로그램 목록</a></li>
		</c:if>
	</ul>
</ul>

</body>
</html>
