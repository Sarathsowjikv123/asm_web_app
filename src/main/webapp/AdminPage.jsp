<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ASM - Admin Page</title>
</head>
<body>
	<%
	if (session.getAttribute("admin-username") == null || session.getAttribute("admin-password") == null) {
		response.sendRedirect("/asm_web_app");
	}
	%>
	<h1>Welcome Admin !!!</h1>

	<ol>
		<li><a href="DisplayActiveAssets">Display Active Assets</a></li>
		<li><a href="DisplayActiveUsers">Display Active Users</a></li>
		<li><a href="DisplayUsersAndAllocatedAssets">Display List of
				Assets Allocated to Users</a></li>
		<li><a href="DisplayRetainedAssets">Display Retained Assets</a></li>
		<li><a href="DisplayPossibleAssignments">Display All possible
				Assignments</a></li>
		<li><a href="DisplayHistoryOfUsers/">Display History of All
				Users</a></li>
		<li><a href="DisplayHistoryOfUsers/1">Display History of
				Single User</a></li>
		<li><a href="Requests/DisplayAllRequests">Display All
				Requests</a></li>
		<li><a href="Requests/DisplayCompletedRequests">Display
				Completed Requests</a></li>
		<li><a href="Requests/DisplayInCompletedRequests">Display In
				Completed Requests</a></li>
	</ol>

	<!-- Form to Add New User -->
	<form action="AddNewUser" method="post" id="add-new-user-form">
		<h2>Add New User</h2>
		Enter Username : <input type="text" id="new-username"
			name="new-username"><br> <br> Select User Role : <select
			id="new-user-type" name="new-user-type">
			<option value="MANAGER">Manager</option>
			<option value="EMPLOYEE">Employee</option>
			<option value="TRAINEE">Trainee</option>
		</select><br> <br> <input type="submit" value="Add New User">
	</form>

	<!-- Form to add New Asset -->
	<form action="AddNewAsset" method="post" id="add-new-asset-form">
		<h2>Add New Asset</h2>
		Enter Asset Name : <input type="text" id="new-assetname"
			name="new-assetname"><br> <br> Select Asset Type :
		<select id="new-asset-type" name="new-asset-type">
			<option value="HARDWARE">Hardware</option>
			<option value="SOFTWARE">Software</option>
		</select><br> <br> Enter Asset Count : <input type="number"
			id="new-asset-count" name="new-asset-count"><br> <br>
		For : <input type="checkbox" id="to-user-types" name="to-user-types"
			value="MANAGER">Manager <input type="checkbox"
			id="to-user-types" name="to-user-types" value="EMPLOYEE">Employee
		<input type="checkbox" id="to-user-types" name="to-user-types"
			value="TRAINEE">Trainee<br> <br> <input
			type="submit" value="Add New Asset"><br> <br>
	</form>

	<!-- Form to allocate asset to an user -->
	<form action="AllocateAsset" method="post" id="allocate-asset-form"
		name="allocate-asset-form">
		<h2>Allocate Asset</h2>
		Enter User-ID : <input type="number" id="user-id" name="user-id">
		Enter Asset-ID : <input type="number" id="asset-id" name="asset-id">
		<input type="submit" value="Allocate Asset">
	</form>
	
	<!-- Form to retain asset from an user -->
	<form action="RetainAsset" method="post" id="retain-asset-form"
		name="retain-asset-form">
		<h2>Retain Asset</h2>
		Enter User-ID : <input type="number" id="user-id" name="user-id">
		Enter Asset-ID : <input type="number" id="asset-id" name="asset-id">
		<input type="submit" value="Retain Asset">
	</form>

	<!-- Form to Update Asset -->
	<form id="update-asset-form">
    <h2>Update Asset Info</h2>
    Enter Asset ID: <input type="number" id="update-asset-id" name="assetID"><br><br>
    Enter Asset Name: <input type="text" id="assetname" name="assetName"><br><br>
    Select Asset Type:
    <select id="asset-type" name="assetType">
        <option value="HARDWARE">Hardware</option>
        <option value="SOFTWARE">Software</option>
    </select><br><br>
    Enter Asset Count: <input type="number" id="asset-count" name="assetCount"><br><br>
    <button type="submit">Update Asset</button><br><br>
	</form>
	
	<!-- Form to remove an asset -->
	<form id="remove-asset-form">
		<h2>Remove Asset</h2>
		Enter Asset-ID : <input type = "number" id="remove-asset-id" name="remove-asset-id">
		<input type="submit" value="Remove Asset">
	</form>

	
	<!-- Form to remove an user -->
	<form id="remove-user-form">
		<h2>Remove User</h2>
		Enter User-ID : <input type = "number" id="remove-user-id" name="remove-user-id">
		<input type="submit" value="Remove Asset">
	</form>
	
	<!-- Retain All Assets -->
	<form action="RetainAllAssets" id="remove-user-form" name="retain-all-assets-form" method="post">
		<h2>Retain all assets</h2>
		Enter User-ID : <input type = "number" id="retain-all-user-id" name="retain-all-user-id">
		<input type="submit" value="Retain All Assets">
	</form>
	
	<form action="UpdateUserInfoAndRoles" method="post" id="update-user-info-form" name="update-user-info-form">
	<h2>Update User Info & Roles</h2>
		Enter User-ID : <input type="number" id="new-user-id" name="new-user-id">
		Enter New Username : <input type="text" id="new-user-name" name="new-user-name"><br> <br> 
		Select User Role : <select id="new-user-type-id" name="new-user-type-id">
			<option value="1">Manager</option>
			<option value="2">Employee</option>
			<option value="3">Trainee</option>
		</select><br><br>
		<input type="submit" value="Update User">
	</form>
	
	<!-- Form for Admin Logout -->
	<br>
	<br>
	<br>
	<form action="AdminLogoutServlet" method="post" id="admin-logout-form"
		name="admin-logout-form">
		<input type="submit" value="Logout">
	</form>

<script>
document.getElementById("update-asset-form").addEventListener('submit', function(event) {
    event.preventDefault();

    const assetID = document.getElementById("update-asset-id").value;
    const assetName = document.getElementById("assetname").value;
    const assetType = document.getElementById("asset-type").value;
    const assetCount = document.getElementById("asset-count").value;

    const data = {
        assetID: assetID,
        assetName: assetName,
        assetType: assetType,
        assetCount: assetCount
    };

    const xhttp = new XMLHttpRequest();
    const url = "http://localhost:8080/asm_web_app/UpdateAssetInfo";
    xhttp.open("PUT", url, true);
    xhttp.setRequestHeader('Content-Type', 'application/json');
    xhttp.onload = function () {
    	const response = xhttp.responseText;
    	console.log(response);
    };
    console.log(data);
    xhttp.send(JSON.stringify(data));
});


document.getElementById("remove-asset-form").addEventListener('submit', function(event) {
    event.preventDefault();
    
    const assetID = document.getElementById("remove-asset-id").value;
    const data = {
            assetID: assetID
    }
    const xhttp = new XMLHttpRequest();
    const url = "http://localhost:8080/asm_web_app/RemoveAsset";
    xhttp.open("POST", url, true);
    xhttp.setRequestHeader('Content-Type', 'application/json');
    xhttp.onload = function () {
    	const response = xhttp.responseText;
    	console.log(response);
    };
    console.log(data);
    xhttp.send(JSON.stringify(data));
});

document.getElementById("remove-user-form").addEventListener('submit', function(event) {
    event.preventDefault();
    
    const userID = document.getElementById("remove-user-id").value;
    const data = {
            userID: userID
    }
    const xhttp = new XMLHttpRequest();
    const url = "http://localhost:8080/asm_web_app/RemoveUser";
    xhttp.open("POST", url, true);
    xhttp.setRequestHeader('Content-Type', 'application/json');
    xhttp.onload = function () {
    	const response = xhttp.responseText;
    	console.log(response);
    };
    console.log(data);
    xhttp.send(JSON.stringify(data));
});
</script>
</body>
</html>