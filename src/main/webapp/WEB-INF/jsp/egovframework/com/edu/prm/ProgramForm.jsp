<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<title>프로그램 등록/수정</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">

<script type="text/javaScript" language="javascript" defer="defer">
function fnSave(){
    var form = document.programForm;
    if (!form.programCode.value) {
        alert("프로그램 코드를 입력하세요.");
        form.programCode.focus();
        return;
    }
    if (!form.programName.value) {
        alert("프로그램 명칭을 입력하세요.");
        form.programName.focus();
        return;
    }
    if (form.mode.value === "update") {
        form.action = "<c:url value='/adm/program/update.do'/>";
    } else {
        form.action = "<c:url value='/adm/program/insert.do'/>";
    }
    form.submit();
}

function fnDelete(){
    if (confirm("삭제하시겠습니까?")) {
        var form = document.programForm;
        form.action = "<c:url value='/adm/program/delete.do'/>";
        form.submit();
    }
}

function fnList(){
    location.href = "<c:url value='/adm/program/manage.do'/>";
}
</script>
</head>
<body>
<div class="board">
    <h1>프로그램 등록/수정</h1>
    <form name="programForm" method="post">
        <input type="hidden" name="mode" value="<c:out value='${program.programCode != null ? "update" : "insert"}'/>" />
        <table class="board_list">
            <colgroup>
                <col style="width: 20%;">
                <col style="width: 80%;">
            </colgroup>
            <tbody>
            <tr>
                <th>프로그램 코드</th>
                <td>
                    <input type="text" name="programCode" value="<c:out value='${program.programCode}'/>" <c:if test="${program.programCode != null}">readonly</c:if> />
                </td>
            </tr>
            <tr>
                <th>프로그램 명칭</th>
                <td><input type="text" name="programName" value="<c:out value='${program.programName}'/>" /></td>
            </tr>
            <tr>
                <th>기간</th>
                <td>
                    <input type="date" name="startDate" value="<c:out value='${program.startDate}'/>" />
                    ~
                    <input type="date" name="endDate" value="<c:out value='${program.endDate}'/>" />
                </td>
            </tr>
            <tr>
                <th>정원</th>
                <td><input type="number" name="capacity" value="<c:out value='${program.capacity}'/>" /></td>
            </tr>
            <tr>
                <th>사용 여부</th>
                <td>
                    <select name="useYn">
                        <option value="Y" <c:if test="${program.useYn == 'Y' || program.useYn == null}">selected</c:if>>Y</option>
                        <option value="N" <c:if test="${program.useYn == 'N'}">selected</c:if>>N</option>
                    </select>
                </td>
            </tr>
            <tr>
                <th>설명</th>
                <td><textarea name="description" rows="5" cols="60"><c:out value='${program.description}'/></textarea></td>
            </tr>
            </tbody>
        </table>
        <div style="margin-top:10px;">
            <input type="button" class="s_btn" value="저장" onclick="fnSave();" />
            <c:if test="${program.programCode != null}">
                <input type="button" class="s_btn" value="삭제" onclick="fnDelete();" />
            </c:if>
            <input type="button" class="s_btn" value="목록" onclick="fnList();" />
        </div>
    </form>
</div>
</body>
</html>
