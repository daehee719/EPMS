<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>회원가입 유형 선택</title>
<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">
<style type="text/css">
.sbscrb-wrap { max-width: 560px; margin: 40px auto; padding: 20px; }
.sbscrb-title { font-size: 20px; margin-bottom: 16px; }
.sbscrb-desc { color: #666; margin-bottom: 24px; }
.sbscrb-actions { display: flex; gap: 12px; }
.sbscrb-actions a { display: inline-block; padding: 10px 16px; border: 1px solid #c8c8c8; background: #f7f7f7; text-decoration: none; color: #333; }
.sbscrb-actions a:hover { background: #efefef; }
</style>
</head>
<body>
    <div class="sbscrb-wrap">
        <div class="sbscrb-title">회원가입 유형 선택</div>
        <div class="sbscrb-desc">가입하려는 유형을 선택해 주세요.</div>
        <div class="sbscrb-actions">
            <a href="<c:url value='/uss/umt/EgovStplatCnfirmMber.do'/>">일반회원 가입</a>
            <a href="<c:url value='/uss/umt/EgovStplatCnfirmEntrprs.do'/>">기업회원 가입</a>
        </div>
    </div>
</body>
</html>
