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

			<h2>Log In</h2>
			
			<p>Use this page to log in.</p> 
			
			<div class="well">
				<div class="row-fluid">
					
					<div class="span2">
						<form action="openid_connect_login" method="get">
							<input type="hidden" class="input-xxlarge" name="identifier" id="identifier" value="http://localhost:8080/openid-connect-server-webapp/"/>
							<input type="submit" value="Local MITREid Connect Server" />
						</form>
					</div>
				
				</div>				
		</div>
	</div>
</div>

<!-- javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="resources/bootstrap2/js/bootstrap.js"></script>
</body>
</html>