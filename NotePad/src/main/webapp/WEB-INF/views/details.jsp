<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
 <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/reset.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/fontawesome-free-5.10.1-web/css/all.min.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/admin.css" />
  <script src="https://code.jquery.com/jquery-3.4.1.js"></script>
  <script src="${pageContext.request.contextPath}/resources/details.js"></script>
<title>Insert title here</title>
</head>
<body>
  <header class="top_nav">

    </header>
    <div class="content">
      <div class="button_section">
        <a href="/memo/insert" class="plus"><i class="fas fa-plus"></i></a>
        <a href="/memo/delete"><i class="fas fa-minus"></i></a>
        <a href="/memo/details"><i class="fas fa-wrench"></i></a>
        <input type="button" class="update" value="수정하러가기" />
      </div>
      <div class="table_section">
      <table>
        <colgroup>
          <col style="width: 10%;"/>
          <col />
          <col />
          <col style="width: 20%;" />
        </colgroup>
        <thead>
          <tr>
           <th>idx</th><th>no</th><th>내용</th>
          </tr>
        </thead>
        <tbody>
        </tbody>
      </table>
    </div>
    </div>
</body>
</html>