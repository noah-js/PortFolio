<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
<link
	href="${pageContext.request.contextPath}/resources/vendor/fontawesome-free/css/all.css"
	rel="stylesheet" type="text/css">

<link
	href="${pageContext.request.contextPath}/resources/vendor/bootstrap/js/bootstrap.bundle.min.js"
	rel="stylesheet" type="text/css">
<link
	href="${pageContext.request.contextPath}/resources/vendor/jquery/jquery.slim.min.js"
	rel="stylesheet" type="text/css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/loginsample.css">
<head>
<title>로그인</title>
<meta charset="utf-8" />
</head>
<style>
</style>

<body>
	<div class="container">
		<div class="row">
			<div class="col-sm-9 col-md-7 col-lg-5 mx-auto">
				<div class="card card-signin my-4">
					<div class="card-body">
						<h5 class="card-title text-center">로그인</h5>
						<form class="form-signin" action="do_login" method="POST">
						
							<div class="form-label-group">
								<input type="text" id="inputEmail" name="id"
									class="form-control" placeholder="아이디" required autofocus>
								<label for="inputEmail">ID</label>
							</div>

							<div class="form-label-group">
								<input type="password" id="inputPassword" class="form-control"
									placeholder="패스워드" required="required" name="password">
								<label for="inputPassword">Password</label>
							</div>

							<button class="btn btn-lg btn-primary btn-block text-uppercase"
								type="submit">Sign in</button>
							<a href="join"
								class="btn btn-lg btn-primary btn-block text-uppercase">Sign up</a>

						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>


</html>
