package io.dropwizard.employeeapiauth.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author fransiskus.prayuda
 * New annotation type to mark resources that can only be accessed if a valid token sent inside the request header
 */

@javax.ws.rs.NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface JWTRequestNeeded {

}
