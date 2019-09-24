package io.dropwizard.employeeapiauth.endpoints;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.employeeapiauth.filter.JWTRequestNeeded;
import io.dropwizard.employeeapiauth.model.User;
import io.dropwizard.employeeapiauth.utils.JwtHelper;
import io.dropwizard.jackson.Jackson;
import lombok.extern.log4j.Log4j;

/**
 * @author fransiskus.prayuda
 * Holds all the HTTP endpoint resources that were exposed by the application 
 */

@Path("/")
@Produces(APPLICATION_JSON)
@Log4j
public class EmployeeAuthResource {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	
	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

	/**
	 * Resource that can be use by the user to login
	 * @param loginName string value of username
	 * @param password
	 * @param uriInfo injected uri info to fill in the authorizer property of the token
	 * @return with valid credentials the method will return authorization token that can be use in the header of next request
	 */
	@POST
	@Path(value = "/login")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response authenticateUser(@FormParam(USERNAME) String loginName, @FormParam(PASSWORD) String password, @Context UriInfo uriInfo) {
		try {
			log.info("Receieve login data : " + loginName + " " + password );

			// Authenticate the user using the credentials provided
			User authenticated = authenticate(loginName, password);

			// Issue a token for the user
			String token = JwtHelper.getInstance().issueToken(loginName, uriInfo.getAbsolutePath().toASCIIString());

			// Return the token on the response
			return Response.ok(MAPPER.writeValueAsString(authenticated)).header(AUTHORIZATION, "Bearer " + token).build();

		} catch (Exception e) {
			log.error("Log in process error", e);
			return Response.status(UNAUTHORIZED).build();
		}
	}
	
	/**
	 * Resource that only available if the user send valid token in the request header
	 * @return Response with status 200 and body "Authorized by token"
	 */
	@POST
	@Path(value = "/loggedIn")
	@Consumes(APPLICATION_FORM_URLENCODED)
	@JWTRequestNeeded
	public Response tokenAuthenticatedUser() {
		return Response.ok("Authorized by token").build();
	}

	private User authenticate(String login, String password) {
		if (!login.equalsIgnoreCase(password))
			throw new SecurityException("Invalid user/password");
		else
			return new User(login, password);
	}

}
