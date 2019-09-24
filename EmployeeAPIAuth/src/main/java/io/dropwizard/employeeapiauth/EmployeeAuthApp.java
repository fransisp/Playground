package io.dropwizard.employeeapiauth;

import io.dropwizard.Application;
import io.dropwizard.employeeapiauth.endpoints.EmployeeAuthResource;
import io.dropwizard.employeeapiauth.filter.JWTRequestFilter;
import io.dropwizard.employeeapiauth.utils.AppConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Main class of the Auth app
 */
public class EmployeeAuthApp extends Application<AppConfiguration>
{
	//create new instance of the app and run it with the given arguments
    public static void main(String[] args) throws Exception
    {
    	new EmployeeAuthApp().run(args);
    }
    
    @Override
    public String getName() {
        return "Employee Auth Service";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        // nothing to do yet
    }

    /**
     * Runs the app and register all the endpoint resources
     */
	@Override
	public void run(AppConfiguration configuration, Environment environment) throws Exception {
		final EmployeeAuthResource authResource = new EmployeeAuthResource();
		final JWTRequestFilter requestFilter = new JWTRequestFilter();
		environment.jersey().register(authResource);
		environment.jersey().register(requestFilter);
	}
}
