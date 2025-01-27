<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ASM - UserPage</title>
</head>
<body>
	<%
	String id = String.valueOf(session.getAttribute("user-id"));
	if (session.getAttribute("user-id") == null) {
		response.sendRedirect("/asm_web_app");
	}
	%>
	
	<h1>Welcome User <%= id %></h1>
	<ol>
	<li><a href="DisplayHistoryOfUsers/<%= id %>">Display History of Single User</a></li>
	<li><a href="DisplayAssetsOfSingleUser/<%= id %>">Display Assets Allocated to Single User</a></li>
	</ol>

	<!-- Form to Raise Request -->
	<form action="RaiseRequest" method="post" id="raise-request-form" name="raise-request-form">
	<h2>Raise Request</h2>
	Enter User-ID : <input type="number" id="user-id" name="user-id"><br><br>
	Enter Asset-ID : <input type="number" id="asset-id" name="asset-id"><br><br>
	Select Operation :
	<select id="operation" name="operation">
		<option value="ASSIGN">Assign</option>
		<option value="RETAIN">Retain</option>
	</select><br><br>
	<input type="submit" value="Raise Request">
	</form>
	
	<br><br><br>
	
	<form action="UserLogoutServlet" method="post" id="user-logout-form"
		name="user-logout-form">
		<input type="submit" value="Logout">
	</form>

</body>
</html>