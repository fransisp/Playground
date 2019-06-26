package io.dropwizard.employeeapiauth.filter;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import io.dropwizard.employeeapiauth.utils.JwtHelper;
import lombok.extern.log4j.Log4j;

@Log4j
@Provider
@JWTRequestNeeded
@Priority(Priorities.AUTHENTICATION)
public class JWTRequestFilter implements ContainerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer";

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// Get the HTTP Authorization header from the request
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
 
        try {
            // Extract the token from the HTTP Authorization header
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            // Validate the token
        	String subject = JwtHelper.getInstance().checkToken(token);
        	if (Objects.isNull(subject)) throw new NotAuthorizedException("Invalid / expired token were given");
        	else
        		log.info("#### valid token : " + token);
 
        } catch (Exception e) {
        	log.error("#### invalid / expired token ");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
		
	}

}
