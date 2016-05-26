package org.mitre.web;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class AuthorizationCodeGrantTests {

	@Rule
	public ServerRunning serverRunning = ServerRunning.isRunning();

	@Test
	public void testLogin() throws Exception {

		ResponseEntity<String> page = serverRunning.getForString("/openid-connect-server-webapp/login");
		String cookie = page.getHeaders().getFirst("Set-Cookie");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookie);
		Matcher matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());

		MultiValueMap<String, String> form;
		form = new LinkedMultiValueMap<String, String>();
		form.add("j_username", "user");
		form.add("j_password", "password");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
			System.out.println("_csrf " + matcher.group(1));
		}
		ResponseEntity<Void> response = serverRunning.postForStatus("/openid-connect-server-webapp/j_spring_security_check", headers, 
				form);
		System.out.println("redirect login status code " + response.getStatusCode());
		if (response.getStatusCode().equals(HttpStatus.FOUND)) {
			System.out.println("found location " + response.getHeaders().getLocation());
		}
		assertTrue(response.getStatusCode().is3xxRedirection());//http://localhost:8080/openid-connect-server-webapp/
		ResponseEntity<String> responseString = serverRunning.getForString(response.getHeaders().getLocation().toString(), headers);
		System.out.println("login status code " + responseString.getStatusCode());
		assertTrue(responseString.getStatusCode().is2xxSuccessful());
	}
	
	@Test
	public void testTokenAcquisitionWithRegisteredRedirect() throws Exception {

		HttpHeaders headers = new HttpHeaders();

		String location = serverRunning.getForRedirect(
				"/simple-web-app/openid_connect_login?identifier=http://localhost:8080/openid-connect-server-webapp/", headers);
		System.out.println("client to login location " + location);
		String authorizeLocation = serverRunning.getForRedirect(location, headers);
		System.out.println("client to login authorizeLocation " + authorizeLocation);
		
		ResponseEntity<String> page = serverRunning.getForString(authorizeLocation);//http://localhost:8080/openid-connect-server-webapp/login
		String cookie = page.getHeaders().getFirst("Set-Cookie");
		headers.set("Cookie", cookie);
		Matcher matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());

		MultiValueMap<String, String> form;
		form = new LinkedMultiValueMap<String, String>();
		form.add("j_username", "user");
		form.add("j_password", "password");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
		}
		form.add("submit", "Login");

		 
		ResponseEntity<String> response = serverRunning.postForString("/openid-connect-server-webapp/j_spring_security_check", headers, 
				form);
		System.out.println("login status code " + response.getStatusCode());
		if (response.getStatusCode().equals(HttpStatus.FOUND)) {
			System.out.println("found location " + response.getHeaders().getLocation());
		}
		assertTrue(response.getStatusCode().is3xxRedirection());//back to /openid-connect-server-webapp 
		

		page = serverRunning.getForString(location, headers);
		if (page.getStatusCode().equals(HttpStatus.FOUND)) {
			System.out.println("request get for location " + location);
			System.out.println("get for location " + response.getHeaders().getLocation());
		}
		assertTrue(page.getStatusCode().is2xxSuccessful());
		matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());
		
		form = new LinkedMultiValueMap<String, String>();
		form.add("scope_openid", "openid");
		form.add("scope_profile", "profile");
		form.add("scope_email", "email");
		form.add("scope_address", "address");
		form.add("scope_phone", "phone");
		form.add("user_oauth_approval", "true");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
		}
		form.add("authorize", "Authorize");

		page = serverRunning.postForString("/openid-connect-server-webapp/authorize", headers, form);
		if (page.getStatusCode().equals(HttpStatus.FOUND)) {
			System.out.println("found location " + page.getHeaders().getLocation());
		}

		assertTrue("Redirect location should contain code", page.getHeaders().getLocation().toString().contains("code"));
		HttpStatus status = serverRunning.getStatusCode(location, headers);
		assertEquals(HttpStatus.OK, status);
	}

}
