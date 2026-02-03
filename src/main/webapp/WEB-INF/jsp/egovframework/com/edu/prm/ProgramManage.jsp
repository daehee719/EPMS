<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" uri="http://egovframework.gov/ctl/ui"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
<title>프로그램 관리</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">

<script type="text/javaScript" language="javascript" defer="defer">
function linkPage(pageNo){
    document.listForm.pageIndex.value = pageNo;
    document.listForm.action = "<c:url value='/adm/program/manage.do'/>";
    document.listForm.submit();
}

function fnSearch(){
    document.listForm.pageIndex.value = 1;
    document.listForm.action = "<c:url value='/adm/program/manage.do'/>";
    document.listForm.submit();
}

function fnDetail(programCode){
    document.listForm.programCode.value = programCode;
    document.listForm.action = "<c:url value='/adm/program/form.do'/>";
    document.listForm.submit();
}

function fnCreate(){
    location.href = "<c:url value='/adm/program/form.do'/>";
}

function fnDownloadCsv(){
    document.listForm.action = "<c:url value='/adm/program/export.csv'/>";
    document.listForm.submit();
}

function fnDownloadXlsx(){
    document.listForm.action = "<c:url value='/adm/program/export.xlsx'/>";
    document.listForm.submit();
}
</script>
</head>
<body>
<form:form name="listForm" method="post">
<input type="hidden" name="pageIndex" value="${programVO.pageIndex}" />
<input type="hidden" name="programCode" />
<div class="board">
    <h1>프로그램 관리</h1>
    <div class="search_box">
        <ul>
            <li>
                <select name="searchCondition">
                    <option value="1" <c:if test="${programVO.searchCondition == '1'}">selected</c:if>>코드</option>
                    <option value="2" <c:if test="${programVO.searchCondition == '2'}">selected</c:if>>명칭</option>
                    <option value="3" <c:if test="${programVO.searchCondition == '3'}">selected</c:if>>사용여부</option>
                </select>
            </li>
            <li>
                <input class="s_input" name="searchKeyword" type="text" size="30" value="<c:out value='${programVO.searchKeyword}'/>" />
                <input type="button" class="s_btn" value="조회" onClick="fnSearch();" />
                <span class="btn_b"><a href="#" onClick="fnCreate();return false;">등록</a></span>
                <span class="btn_b"><a href="<c:url value='/adm/program/upload.do'/>">대량업로드</a></span>
                <span class="btn_b"><a href="#" onClick="fnDownloadCsv();return false;">CSV 다운로드</a></span>
                <span class="btn_b"><a href="#" onClick="fnDownloadXlsx();return false;">엑셀 다운로드</a></span>
            </li>
            <li>
                <label>기간</label>
                <input type="date" name="filterStartDate" value="<c:out value='${programVO.filterStartDate}'/>" />
                ~
                <input type="date" name="filterEndDate" value="<c:out value='${programVO.filterEndDate}'/>" />
            </li>
            <li>
                <label>모집상태</label>
                <select name="recruitStatus">
                    <option value="" <c:if test="${empty programVO.recruitStatus}">selected</c:if>>전체</option>
                    <option value="OPEN" <c:if test="${programVO.recruitStatus == 'OPEN'}">selected</c:if>>모집중</option>
                    <option value="CLOSED" <c:if test="${programVO.recruitStatus == 'CLOSED'}">selected</c:if>>모집마감</option>
                </select>
                <label>정원</label>
                <input class="s_input" name="capacityMin" type="number" min="0" style="width:90px;" value="<c:out value='${programVO.capacityMin}'/>" />
                ~
                <input class="s_input" name="capacityMax" type="number" min="0" style="width:90px;" value="<c:out value='${programVO.capacityMax}'/>" />
                <label>등록일</label>
                <select name="regSort">
                    <option value="new" <c:if test="${programVO.regSort != 'old'}">selected</c:if>>최신순</option>
                    <option value="old" <c:if test="${programVO.regSort == 'old'}">selected</c:if>>오래된순</option>
                </select>
            </li>
        </ul>
    </div>

    <table class="board_list">
        <colgroup>
            <col style="width: 8%;">
            <col style="width: 15%;">
            <col style="width: 30%;">
            <col style="width: 18%;">
            <col style="width: 10%;">
            <col style="width: 8%;">
            <col style="width: 11%;">
        </colgroup>
        <thead>
        <tr>
            <th>번호</th>
            <th>코드</th>
            <th>명칭</th>
            <th>기간</th>
            <th>정원</th>
            <th>사용</th>
            <th>등록일</th>
        </tr>
        </thead>
        <tbody class="ov">
        <c:if test="${fn:length(programList) == 0}">
            <tr>
                <td colspan="7">데이터가 없습니다.</td>
            </tr>
        </c:if>
        <c:forEach var="item" items="${programList}" varStatus="status">
            <tr>
                <td><c:out value='${(programVO.pageIndex - 1) * programVO.pageUnit + status.index + 1}'/></td>
                <td>
                    <a href="#" onClick="fnDetail('<c:out value='${item.programCode}'/>');return false;">
                        <c:out value='${item.programCode}'/>
                    </a>
                </td>
                <td><c:out value='${item.programName}'/></td>
                <td><c:out value='${item.startDate}'/> ~ <c:out value='${item.endDate}'/></td>
                <td><c:out value='${item.capacity}'/></td>
                <td><c:out value='${item.useYn}'/></td>
                <td><c:out value='${item.regDt}'/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

    <div class="pagination">
        <ui:pagination paginationInfo="${paginationInfo}" type="image" jsFunction="linkPage" />
    </div>
</div>
</form:form>
</body>
</html>
