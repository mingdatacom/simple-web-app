<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>

    <c:set var="req" value="${pageContext.request}" />
	<c:set var="uri" value="${req.requestURI}" />
	<c:set var="url">${req.requestURL}</c:set>
	<base href="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/" />

    <meta charset="utf-8">
    <title>Simple Web App</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- stylesheets -->
    <link href="resources/bootstrap2/css/bootstrap.css" rel="stylesheet">
    <link href="resources/bootstrap2/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Load jQuery up here so that we can use in-page functions -->
    <script type="text/javascript" src="resources/js/lib/jquery.js"></script>
</head>

<body>

<div class="navbar navbar-inverse">
	<div class="navbar-inner">
		<div class="container">
			<button class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span> 
				<span class="icon-bar"></span> 
				<span class="icon-bar"></span>
			</button>
			<a class="brand" href="">MITREid Connect: Simple Web App</a>
				<div class="nav-collapse collapse">
					<ul class="nav">
								<li><a href=".">Home</a></li>
								<li><a href="views/user.jsp">User</a></li>
					</ul>
					<ul class="nav pull-right">
	                    <security:authorize access="hasRole('ROLE_USER')">
							<li><a href="j_spring_security_logout" data-toggle="collapse" data-target=".nav-collapse"><i class="icon-remove"></i> Log out</a></li>
	                    </security:authorize>
	                    <security:authorize access="!hasRole('ROLE_USER')">
		                    <li>
		                    	<a id="loginButton" href="login" data-toggle="collapse" data-target=".nav-collapse"><i class="icon-lock icon-white"></i> Log in</a>
		                    </li>
	                    </security:authorize>
	                </ul>

	            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>

<div class="container-fluid main">
	<div class="row-fluid">
		<div class="span10 offset1">
			
			<security:authentication property="userInfo" var="userInfo" />
			<h1>Hello ${ userInfo.name }</h1>

			<div>
				<p>This page requires that the user be logged in with a valid account and the <code>ROLE_USER</code> Spring Security authority.
				If you are reading this page, <span class="text-success">you are currently logged in</span>.</p>
				
				<h3>ID Token</h3>

				<p>Your ID Token has the following set of claims:</p>
				
				<security:authentication property="idToken" var="idToken" />
				<table class="table table-striped table-hover" id="idTokenTable">
					<thead>
						<tr>
							<th class="span1">Name</th>
							<th class="span11">Value</th>
						</tr>
					</thead>
					<tbody>
					</tbody>				
				</table>
				<p>Your id token is: ${ idToken.serialize() }</p>
				
				<h3>Access Token</h3>
				<security:authentication property="accessTokenValue" var="accessToken" />
				<p>Your access token is: ${ accessToken }</p>
				
				<h3>User Info</h3>
				
				<p>The call to the User Info Endpoint returned the following set of claims:</p>

				<table class="table table-striped table-hover" id="userInfoTable">
					<thead>
						<tr>
							<th class="span1">Name</th>
							<th class="span11">Value</th>
						</tr>
					</thead>
					<tbody>
					</tbody>				
				</table>

			</div>

		</div>
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function () {
		//parse idToken
		var idTokenString = "${ idToken.serialize() }";
		var idToken = jwt.WebTokenParser.parse(idTokenString);
		var idHeader = JSON.parse(jwt.base64urldecode(idToken.headerSegment));
		var idClaims = JSON.parse(jwt.base64urldecode(idToken.payloadSegment));
	
		_.each(idClaims, function(val, key, list) {
			if (_.contains(["iat", "exp", "auth_time", "nbf"], key)) {
				// it's a date field, parse and print it
				var date = new Date(val * 1000);
				$('#idTokenTable tbody').append('<tr><td>' + _.escape(key) + '</td><td><span title="' + _.escape(val) + '">' + date + '</span></td></tr>');
			} else {
				$('#idTokenTable tbody').append('<tr><td>' + _.escape(key) + '</td><td>' + _.escape(val) + '</td></tr>');
			}
		});
		
		_.each(idHeader, function(val, key, list) {
			if (_.contains(["iat", "exp", "auth_time", "nbf"], key)) {
				// it's a date field, parse and print it
				var date = new Date(val * 1000);
				$('#idTokenHeader tbody').append('<tr><td>' + _.escape(key) + '</td><td><span title="' + _.escape(val) + '">' + date + '</span></td></tr>');
			} else {
				$('#idTokenHeader tbody').append('<tr><td>' + _.escape(key) + '</td><td>' + _.escape(val) + '</td></tr>');
			}
		});
		//parse user info
		var userInfo = ${ userInfo.toJson() };
		_.each(userInfo, function(val, key, list) {
			$('#userInfoTable tbody').append('<tr><td>' + _.escape(key) + '</td><td>' + _.escape(val) + '</td></tr>');
		});
	});

</script>

<!-- javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="resources/bootstrap2/js/bootstrap.js"></script>
<script type="text/javascript" src="resources/js/lib/underscore.js"></script>
<script type="text/javascript" src="resources/js/lib/jwt.js"></script>
</body>
</html>