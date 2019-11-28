<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/login.css?r=123" />

<title>Insert title here</title>
</head>
<body>
<div class="in-line">
	<form action="input_password" method="post">
		<input type="text" name="id" placeholder="id 입력" /> 
		<input type="password" name="password"placeholder="비밀번호 입력" /> 
		<input class="btn"type="submit" value="전송" />
		<a href="/signup" type="button">회원가입</a>
	</form>

</div>
</body>
</html>