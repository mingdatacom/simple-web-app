package org.mitre.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@EnableOAuth2Client
@Configuration
public class OAuth2RestTemplateTester {

	private BaseOAuth2ProtectedResourceDetails resource;
	private OAuth2RestTemplate restTemplate;
	private AuthorizationCodeAccessTokenProvider accessTokenProvider;
	
	@Value("${oauth.resource:http://localhost:8080}")
    private String baseUrl;
    @Value("${oauth.authorize:http://localhost:8080/openid-connect-server-webapp/authorize}")
    private String authorizeUrl;
    @Value("${oauth.token:http://localhost:8080/openid-connect-server-webapp/token}")
    private String tokenUrl;
    
	public OAuth2RestTemplateTester() {
		resource = new BaseOAuth2ProtectedResourceDetails();
		resource.setTokenName("access_token");
		restTemplate = new OAuth2RestTemplate(resource);
		restTemplate.setAccessTokenProvider(accessTokenProvider);

	}
	
	public void retrieveAccessToken() {
		OAuth2AccessToken token = restTemplate.getAccessToken();
		for (String scope : token.getScope()) {
			System.out.println("token scope " + scope);
		}
	}
	
	public static void main(String[] args) {
		OAuth2RestTemplateTester tester = new OAuth2RestTemplateTester();
		

	}

}
