package io.dropwizard.employeeapiauth;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.employeeapiauth.utils.AppConfiguration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import lombok.extern.log4j.Log4j;

/**
 * Unit test for simple App.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
@Log4j
public class EmployeeAuthAppTest {
	public static final DropwizardAppExtension<AppConfiguration> RULE = new DropwizardAppExtension<>(EmployeeAuthApp.class,
			new AppConfiguration());
	
	private static final String USERNAME_PARAM = "username";
	private static final String PASSWORD_PARAM = "password";
	
	private static Form validForm;
	private static Form invalidForm;
	private static WebTarget endpointTarget;
	
	@BeforeAll
	public static void setUp()
	{
		validForm = new Form()
				.param(USERNAME_PARAM, "admin")
				.param(PASSWORD_PARAM, "admin");
		
		invalidForm = new Form()
				.param(USERNAME_PARAM, "admin")
				.param(PASSWORD_PARAM, "password");
		
		endpointTarget = new JerseyClientBuilder(RULE.getEnvironment()).build("test auth").target(String.format("http://localhost:%d/login", RULE.getLocalPort()));
	}

	@Test
	public void whenGivenValidUserAndPass_thenReturnStatus200() {
		
		Response response = endpointTarget.request().post(Entity.form(EmployeeAuthAppTest.validForm));
		
		log.info("Result from call " + response.getHeaderString(AUTHORIZATION));

		assertThat(response.getStatus(), is(equalTo(HttpStatus.OK_200)));
	}
	
	@Test
	public void whenGivenInvalidUserAndPass_thenReturnStatus401() {
		
		Response response = endpointTarget.request().post(Entity.form(EmployeeAuthAppTest.invalidForm));
		
		log.info("Result from call " + response);

		assertThat(response.getStatus(), is(equalTo(HttpStatus.UNAUTHORIZED_401)));
	}
	
	@Test
	public void whenGivenToken_thenReturnStatus200WithUserName() {
		
		Response response = endpointTarget.request().post(Entity.form(EmployeeAuthAppTest.validForm));
		
		log.info("Result from call " + response.getHeaderString(AUTHORIZATION));

		assertThat(response.getStatus(), is(equalTo(HttpStatus.OK_200)));
	}
}