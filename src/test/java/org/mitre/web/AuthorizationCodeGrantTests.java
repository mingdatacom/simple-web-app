package org.mitre.web;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
public class AuthorizationCodeGrantTests {

	@Rule
	public ServerRunning serverRunning = ServerRunning.isRunning();

	private AuthorizationCodeResourceDetails resource = new AuthorizationCodeResourceDetails();

	{
		resource.setAccessTokenUri(serverRunning
				.getUrl("/openid-connect-server-webapp/token"));
		resource.setClientId("87928b55-99c2-4be3-a6d1-70586ed5120e");
		resource.setClientSecret("TkhbnOmPNYnz_EeFJAG8PJdQ0p6NAdv3_Zzm75ZQJcA-IoO4z9JNFzXPThj0dVZr66p9eh4xtK3cB4xnnFe6oA");
		resource.setId("sparklr");
		resource.setScope(Arrays.asList(new String[]{"openid", "profile"}));
		resource.setUserAuthorizationUri(serverRunning
				.getUrl("/openid-connect-server-webapp/authorize"));
	}

	@Test
	public void testTokenAcquisitionWithRegisteredRedirect() throws Exception {

		ResponseEntity<String> page = serverRunning.getForString("/openid-connect-server-webapp/login");
		String cookie = page.getHeaders().getFirst("Set-Cookie");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookie);
		Matcher matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());

		MultiValueMap<String, String> form;
		form = new LinkedMultiValueMap<String, String>();
		form.add("username", "user");
		form.add("password", "password");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
		}
		ResponseEntity<Void> response = serverRunning.postForStatus("/openid-connect-server-webapp/login", headers, 
				form);
		cookie = response.getHeaders().getFirst("Set-Cookie");

		headers = new HttpHeaders();
		headers.set("Cookie", cookie);

		// The registered redirect is /redirect, but /trigger is used as a test
		// because it is different (i.e. not the current request URI).
		String location = serverRunning.getForRedirect(
				"/simple-web-app/openid_connect_login?identifier=http://localhost:8080/openid-connect-server-webapp/", headers);
		location = authenticateAndApprove(location);

		assertTrue("Redirect location should be to the original photo URL: "
				+ location, location.contains("sparklr/redirect"));
		HttpStatus status = serverRunning.getStatusCode(location, headers);
		assertEquals(HttpStatus.OK, status);
	}

	private String authenticateAndApprove(String location) {
		HttpHeaders redirectHeaders = new HttpHeaders();
		String authorizeLocation = serverRunning.getForRedirect(location, redirectHeaders);
		//String loginLocation = serverRunning.getForRedirect(authorizeLocation, redirectHeaders);
		ResponseEntity<String> page = serverRunning.getForString(authorizeLocation);
		String cookie = page.getHeaders().getFirst("Set-Cookie");
		Matcher matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());

		MultiValueMap<String, String> form;
		form = new LinkedMultiValueMap<String, String>();
		form.add("j_username", "user");
		form.add("j_password", "password");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
		}

		HttpHeaders response = new HttpHeaders(); 
		page = serverRunning.postForString("/openid-connect-server-webapp/j_spring_security_check", form);

		cookie = response.getFirst("Set-Cookie");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Cookie", cookie);

		URI securityLocation = response.getLocation();
		String secureLocation = "";
		try {
			secureLocation = URLDecoder.decode(securityLocation.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Could not decode URL", e);
		}
		page = serverRunning.getForString(secureLocation, headers);
		matcher = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*").matcher(page.getBody());
		// Should be on user approval page now
		form = new LinkedMultiValueMap<String, String>();
		form.add("user_oauth_approval", "true");
		form.add("scope_openid", "openid");
		form.add("scope_profile", "profile");
		form.add("scope_email", "email");
		form.add("scope_address", "address");
		form.add("scope_phone", "phone");
		if (matcher.matches()) {
			form.add("_csrf", matcher.group(1));
		}
		response = serverRunning.postForHeaders("/openid-connect-server-webapp/authorize",
				form, headers);

		return response.getLocation().toString();
	}

}
