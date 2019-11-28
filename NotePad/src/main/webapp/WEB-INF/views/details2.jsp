<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/reset.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/fontawesome-free-5.10.1-web/css/all.min.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/admin.css?random=98" />
  <script src="https://code.jquery.com/jquery-3.4.1.js"></script>
  <script>var idx = ${idx_string};</script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/smarteditor/js/service/HuskyEZCreator.js" charset="utf-8"></script>
  <script src="${pageContext.request.contextPath}/resources/details_2.js?r=114"></script>


</head>
<body>
  <header class="top_nav">
   
   
  </header>
  <div class="content">
  <input type="hidden" id="idx" value="${idx_string};" />
	    <textarea id="text" name="text" cols="45" rows="10"></textarea>
 
    <div class="button_center_section">
      <a class="submit_button">수정완료</a>
    </div>
  </div>

</body>
</html>
