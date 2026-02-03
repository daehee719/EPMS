<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<title>프로그램 대량 업로드</title>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/com/com.css' />">
<script type="text/javascript" src="<c:url value='/js/egovframework/com/cmm/jquery.js'/>"></script>
<style type="text/css">
.upload-wrap { max-width: 760px; margin: 30px auto; padding: 10px; }
.upload-box { padding: 16px; border: 1px solid #d0d0d0; background: #fafafa; }
.upload-actions { margin-top: 12px; }
.upload-actions input { margin-right: 6px; }
.overlay {
  display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); z-index: 9999; align-items: center; justify-content: center;
}
.overlay .panel {
  background: #fff; padding: 24px 28px; border-radius: 6px; text-align: center; min-width: 280px;
}
.spinner {
  margin: 0 auto 12px auto; width: 36px; height: 36px; border: 4px solid #ddd; border-top-color: #3b82f6; border-radius: 50%;
  animation: spin 0.9s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.status-box { margin-top: 14px; font-size: 13px; color: #444; }
</style>
</head>
<body>
<div class="upload-wrap">
  <h1>프로그램 대량 업로드</h1>
  <div class="upload-box">
    <p>CSV 또는 XLSX 파일을 업로드하세요. (최대 10,000건)</p>
    <form id="uploadForm" method="post" enctype="multipart/form-data">
      <input type="file" name="uploadFile" id="uploadFile" accept=".csv,.xlsx">
      <div class="upload-actions">
        <input type="button" value="업로드" class="s_btn" onclick="startUpload();">
        <span class="btn_s"><a href="<c:url value='/adm/program/manage.do'/>" target="_content">목록</a></span>
      </div>
    </form>
    <div id="statusBox" class="status-box"></div>
  </div>
</div>

<div id="overlay" class="overlay">
  <div class="panel">
    <div class="spinner"></div>
    <div id="overlayText">업로드 처리 중입니다...</div>
  </div>
</div>

<script type="text/javascript">
var pollTimer = null;
function startUpload() {
  var fileInput = document.getElementById("uploadFile");
  if (!fileInput.value) {
    alert("파일을 선택하세요.");
    return;
  }
  var formData = new FormData(document.getElementById("uploadForm"));
  $("#overlay").css("display", "flex");
  $("#statusBox").text("");

  $.ajax({
    url: "<c:url value='/adm/program/upload/submit.do'/>",
    type: "POST",
    data: formData,
    processData: false,
    contentType: false,
    success: function(res) {
      if (!res || !res.jobId) {
        $("#overlay").hide();
        alert("업로드 작업 생성에 실패했습니다.");
        return;
      }
      startPolling(res.jobId);
    },
    error: function() {
      $("#overlay").hide();
      alert("업로드 요청 중 오류가 발생했습니다.");
    }
  });
}

function startPolling(jobId) {
  if (pollTimer) {
    clearInterval(pollTimer);
  }
  pollTimer = setInterval(function() {
    $.ajax({
      url: "<c:url value='/adm/program/upload/status.do'/>",
      type: "GET",
      data: { jobId: jobId },
      success: function(res) {
        if (!res || !res.status) {
          return;
        }
        $("#statusBox").text("상태: " + res.status + " / 전체: " + res.totalCount + " / 성공: " + res.successCount + " / 오류: " + res.errorCount);
        if (res.status === "SUCCESS" || res.status === "FAILED") {
          clearInterval(pollTimer);
          $("#overlay").hide();
          if (res.message) {
            alert(res.message);
          }
        }
      }
    });
  }, 2000);
}
</script>
</body>
</html>
