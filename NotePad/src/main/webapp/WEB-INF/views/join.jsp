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
	href="${pageContext.request.contextPath}/resources/css/login.css">
<head>
<title>회원가입</title>
<meta charset="utf-8" />
</head>
<style>
</style>

<!-- This snippet uses Font Awesome 5 Free as a dependency. You can download it at fontawesome.io! -->

<body>
	<div class="container">
		<div class="row">
			<div class="col-sm-9 col-md-7 col-lg-5 mx-auto">
				<div class="card card-signin my-5">
					<div class="card-body">
						<h5 class="card-title text-center">회원가입</h5>
						<form class="form-signin" action="do_join" method="POST">

							<div class="form-label-group">
								<input type="text" id="inputid" name="id" class="form-control"
									placeholder="아이디" required autofocus> <label
									for="inputEmail">아이디</label>
							</div>

							<div class="form-label-group">
								<input type="password" id="inputPassword" class="form-control"
									placeholder="패스워드" required="required" name="password">
								<label for="inputPassword">비밀번호</label>
							</div>

							<div class="form-label-group">생년월일</div>


							<div class="form-row">
								<div class="form-group col-md-4">

									<select id="year" class="form-control" name="year" required>

										<option value="" disabled selected>년</option>
										<option value="2000">2000년</option>
										<option value="1999">1999년</option>
										<option value="1998">1998년</option>
										<option value="1997">1997년</option>
										<option value="1996">1996년</option>
										<option value="1995">1995년</option>
										<option value="1994">1994년</option>
										<option value="1993">1993년</option>
										<option value="1992">1992년</option>
										<option value="1991">1991년</option>
										<option value="1990">1990년</option>
										<option value="1989">1989년</option>
										<option value="1988">1988년</option>
										<option value="1987">1987년</option>
										<option value="1986">1986년</option>
										<option value="1985">1985년</option>
										<option value="1984">1984년</option>
										<option value="1983">1983년</option>
										<option value="1982">1982년</option>
										<option value="1981">1981년</option>
									</select>
								</div>
								<div class="form-group col-md-4">
									<select id="month" class="form-control" name="month" required>
										<option value="" disabled selected>월</option>
										<option value="01">1월</option>
										<option value="02">2월</option>
										<option value="03">3월</option>
										<option value="04">4월</option>
										<option value="05">5월</option>
										<option value="06">6월</option>
										<option value="07">7월</option>
										<option value="08">8월</option>
										<option value="09">9월</option>
										<option value="10">10월</option>
										<option value="11">11월</option>
										<option value="12">12월</option>
									</select>
								</div>
								<div class="form-group col-md-4">
									<select id="name" class="form-control" name="name" required>
										<option value="" disabled selected>일</option>
										<option value="01">1일</option>
										<option value="02">2일</option>
										<option value="03">3일</option>
										<option value="04">4일</option>
										<option value="05">5일</option>
										<option value="06">6일</option>
										<option value="07">7일</option>
										<option value="08">8일</option>
										<option value="09">9일</option>
										<option value="10">10일</option>
										<option value="11">11일</option>
										<option value="12">12일</option>
										<option value="13">13일</option>
										<option value="14">14일</option>
										<option value="15">15일</option>
										<option value="16">16일</option>
										<option value="17">17일</option>
										<option value="18">18일</option>
										<option value="19">19일</option>
										<option value="20">20일</option>
										<option value="21">21일</option>
										<option value="22">22일</option>
										<option value="23">23일</option>
										<option value="24">24일</option>
										<option value="25">25일</option>
										<option value="26">26일</option>
										<option value="27">27일</option>
										<option value="28">28일</option>
										<option value="29">29일</option>
										<option value="30">30일</option>
										<option value="31">31일</option>
									</select>
								</div>
							</div>


							<div class="form-label-group">
								<input type="text" id="inputName" name="name"
									class="form-control" placeholder="이름" required autofocus>
								<label for="inputName">이름</label>
							</div>
							<p>
							<hr>
							</p>
							<button class="btn btn-lg btn-primary btn-block text-uppercase"
								type="submit">Sign up</button>
							<a href="/memo"
								class="btn btn-lg btn-primary btn-block text-uppercase">Back</a>

						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
<script>
	
</script>

</html>