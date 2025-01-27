<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Asset Management System</title>
</head>
<body>
	<h2>Welcome To Asset Management System !!!</h2>
	<form action="AdminLoginServlet" method="post" id="admin-login-form" name="admin-login-form">
		<h3>Admin</h3>
		Enter Username : <input type="text" id="admin-username" name="admin-username"> <br><br>
		Enter Password : <input type="password" id="admin-password" name="admin-password"> <br><br>
		<input type="submit" value="Admin Login">
	</form>
	<br>
	<br>
	<form action="UserLoginServlet" method="post" id="user-login-form" name="user-login-form">
		<h3>User</h3>
		Enter User-Id : <input type="text" id="user-id" name="user-id"> <br><br>
		<input type="submit" value="User Login">
	</form>
</body>
<script>
	
</script>
</html>